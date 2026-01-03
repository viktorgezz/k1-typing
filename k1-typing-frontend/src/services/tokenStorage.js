import Cookies from 'js-cookie'

/**
 * Сервис для управления хранением токенов
 * Единственный источник правды для операций с токенами
 */

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

// Настройки cookie для refresh token
const REFRESH_TOKEN_OPTIONS = {
  expires: 7, // 7 дней
  secure: import.meta.env.PROD, // secure только в production
  sameSite: 'lax',
}

/**
 * Сохранить access token
 * @param {string} token
 */
export function setAccessToken(token) {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
  }
}

/**
 * Получить access token
 * @returns {string|null}
 */
export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

/**
 * Удалить access token
 */
export function removeAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

/**
 * Сохранить refresh token
 * @param {string} token
 */
export function setRefreshToken(token) {
  if (token) {
    Cookies.set(REFRESH_TOKEN_KEY, token, REFRESH_TOKEN_OPTIONS)
  }
}

/**
 * Получить refresh token
 * @returns {string|undefined}
 */
export function getRefreshToken() {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

/**
 * Удалить refresh token
 */
export function removeRefreshToken() {
  Cookies.remove(REFRESH_TOKEN_KEY)
}

/**
 * Сохранить данные пользователя
 * @param {Object} user
 */
export function setUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }
}

/**
 * Получить данные пользователя
 * @returns {Object|null}
 */
export function getUser() {
  const userData = localStorage.getItem(USER_KEY)
  try {
    return userData ? JSON.parse(userData) : null
  } catch {
    return null
  }
}

/**
 * Удалить данные пользователя
 */
export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

/**
 * Сохранить все токены после логина
 * @param {string} accessToken
 * @param {string} refreshToken
 * @param {Object} user
 */
export function saveAuthData(accessToken, refreshToken, user) {
  setAccessToken(accessToken)
  setRefreshToken(refreshToken)
  setUser(user)
}

/**
 * Очистить все данные аутентификации
 */
export function clearAuthData() {
  removeAccessToken()
  removeRefreshToken()
  removeUser()
}

/**
 * Проверить наличие токенов
 * @returns {boolean}
 */
export function hasValidTokens() {
  return !!(getAccessToken() && getRefreshToken())
}

