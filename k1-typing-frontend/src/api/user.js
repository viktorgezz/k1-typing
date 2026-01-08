import apiClient from './axios'
import { clearAuthData, setUser, getUser } from '@/services/tokenStorage'

/**
 * API методы для работы с пользователем
 */
export const userAPI = {
  /**
   * Получить данные текущего пользователя
   * @returns {Promise<{username: string, role: string}>}
   */
  async getMyself() {
    const response = await apiClient.get('/user/myself')
    // Обновляем локальные данные пользователя
    const currentUser = getUser() || {}
    setUser({ ...currentUser, ...response.data })
    return response.data
  },

  /**
   * Обновить данные пользователя
   * @param {Object} data - Данные для обновления
   * @param {string} data.username - Новое имя пользователя
   * @param {string} data.newPassword - Новый пароль
   * @returns {Promise<void>}
   */
  async updateProfile(data) {
    const response = await apiClient.put('/user-update', data)
    // Обновляем локальные данные пользователя
    const currentUser = getUser() || {}
    setUser({ ...currentUser, username: data.username })
    return response.data
  },

  /**
   * Удалить аккаунт текущего пользователя
   * @returns {Promise<void>}
   */
  async deleteAccount() {
    await apiClient.delete('/user/myself')
    clearAuthData()
  },
}
