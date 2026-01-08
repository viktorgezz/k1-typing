import apiClient from './axios'

/**
 * API методы для работы с результатами
 */
export const resultItemAPI = {
  /**
   * Сохранить результат одиночного контеста
   * @param {Object} data - Данные результата
   * @param {number} data.durationSeconds - Длительность в секундах
   * @param {number} data.speed - Скорость (символов в минуту)
   * @param {number} data.accuracy - Точность (0-100)
   * @param {number} data.idContest - ID контеста
   * @returns {Promise<Object>} Записанный результат с местом
   */
  async saveSingleContestResult({ durationSeconds, speed, accuracy, idContest }) {
    const response = await apiClient.post('/result_item/single-contest', {
      durationSeconds,
      speed,
      accuracy,
      idContest,
    })
    return response.data
  },
}

