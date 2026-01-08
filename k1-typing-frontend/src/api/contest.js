import apiClient from './axios'

/**
 * API методы для работы с контестами
 */
export const contestAPI = {
  /**
   * Создать одиночный контест для упражнения
   * @param {number} idExercise - ID упражнения
   * @returns {Promise<{idContest: number}>} ID созданного контеста
   */
  async createSingle(idExercise) {
    const response = await apiClient.post('/contest/single', { idExercise })
    return response.data
  },
}

