import apiClient from './axios'

/**
 * API для работы с аватарками пользователей.
 */
export const avatarAPI = {
    /**
     * Получить аватарку текущего пользователя.
     * @returns {Promise<{ photo: string, contentType: string }>}
     */
    async getMyAvatar() {
        const response = await apiClient.get('/avatar/me')
        return response.data
    },

    /**
     * Получить аватарки участников по списку ID.
     * @param {number[]} idsUser — массив userId
     * @returns {Promise<Array<{ idUser: number, photo: string, contentType: string }>>}
     */
    async getAvatarsByUserIds(idsUser) {
        const response = await apiClient.get('/avatar', {
            params: { idsUser: idsUser.join(',') },
        })
        return response.data
    },

    /**
     * Запустить асинхронную генерацию аватарки по промту.
     * @param {string} prompt — текстовое описание для генерации
     * @returns {Promise<void>}
     */
    async generateAvatar(prompt) {
        await apiClient.post('/avatar', null, {
            params: { promt: prompt },
        })
    },
}
