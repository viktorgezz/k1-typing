import apiClient from './axios'

/**
 * API методы для модуля статистики
 */
export const statisticsAPI = {
  /**
   * Получить топ-10 пользователей (лидерборд)
   * Доступно без авторизации
   * @returns {Promise<Array<{rankPlace: number, username: string, averageSpeed: number, countContest: number, firstPlacesCount: number}>>}
   */
  async getLeaderboard() {
    const response = await apiClient.get('/statistics/leaderboard')
    return response.data
  },

  /**
   * Получить статистику по упражнениям (рекорды)
   * Доступно без авторизации
   * @returns {Promise<Array<{title: string, username: string, maxSpeed: number, minDuration: number}>>}
   */
  async getExerciseRecords() {
    const response = await apiClient.get('/statistics/exercise')
    return response.data
  },

  /**
   * Получить персональную статистику текущего пользователя
   * Требует авторизации
   * @returns {Promise<{rankPlace: number, countContest: number, averageSpeed: number, firstPlacesCount: number}>}
   */
  async getPersonalStatistics() {
    const response = await apiClient.get('/statistics/personal')
    return response.data
  },
}
