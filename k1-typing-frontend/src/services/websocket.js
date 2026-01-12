import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { getAccessToken } from './tokenStorage'

const WEBSOCKET_URL = 'http://localhost:8080/ws/contest'

/**
 * WebSocket сервис для мультиплеерных соревнований
 */
class ContestWebSocketService {
  constructor() {
    this.client = null
    this.subscriptions = new Map()
    this.connected = false
    this.contestId = null
  }

  /**
   * Подключение к WebSocket серверу
   * @param {number} contestId - ID контеста
   * @param {Object} handlers - Обработчики событий
   * @returns {Promise<void>}
   */
  connect(contestId, handlers = {}) {
    return new Promise((resolve, reject) => {
      if (this.connected && this.contestId === contestId) {
        resolve()
        return
      }

      this.disconnect()
      this.contestId = contestId

      const token = getAccessToken()

      this.client = new Client({
        webSocketFactory: () => new SockJS(WEBSOCKET_URL),
        connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('[STOMP]', str)
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      })

      this.client.onConnect = () => {
        this.connected = true
        this.subscribeToTopics(contestId, handlers)
        resolve()
      }

      this.client.onStompError = (frame) => {
        console.error('[STOMP Error]', frame.headers['message'])
        reject(new Error(frame.headers['message']))
      }

      this.client.onDisconnect = () => {
        this.connected = false
        if (handlers.onDisconnect) {
          handlers.onDisconnect()
        }
      }

      this.client.activate()
    })
  }

  /**
   * Подписка на топики контеста
   * @param {number} contestId
   * @param {Object} handlers
   */
  subscribeToTopics(contestId, handlers) {
    const topics = [
      { topic: `/topic/contest/${contestId}/progress`, handler: handlers.onProgress },
      { topic: `/topic/contest/${contestId}/player-joined`, handler: handlers.onPlayerJoined },
      { topic: `/topic/contest/${contestId}/player-left`, handler: handlers.onPlayerLeft },
      { topic: `/topic/contest/${contestId}/player-ready`, handler: handlers.onPlayerReady },
      { topic: `/topic/contest/${contestId}/player-finished`, handler: handlers.onPlayerFinished },
      { topic: `/topic/contest/${contestId}/countdown`, handler: handlers.onCountdown },
      { topic: `/topic/contest/${contestId}/start`, handler: handlers.onStart },
      { topic: `/topic/contest/${contestId}/finished`, handler: handlers.onFinished },
    ]

    topics.forEach(({ topic, handler }) => {
      if (handler) {
        const subscription = this.client.subscribe(topic, (message) => {
          try {
            const data = JSON.parse(message.body)
            handler(data)
          } catch (e) {
            console.error(`[STOMP] Error parsing message from ${topic}:`, e)
          }
        })
        this.subscriptions.set(topic, subscription)
      }
    })
  }

  /**
   * Отправка обновления прогресса
   * @param {Object} data - Данные прогресса
   * @param {number} data.progress - Процент прогресса (0-100)
   * @param {number} data.speed - Скорость (символов в минуту)
   * @param {number} data.accuracy - Точность (0-100)
   */
  sendProgress({ progress, speed, accuracy }) {
    if (!this.connected || !this.contestId) return

    this.client.publish({
      destination: `/app/contest/${this.contestId}/progress`,
      body: JSON.stringify({ progress, speed, accuracy }),
    })
  }

  /**
   * Отправка сообщения о финише
   * @param {Object} data - Данные финиша
   * @param {number} data.speed - Скорость печати
   * @param {number} data.accuracy - Точность
   * @param {number} data.durationSeconds - Время в секундах
   */
  sendFinish(data) {
    if (!this.connected || !this.contestId) return

    this.client.publish({
      destination: `/app/contest/${this.contestId}/finish`,
      body: JSON.stringify(data),
    })
  }

  /**
   * Отправка сообщения о готовности
   */
  sendReady() {
    if (!this.connected || !this.contestId) return

    this.client.publish({
      destination: `/app/contest/${this.contestId}/ready`,
      body: '{}',
    })
  }

  /**
   * Отключение от WebSocket
   */
  disconnect() {
    if (this.client) {
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe()
      })
      this.subscriptions.clear()

      if (this.client.connected) {
        this.client.deactivate()
      }
      this.client = null
    }
    this.connected = false
    this.contestId = null
  }

  /**
   * Проверка подключения
   * @returns {boolean}
   */
  isConnected() {
    return this.connected
  }
}

export const contestWebSocket = new ContestWebSocketService()
