import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { multiplayerAPI } from '@/api/multiplayer'

/**
 * Store для управления мультиплеерными комнатами
 */
export const useMultiplayerStore = defineStore('multiplayer', () => {
  // State
  const rooms = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Пагинация
  const pagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  })

  // Getters
  const hasRooms = computed(() => rooms.value.length > 0)

  const hasNextPage = computed(() => pagination.value.page < pagination.value.totalPages - 1)

  const hasPrevPage = computed(() => pagination.value.page > 0)

  const currentPage = computed(() => pagination.value.page + 1)

  // Actions

  /**
   * Загрузить доступные комнаты с пагинацией
   * @param {Object} params
   * @param {number} params.page - Номер страницы (0-based)
   * @param {number} params.size - Размер страницы
   * @returns {Promise<{success: boolean, error?: string}>}
   */
  async function fetchRooms({ page = 0, size = 20 } = {}) {
    loading.value = true
    error.value = null

    try {
      const response = await multiplayerAPI.getAvailableRooms({ page, size })

      // Обрабатываем Spring PagedModel структуру
      rooms.value = response.content || []

      pagination.value = {
        page: response.page?.number ?? page,
        size: response.page?.size ?? size,
        totalElements: response.page?.totalElements ?? 0,
        totalPages: response.page?.totalPages ?? 0,
      }

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки комнат'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Перейти на следующую страницу
   */
  async function nextPage() {
    if (hasNextPage.value) {
      return fetchRooms({
        page: pagination.value.page + 1,
        size: pagination.value.size,
      })
    }
  }

  /**
   * Перейти на предыдущую страницу
   */
  async function prevPage() {
    if (hasPrevPage.value) {
      return fetchRooms({
        page: pagination.value.page - 1,
        size: pagination.value.size,
      })
    }
  }

  /**
   * Перейти на конкретную страницу
   * @param {number} page - Номер страницы (0-based)
   */
  async function goToPage(page) {
    if (page >= 0 && page < pagination.value.totalPages) {
      return fetchRooms({
        page,
        size: pagination.value.size,
      })
    }
  }

  /**
   * Создать новую комнату
   * @param {Object} data - Данные для создания комнаты
   * @param {number} data.idExercise - ID упражнения
   * @param {number} data.maxParticipants - Максимальное количество участников
   * @returns {Promise<{success: boolean, data?: Object, error?: string}>}
   */
  async function createRoom(data) {
    loading.value = true
    error.value = null

    try {
      const response = await multiplayerAPI.createRoom(data)
      // Обновляем список комнат после создания
      await fetchRooms({ page: pagination.value.page, size: pagination.value.size })
      return { success: true, data: response }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка создания комнаты'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Очистка ошибок
   */
  function clearError() {
    error.value = null
  }

  /**
   * Сброс состояния
   */
  function reset() {
    rooms.value = []
    pagination.value = {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    }
    error.value = null
  }

  return {
    // State
    rooms,
    loading,
    error,
    pagination,
    // Getters
    hasRooms,
    hasNextPage,
    hasPrevPage,
    currentPage,
    // Actions
    fetchRooms,
    nextPage,
    prevPage,
    goToPage,
    createRoom,
    clearError,
    reset,
  }
})
