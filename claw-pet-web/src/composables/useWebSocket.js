/**
 * composables/useWebSocket.js - WebSocket 通知连接管理
 * 支持自动重连（指数退避，最大 30 秒），提供连接/断开/消息监听能力
 */
import { ref, onUnmounted } from 'vue'

export function useWebSocket() {
  let ws = null
  let reconnectTimer = null
  let reconnectDelay = 1000
  const isConnected = ref(false)

  // 消息监听器集合
  const listeners = new Set()

  /**
   * 建立 WebSocket 连接
   * @param {string} token - 认证 token（拼接在 URL 查询参数中）
   */
  function connect(token) {
    if (!token) return
    // 先断开已有连接，避免重复
    disconnect()

    // 根据当前页面协议自动选择 ws:// 或 wss://
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = location.host
    const url = `${protocol}//${host}/ws/notification?token=${token}`

    try {
      ws = new WebSocket(url)

      ws.onopen = () => {
        isConnected.value = true
        reconnectDelay = 1000 // 重置重连间隔
      }

      // 收到消息后分发给所有注册的监听器
      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          listeners.forEach(fn => fn(data))
        } catch { /* 忽略非 JSON 消息 */ }
      }

      ws.onclose = () => {
        isConnected.value = false
        ws = null
        scheduleReconnect(token)
      }

      ws.onerror = () => {
        ws?.close()
      }
    } catch {
      scheduleReconnect(token)
    }
  }

  /** 断开 WebSocket 连接，清除重连定时器 */
  function disconnect() {
    clearTimeout(reconnectTimer)
    if (ws) {
      // 清除事件回调，避免断线时触发重连
      ws.onclose = null
      ws.onerror = null
      ws.close()
      ws = null
    }
    isConnected.value = false
  }

  /**
   * 注册消息监听器
   * @param {Function} fn - 消息回调函数，接收解析后的 data 对象
   * @returns {Function} 取消监听的函数
   */
  function onMessage(fn) {
    listeners.add(fn)
    return () => listeners.delete(fn)
  }

  /**
   * 自动重连（指数退避）
   * 重连延迟按 1s → 2s → 4s → ... → 30s 递增
   * @param {string} token
   */
  function scheduleReconnect(token) {
    clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      if (token) connect(token)
      reconnectDelay = Math.min(reconnectDelay * 2, 30000)
    }, reconnectDelay)
  }

  // 组件卸载时自动断开
  onUnmounted(() => {
    disconnect()
  })

  return { connect, disconnect, isConnected, onMessage }
}
