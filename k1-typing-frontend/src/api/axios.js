import axios from 'axios'
import {
  getAccessToken,
  getRefreshToken,
  setAccessToken,
  clearAuthData,
  isAccessTokenExpired,
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

// Флаг для предотвращения множественных запросов на проактивное обновление токена
let isProactiveRefreshing = false
// Promise для ожидания завершения проактивного обновления
let proactiveRefreshPromise = null

/**
 * Проактивное обновление токена, если он истёк
 * @returns {Promise<string|null>} Новый токен или null
 */
async function refreshTokenIfExpired() {
  const token = getAccessToken()

  // Если токена нет или он не истёк — возвращаем текущий
  if (!token || !isAccessTokenExpired()) {
    return token
  }

  // Если уже идёт обновление — ждём его завершения
  if (isProactiveRefreshing && proactiveRefreshPromise) {
    return proactiveRefreshPromise
  }

  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    clearAuthData()
    return null
  }

  isProactiveRefreshing = true
  proactiveRefreshPromise = (async () => {
    try {
      const response = await axios.post(
        `${apiClient.defaults.baseURL}/auth/refresh`,
        { refreshToken },
        { withCredentials: true }
      )
      const { access_token } = response.data
      setAccessToken(access_token)
      return access_token
    } catch {
      clearAuthData()
      return null
    } finally {
      isProactiveRefreshing = false
      proactiveRefreshPromise = null
    }
  })()

  return proactiveRefreshPromise
}

// Interceptor для добавления токена к запросам
apiClient.interceptors.request.use(
  async (config) => {
    // Проактивно обновляем токен, если он истёк
    const token = await refreshTokenIfExpired()
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
