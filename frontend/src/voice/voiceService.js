import { ElMessage } from 'element-plus'

const ICE_SERVERS = [{ urls: 'stun:stun.l.google.com:19302' }]

export function createVoiceService({ getStomp, getCurrentUser, onSignal, onStatus }) {
  let peer = null
  let localStream = null
  let remoteAudio = null
  let activePeerId = null

  function send(destination, payload) {
    const stomp = getStomp()
    if (!stomp?.connected) {
      throw new Error('实时连接未建立')
    }
    stomp.publish({ destination, body: JSON.stringify(payload) })
  }

  async function ensureLocalStream() {
    if (localStream) return localStream
    if (!navigator.mediaDevices?.getUserMedia) {
      throw new Error('当前环境不支持麦克风采集')
    }
    try {
      localStream = await navigator.mediaDevices.getUserMedia({ audio: true })
      return localStream
    } catch (error) {
      throw new Error('麦克风权限被拒绝')
    }
  }

  async function ensurePeer(toUserId) {
    if (peer) return peer
    activePeerId = toUserId
    peer = new RTCPeerConnection({ iceServers: ICE_SERVERS })
    const stream = await ensureLocalStream()
    for (const track of stream.getTracks()) {
      peer.addTrack(track, stream)
    }
    peer.onicecandidate = (event) => {
      if (event.candidate) {
        send('/app/voice.ice', { toUserId: activePeerId, candidate: event.candidate.toJSON() })
      }
    }
    peer.ontrack = (event) => {
      if (!remoteAudio) {
        remoteAudio = new Audio()
        remoteAudio.autoplay = true
      }
      remoteAudio.srcObject = event.streams[0]
    }
    peer.onconnectionstatechange = () => {
      if (['failed', 'disconnected'].includes(peer.connectionState)) {
        onStatus?.('连接失败')
      }
    }
    return peer
  }

  async function startCall(toUserId) {
    activePeerId = toUserId
    await ensureLocalStream()
    send('/app/voice.call', { toUserId })
    onStatus?.('呼叫中')
  }

  async function acceptCall(toUserId) {
    activePeerId = toUserId
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
    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)
    send('/app/voice.offer', { toUserId, sdp: JSON.stringify(offer) })
  }

  async function handleOffer(signal) {
    const pc = await ensurePeer(signal.fromUserId)
    await pc.setRemoteDescription(new RTCSessionDescription(JSON.parse(signal.sdp)))
    const answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)
    send('/app/voice.answer', { toUserId: signal.fromUserId, sdp: JSON.stringify(answer) })
    onStatus?.('通话中')
  }

  async function handleAnswer(signal) {
    if (!peer) return
    await peer.setRemoteDescription(new RTCSessionDescription(JSON.parse(signal.sdp)))
    onStatus?.('通话中')
  }

  async function handleIce(signal) {
    if (!peer || !signal.candidate) return
    await peer.addIceCandidate(new RTCIceCandidate(signal.candidate))
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
