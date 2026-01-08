import apiClient from './axios'

/**
 * API методы для работы с упражнениями
 */
export const exercisesAPI = {
  /**
   * Получить список упражнений с пагинацией
   * @param {Object} params - Параметры запроса
   * @param {number} params.page - Номер страницы (начиная с 0)
   * @param {number} params.size - Количество элементов на странице
   * @param {string} params.sort - Сортировка (например, 'id,asc')
   * @returns {Promise<Object>} Пагинированный ответ с упражнениями
   */
  async getAll({ page = 0, size = 20, sort = 'id' } = {}) {
    const response = await apiClient.get('/exercise', {
      params: { page, size, sort },
    })
    return response.data
  },

  /**
   * Получить упражнение по ID
   * @param {number} id - ID упражнения
   * @returns {Promise<Object>} Данные упражнения
   */
  async getById(id) {
    const response = await apiClient.get(`/exercise/${id}`)
    return response.data
  },
}

