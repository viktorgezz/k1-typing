import apiClient from './axios'
import {
  saveAuthData,
  clearAuthData,
  getRefreshToken,
  setAccessToken,
  setRefreshToken,
} from '@/services/tokenStorage'

/**
 * API методы для аутентификации
 */
export const authAPI = {
  /**
   * Регистрация нового пользователя
   * @param {Object} data - Данные для регистрации
   * @param {string} data.username - Имя пользователя (2-255 символов)
   * @param {string} data.password - Пароль (8-255 символов)
   * @param {string} data.confirmPassword - Подтверждение пароля (8-255 символов)
   * @returns {Promise<void>}
   */
  async register(data) {
    const response = await apiClient.post('/auth/register', data)
    return response.data
  },

  /**
   * Вход пользователя
   * @param {Object} credentials - Данные для входа
   * @param {string} credentials.name - Имя пользователя (2-255 символов)
   * @param {string} credentials.password - Пароль
   * @returns {Promise<Object>} Токены доступа
   */
  async login(credentials) {
    const response = await apiClient.post('/auth/login', credentials)
    // Backend возвращает токены в snake_case
    const { access_token, refresh_token } = response.data

    // Сохраняем токены и данные пользователя
    saveAuthData(access_token, refresh_token, { username: credentials.name })

    return response.data
  },

  /**
   * Выход пользователя
   * @returns {Promise<void>}
   */
  async logout() {
    const refreshToken = getRefreshToken()

    if (refreshToken) {
      try {
        await apiClient.post('/auth/logout', { refreshToken })
      } catch (error) {
        console.error('Logout error:', error)
      }
    }

    // Очищаем токены независимо от результата запроса
    clearAuthData()
  },

  /**
   * Обновление access токена
   * @returns {Promise<Object>} Новые токены
   */
  async refreshToken() {
    const refreshToken = getRefreshToken()

    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    const response = await apiClient.post('/auth/refresh', { refreshToken })
    // Backend возвращает токены в snake_case
    const { access_token, refresh_token: newRefreshToken } = response.data

    setAccessToken(access_token)
    if (newRefreshToken) {
      setRefreshToken(newRefreshToken)
    }

    return response.data
  },
}
