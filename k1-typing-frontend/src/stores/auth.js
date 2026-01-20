import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api/auth'
import {
  hasValidTokens,
  getUser,
  clearAuthData,
  setUser,
  getAccessToken,
} from '@/services/tokenStorage'

/**
 * Store для управления аутентификацией пользователя
 */
export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // Getters
  // Делаем зависимым от реактивного user, чтобы флаг обновлялся без перезагрузки
  const isAuthenticated = computed(() => !!user.value && hasValidTokens())

  const username = computed(() => user.value?.username || null)

  /**
   * Получить userId из JWT токена
   */
  const userId = computed(() => {
    const token = getAccessToken()
    if (!token) return null

    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.userId || payload.id || payload.sub || null
    } catch {
      return null
    }
  })

  // Actions
  /**
   * Регистрация нового пользователя
   * @param {Object} credentials - Данные для регистрации
   * @returns {Promise<{success: boolean, error?: string}>}
   */
  async function register(credentials) {
    loading.value = true
    error.value = null

    try {
      await authAPI.register(credentials)
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка регистрации'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Вход пользователя
   * @param {Object} credentials - Данные для входа
   * @returns {Promise<{success: boolean, data?: Object, error?: string}>}
   */
  async function login(credentials) {
    loading.value = true
    error.value = null

    try {
      const response = await authAPI.login(credentials)
      // Устанавливаем пользователя в store
      user.value = { username: credentials.name }
      return { success: true, data: response }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка входа'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Выход пользователя
   * @returns {Promise<{success: boolean, error?: string}>}
   */
  async function logout() {
    loading.value = true
    error.value = null

    try {
      await authAPI.logout()
      user.value = null
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка выхода'
      // Очищаем данные даже при ошибке
      user.value = null
      clearAuthData()
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Проверка и восстановление статуса аутентификации
   * Вызывается при загрузке приложения
   * @returns {boolean}
   */
  function checkAuth() {
    if (!hasValidTokens()) {
      user.value = null
      return false
    }

    // Восстанавливаем данные пользователя из storage
    const savedUser = getUser()
    if (savedUser) {
      user.value = savedUser
    }

    return true
  }

  /**
   * Обновить данные пользователя
   * @param {Object} userData
   */
  function updateUser(userData) {
    user.value = userData
    setUser(userData)
  }

  /**
   * Очистка ошибок
   */
  function clearError() {
    error.value = null
  }

  return {
    // State
    user,
    loading,
    error,
    // Getters
    isAuthenticated,
    username,
    userId,
    // Actions
    register,
    login,
    logout,
    checkAuth,
    updateUser,
    clearError,
  }
})
