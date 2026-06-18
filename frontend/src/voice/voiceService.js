import { ElMessage } from 'element-plus'

const ICE_SERVERS = [{ urls: 'stun:stun.l.google.com:19302' }]

export function getVoiceSupport() {
  if (!window.isSecureContext) {
    return { supported: false, message: '语音通话需要 HTTPS 或 localhost 环境支持麦克风权限' }
  }
  if (!navigator.mediaDevices?.getUserMedia || !window.RTCPeerConnection) {
    return { supported: false, message: '当前浏览器不支持麦克风采集或 WebRTC' }
  }
  return { supported: true, message: '' }
}

export function createVoiceService({ getStomp, getCurrentUser, onSignal, onStatus }) {
  let peer = null
  let localStream = null
  let remoteAudio = null
  let activePeerId = null
  let pendingRemoteCandidates = []

  function ensureRemoteAudio() {
    if (!remoteAudio) {
      remoteAudio = new Audio()
      remoteAudio.autoplay = true
      remoteAudio.muted = false
      remoteAudio.playsInline = true
    }
    return remoteAudio
  }

  function playRemoteAudio(action) {
    const audio = ensureRemoteAudio()
    const playPromise = audio.play()
    if (playPromise?.catch) {
      playPromise.catch((error) => {
        console.warn(`[voice] remoteAudio.play() failed after ${action}`, error)
      })
    }
  }

  function send(destination, payload) {
    const stomp = getStomp()
    if (!stomp?.connected) {
      throw new Error('实时连接未建立')
    }
    stomp.publish({ destination, body: JSON.stringify(payload) })
  }

  function hasRemoteDescription(pc) {
    return Boolean(pc?.remoteDescription?.type)
  }

  async function addRemoteCandidate(pc, candidate) {
    try {
      await pc.addIceCandidate(new RTCIceCandidate(candidate))
      console.debug('[voice] addIceCandidate success', {
        candidateType: candidate.type,
        protocol: candidate.protocol,
        address: candidate.address,
        port: candidate.port
      })
    } catch (error) {
      console.error('[voice] addIceCandidate failed', error)
      throw error
    }
  }

  async function drainPendingRemoteCandidates(pc) {
    if (!pendingRemoteCandidates.length) return
    const candidates = pendingRemoteCandidates
    pendingRemoteCandidates = []
    console.debug('[voice] drain pending remote ICE candidates', { count: candidates.length })
    for (const candidate of candidates) {
      await addRemoteCandidate(pc, candidate)
    }
  }

  async function ensureLocalStream() {
    if (localStream) return localStream
    const support = getVoiceSupport()
    if (!support.supported) {
      throw new Error(support.message)
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const audioTracks = stream.getAudioTracks()
      console.debug('[voice] getUserMedia({ audio: true }) success', { audioTrackCount: audioTracks.length })
      if (!audioTracks.length) {
        for (const track of stream.getTracks()) track.stop()
        throw new Error('未获取到麦克风音频轨道')
      }
      localStream = stream
      return localStream
    } catch (error) {
      if (error?.message === '未获取到麦克风音频轨道') throw error
      console.error('[voice] getUserMedia({ audio: true }) failed', error)
      throw new Error(error?.name === 'NotAllowedError' ? '麦克风权限被拒绝，请在浏览器设置中允许访问' : '无法访问麦克风设备')
    }
  }

  async function ensurePeer(toUserId) {
    if (peer) return peer
    activePeerId = toUserId
    console.debug('[voice] create RTCPeerConnection', { toUserId, iceServers: ICE_SERVERS })
    peer = new RTCPeerConnection({ iceServers: ICE_SERVERS })
    const stream = await ensureLocalStream()
    for (const track of stream.getAudioTracks()) {
      peer.addTrack(track, stream)
      console.debug('[voice] local audio track added to RTCPeerConnection', { trackId: track.id, enabled: track.enabled })
    }
    peer.onicecandidate = (event) => {
      if (event.candidate) {
        const candidate = event.candidate.toJSON()
        console.debug('[voice] local ICE candidate generated', {
          candidateType: candidate.type,
          protocol: candidate.protocol,
          address: candidate.address,
          port: candidate.port
        })
        console.debug('[voice] send ICE candidate', { toUserId: activePeerId })
        send('/app/voice.ice', { toUserId: activePeerId, candidate })
      } else {
        console.debug('[voice] local ICE candidate gathering complete')
      }
    }
    peer.ontrack = (event) => {
      const audio = ensureRemoteAudio()
      const remoteStream = event.streams[0] || new MediaStream([event.track])
      console.debug('[voice] pc.ontrack received remote stream', {
        streamId: remoteStream.id,
        audioTrackCount: remoteStream.getAudioTracks().length
      })
      audio.srcObject = remoteStream
      audio.muted = false
      playRemoteAudio('remote track received')
    }
    peer.oniceconnectionstatechange = () => {
      console.debug('[voice] ICE connection state changed', peer.iceConnectionState)
      if (peer.iceConnectionState === 'failed') {
        console.error('[voice] ICE failed', peer.iceConnectionState)
      } else if (peer.iceConnectionState === 'disconnected') {
        console.warn('[voice] ICE disconnected', peer.iceConnectionState)
      }
    }
    peer.onconnectionstatechange = () => {
      console.debug('[voice] peer connection state changed', peer.connectionState)
      if (peer.connectionState === 'failed') {
        console.error('[voice] WebRTC connection failed', peer.connectionState)
        onStatus?.('连接失败')
      } else if (peer.connectionState === 'disconnected') {
        console.warn('[voice] WebRTC connection disconnected', peer.connectionState)
      }
    }
    return peer
  }

  async function startCall(toUserId) {
    activePeerId = toUserId
    ensureRemoteAudio()
    playRemoteAudio('start call click')
    await ensureLocalStream()
    send('/app/voice.call', { toUserId })
    onStatus?.('呼叫中')
  }

  async function acceptCall(toUserId) {
    activePeerId = toUserId
    ensureRemoteAudio()
    playRemoteAudio('accept call click')
    await ensurePeer(toUserId)
    send('/app/voice.accept', { toUserId })
    onStatus?.('通话中')
  }

  function rejectCall(toUserId) {
    send('/app/voice.reject', { toUserId })
    cleanup()
    onStatus?.('已拒绝')
  }

  async function createOffer(toUserId) {
    const pc = await ensurePeer(toUserId)
    console.debug('[voice] createOffer')
    const offer = await pc.createOffer()
    console.debug('[voice] setLocalDescription offer')
    await pc.setLocalDescription(offer)
    send('/app/voice.offer', { toUserId, sdp: JSON.stringify(offer) })
    console.debug('[voice] send offer', { toUserId })
  }

  async function handleOffer(signal) {
    console.debug('[voice] receive offer', { fromUserId: signal.fromUserId })
    const pc = await ensurePeer(signal.fromUserId)
    console.debug('[voice] setRemoteDescription offer')
    await pc.setRemoteDescription(new RTCSessionDescription(JSON.parse(signal.sdp)))
    await drainPendingRemoteCandidates(pc)
    console.debug('[voice] createAnswer')
    const answer = await pc.createAnswer()
    console.debug('[voice] setLocalDescription answer')
    await pc.setLocalDescription(answer)
    send('/app/voice.answer', { toUserId: signal.fromUserId, sdp: JSON.stringify(answer) })
    console.debug('[voice] send answer', { toUserId: signal.fromUserId })
    onStatus?.('通话中')
  }

  async function handleAnswer(signal) {
    if (!peer) return
    console.debug('[voice] receive answer', { fromUserId: signal.fromUserId })
    console.debug('[voice] setRemoteDescription answer')
    await peer.setRemoteDescription(new RTCSessionDescription(JSON.parse(signal.sdp)))
    await drainPendingRemoteCandidates(peer)
    onStatus?.('通话中')
  }

  async function handleIce(signal) {
    if (!signal.candidate) return
    console.debug('[voice] receive ICE candidate', {
      fromUserId: signal.fromUserId,
      candidateType: signal.candidate.type,
      protocol: signal.candidate.protocol,
      address: signal.candidate.address,
      port: signal.candidate.port
    })
    activePeerId = signal.fromUserId || activePeerId
    if (!peer || !hasRemoteDescription(peer)) {
      pendingRemoteCandidates.push(signal.candidate)
      console.debug('[voice] queue remote ICE candidate until remoteDescription is set', {
        hasPeer: Boolean(peer),
        pendingCount: pendingRemoteCandidates.length
      })
      return
    }
    await addRemoteCandidate(peer, signal.candidate)
  }

  function hangup(toUserId = activePeerId) {
    if (toUserId) {
      send('/app/voice.hangup', { toUserId })
    }
    cleanup()
    onStatus?.('已挂断')
  }

  function cleanup() {
    if (peer) {
      peer.close()
      peer = null
    }
    if (localStream) {
      for (const track of localStream.getTracks()) track.stop()
      localStream = null
    }
    if (remoteAudio) {
      remoteAudio.srcObject = null
      remoteAudio = null
    }
    pendingRemoteCandidates = []
    activePeerId = null
  }

  async function handleSignal(signal) {
    try {
      if (signal.type === 'CALL') {
        onSignal?.(signal)
      } else if (signal.type === 'ACCEPT') {
        onStatus?.('通话中')
        await createOffer(signal.fromUserId)
      } else if (signal.type === 'REJECT') {
        cleanup()
        onStatus?.('对方拒绝')
      } else if (signal.type === 'OFFER') {
        await handleOffer(signal)
      } else if (signal.type === 'ANSWER') {
        await handleAnswer(signal)
      } else if (signal.type === 'ICE') {
        await handleIce(signal)
      } else if (signal.type === 'HANGUP') {
        cleanup()
        onStatus?.('已挂断')
      } else if (signal.type === 'ERROR') {
        cleanup()
        onStatus?.(signal.message || '连接失败')
        ElMessage.error(signal.message || '连接失败')
      }
    } catch (error) {
      cleanup()
      onStatus?.('连接失败')
      ElMessage.error(error.message || '语音连接失败')
    }
  }

  return {
    startCall,
    acceptCall,
    rejectCall,
    hangup,
    handleSignal,
    cleanup,
    getCurrentUser
  }
}
