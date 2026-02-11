import apiClient from './axios'

/**
 * API методы для работы с балансом
 */
export const balanceAPI = {
    /**
     * Получить баланс текущего пользователя
     * @returns {Promise<{balance: number}>}
     */
    async viewOwnBalance() {
        const response = await apiClient.get('/balance')
        return response.data
    },
}
