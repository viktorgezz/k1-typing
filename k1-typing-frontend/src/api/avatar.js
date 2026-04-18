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
     * Загрузить фото и установить его как аватарку.
     * @param {File} file — файл изображения (jpeg, png, gif; до 2 МБ)
     * @returns {Promise<void>}
     */
    async uploadAvatar(file) {
        const formData = new FormData()
        formData.append('file', file)
        await apiClient.post('/avatar', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
    },
}
