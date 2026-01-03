import axios from 'axios'
import {
  getAccessToken,
  getRefreshToken,
  setAccessToken,
  clearAuthData,
} from '@/services/tokenStorage'

// Флаг для предотвращения множественных запросов на обновление токена
let isRefreshing = false
// Очередь запросов, ожидающих обновления токена
let failedQueue = []

/**
 * Обработка очереди запросов после обновления токена
 * @param {Error|null} error
 * @param {string|null} token
 */
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// Создаем экземпляр axios с базовой конфигурацией
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

// Interceptor для добавления токена к запросам
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor для обработки ошибок и обновления токена
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Если получили 401 и это не повторный запрос
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Если уже идет процесс обновления токена, добавляем запрос в очередь
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return apiClient(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          throw new Error('No refresh token')
        }

        // Пытаемся обновить токен используя чистый axios, чтобы избежать циклической зависимости
        const response = await axios.post(
          `${apiClient.defaults.baseURL}/auth/refresh`,
          { refreshToken },
          { withCredentials: true }
        )

        // Backend возвращает токены в snake_case
        const { access_token } = response.data

        // Сохраняем новый access token
        setAccessToken(access_token)

        // Обрабатываем очередь запросов
        processQueue(null, access_token)

        // Повторяем оригинальный запрос с новым токеном
        originalRequest.headers.Authorization = `Bearer ${access_token}`
        return apiClient(originalRequest)
      } catch (refreshError) {
        // Обрабатываем очередь с ошибкой
        processQueue(refreshError, null)

        // Если обновление токена не удалось, очищаем данные и редиректим на логин
        clearAuthData()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
