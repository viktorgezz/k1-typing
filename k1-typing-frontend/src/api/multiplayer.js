import apiClient from './axios'

/**
 * API методы для работы с мультиплеерными комнатами
 */
export const multiplayerAPI = {
  /**
   * Получить список доступных комнат с пагинацией
   * @param {Object} params - Параметры запроса
   * @param {number} params.page - Номер страницы (начиная с 0)
   * @param {number} params.size - Количество элементов на странице
   * @param {string} params.sort - Сортировка (например, 'id,asc')
   * @returns {Promise<Object>} Пагинированный ответ с комнатами
   */
  async getAvailableRooms({ page = 0, size = 20, sort = 'id' } = {}) {
    const response = await apiClient.get('/multiplayer/room/available', {
      params: { page, size, sort },
    })
    return response.data
  },

  /**
   * Получить информацию о комнате
   * @param {number} idContest - ID контеста
   * @returns {Promise<Object>} Данные комнаты
   */
  async getRoomInfo(idContest) {
    const response = await apiClient.get(`/multiplayer/room/${idContest}`)
    return response.data
  },

  /**
   * Создать новую комнату
   * @param {Object} data - Данные для создания комнаты
   * @param {number} data.idExercise - ID упражнения
   * @param {number} data.maxParticipants - Максимальное количество участников (2-15)
   * @returns {Promise<Object>} Результат создания комнаты
   */
  async createRoom(data) {
    const response = await apiClient.post('/multiplayer/room', data)
    return response.data
  },

  /**
   * Присоединиться к комнате
   * @param {number} idContest - ID контеста
   * @returns {Promise<Object>} Результат присоединения
   */
  async joinRoom(idContest) {
    const response = await apiClient.post(`/multiplayer/room/${idContest}/join`)
    return response.data
  },

  /**
   * Покинуть комнату
   * @param {number} idContest - ID контеста
   * @returns {Promise<void>}
   */
  async leaveRoom(idContest) {
    await apiClient.delete(`/multiplayer/room/${idContest}/leave`)
  },
}
