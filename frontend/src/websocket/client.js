import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function createStompClient(token, handlers = {}) {
  const baseUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'
  return new Client({
    webSocketFactory: () => new SockJS(baseUrl),
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    reconnectDelay: 1000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: handlers.onConnect,
    onDisconnect: handlers.onDisconnect,
    onStompError: handlers.onError
  })
}

