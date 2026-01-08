import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { exercisesAPI } from '@/api/exercises'

/**
 * Store для управления упражнениями
 */
export const useExercisesStore = defineStore('exercises', () => {
  // State
  const exercises = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Пагинация
  const pagination = ref({
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
  })

  // Getters
  const hasExercises = computed(() => exercises.value.length > 0)

  const hasNextPage = computed(() => pagination.value.page < pagination.value.totalPages - 1)

  const hasPrevPage = computed(() => pagination.value.page > 0)

  const currentPage = computed(() => pagination.value.page + 1) // Для отображения (1-based)

  // Actions

  /**
   * Загрузить упражнения с пагинацией
   * @param {Object} params
   * @param {number} params.page - Номер страницы (0-based)
   * @param {number} params.size - Размер страницы
   * @returns {Promise<{success: boolean, error?: string}>}
   */
  async function fetchExercises({ page = 0, size = 12 } = {}) {
    loading.value = true
    error.value = null

    try {
      const response = await exercisesAPI.getAll({ page, size })

      // Обрабатываем Spring PagedModel структуру
      exercises.value = response.content || []

      pagination.value = {
        page: response.page?.number ?? page,
        size: response.page?.size ?? size,
        totalElements: response.page?.totalElements ?? 0,
        totalPages: response.page?.totalPages ?? 0,
      }

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки упражнений'
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
      return fetchExercises({
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
      return fetchExercises({
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
      return fetchExercises({
        page,
        size: pagination.value.size,
      })
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
    exercises.value = []
    pagination.value = {
      page: 0,
      size: 12,
      totalElements: 0,
      totalPages: 0,
    }
    error.value = null
  }

  return {
    // State
    exercises,
    loading,
    error,
    pagination,
    // Getters
    hasExercises,
    hasNextPage,
    hasPrevPage,
    currentPage,
    // Actions
    fetchExercises,
    nextPage,
    prevPage,
    goToPage,
    clearError,
    reset,
  }
})

