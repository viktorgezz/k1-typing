import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { statisticsAPI } from '@/api/statistics'

/**
 * Store для управления данными статистики
 */
export const useStatisticsStore = defineStore('statistics', () => {
  // State
  const leaderboard = ref([])
  const exerciseRecords = ref([])
  const personalStats = ref(null)

  const loadingLeaderboard = ref(false)
  const loadingExercises = ref(false)
  const loadingPersonal = ref(false)

  const errorLeaderboard = ref(null)
  const errorExercises = ref(null)
  const errorPersonal = ref(null)

  // Getters
  const hasLeaderboard = computed(() => leaderboard.value.length > 0)
  const hasExerciseRecords = computed(() => exerciseRecords.value.length > 0)
  const hasPersonalStats = computed(() => personalStats.value !== null)

  // Actions

  /**
   * Загрузить лидерборд (топ-10 пользователей)
   */
  async function fetchLeaderboard() {
    loadingLeaderboard.value = true
    errorLeaderboard.value = null

    try {
      leaderboard.value = await statisticsAPI.getLeaderboard()
    } catch (err) {
      errorLeaderboard.value = err.response?.data?.message || 'Ошибка загрузки лидерборда'
    } finally {
      loadingLeaderboard.value = false
    }
  }

  /**
   * Загрузить рекорды по упражнениям
   */
  async function fetchExerciseRecords() {
    loadingExercises.value = true
    errorExercises.value = null

    try {
      exerciseRecords.value = await statisticsAPI.getExerciseRecords()
    } catch (err) {
      errorExercises.value = err.response?.data?.message || 'Ошибка загрузки рекордов'
    } finally {
      loadingExercises.value = false
    }
  }

  /**
   * Загрузить персональную статистику
   */
  async function fetchPersonalStatistics() {
    loadingPersonal.value = true
    errorPersonal.value = null

    try {
      personalStats.value = await statisticsAPI.getPersonalStatistics()
    } catch (err) {
      errorPersonal.value = err.response?.data?.message || 'Ошибка загрузки персональной статистики'
    } finally {
      loadingPersonal.value = false
    }
  }

  return {
    // State
    leaderboard,
    exerciseRecords,
    personalStats,
    loadingLeaderboard,
    loadingExercises,
    loadingPersonal,
    errorLeaderboard,
    errorExercises,
    errorPersonal,
    // Getters
    hasLeaderboard,
    hasExerciseRecords,
    hasPersonalStats,
    // Actions
    fetchLeaderboard,
    fetchExerciseRecords,
    fetchPersonalStatistics,
  }
})
