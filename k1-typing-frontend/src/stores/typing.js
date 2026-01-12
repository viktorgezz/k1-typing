import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { exercisesAPI } from '@/api/exercises'
import { resultItemAPI } from '@/api/resultItem'

/**
 * Store для управления сессией набора текста
 */
export const useTypingStore = defineStore('typing', () => {
  // === STATE ===

  // Данные упражнения
  const exerciseId = ref(null)
  const exerciseText = ref('')
  const exerciseLanguage = ref('RU') // RU или ENG

  // Прогресс набора
  const currentIndex = ref(0)
  const typedCorrectly = ref(0)
  const errors = ref(0)
  const errorPositions = ref(new Set()) // Позиции, где была ошибка (для учёта одной ошибки на символ)

  // Состояние сессии
  const isStarted = ref(false)
  const isFinished = ref(false)
  const isPaused = ref(false)
  const startTime = ref(null)
  const endTime = ref(null)

  // Таймер
  const elapsedSeconds = ref(0)
  let timerInterval = null

  // Состояния загрузки и ошибок
  const loading = ref(false)
  const error = ref(null)
  const result = ref(null)

  // === GETTERS ===

  const currentChar = computed(() => exerciseText.value[currentIndex.value] || '')

  const typedText = computed(() => exerciseText.value.slice(0, currentIndex.value))

  const remainingText = computed(() => exerciseText.value.slice(currentIndex.value + 1))

  const progress = computed(() => {
    if (!exerciseText.value.length) return 0
    return Math.round((currentIndex.value / exerciseText.value.length) * 100)
  })

  const accuracy = computed(() => {
    const total = typedCorrectly.value + errors.value
    if (total === 0) return 100
    return Math.round((typedCorrectly.value / total) * 10000) / 100
  })

  // Скорость в символах в минуту
  const speed = computed(() => {
    if (elapsedSeconds.value === 0) return 0
    return Math.round((typedCorrectly.value / elapsedSeconds.value) * 60)
  })

  const formattedTime = computed(() => {
    const mins = Math.floor(elapsedSeconds.value / 60)
    const secs = elapsedSeconds.value % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  })

  // === ACTIONS ===

  /**
   * Инициализировать сессию набора текста
   * @param {number} id - ID упражнения
   */
  async function initSession(id) {
    loading.value = true
    error.value = null
    resetState()

    try {
      // Загружаем упражнение
      const exercise = await exercisesAPI.getById(id)
      exerciseId.value = exercise.id
      exerciseText.value = exercise.text
      exerciseLanguage.value = exercise.language || 'RU'

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Ошибка загрузки упражнения'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Обработать нажатие клавиши
   * @param {string} key - Нажатая клавиша
   * @returns {{correct: boolean, finished: boolean}}
   */
  function processKey(key) {
    if (isFinished.value || !exerciseText.value) {
      return { correct: false, finished: isFinished.value }
    }

    // Запускаем таймер при первом нажатии
    if (!isStarted.value) {
      startTimer()
      isStarted.value = true
      startTime.value = Date.now()
    }

    const expectedChar = exerciseText.value[currentIndex.value]
    const isCorrect = key === expectedChar

    if (isCorrect) {
      typedCorrectly.value++
      currentIndex.value++

      // Проверяем завершение
      if (currentIndex.value >= exerciseText.value.length) {
        finishSession()
        return { correct: true, finished: true }
      }

      return { correct: true, finished: false }
    } else {
      // Учитываем ошибку только один раз на позицию
      if (!errorPositions.value.has(currentIndex.value)) {
        errors.value++
        errorPositions.value.add(currentIndex.value)
      }
      return { correct: false, finished: false }
    }
  }

  /**
   * Запустить таймер
   */
  function startTimer() {
    if (timerInterval) return

    timerInterval = setInterval(() => {
      elapsedSeconds.value++
    }, 1000)
  }

  /**
   * Остановить таймер
   */
  function stopTimer() {
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }

  /**
   * Поставить на паузу
   */
  function pause() {
    if (!isStarted.value || isFinished.value || isPaused.value) return
    stopTimer()
    isPaused.value = true
  }

  /**
   * Продолжить после паузы
   */
  function resume() {
    if (!isPaused.value) return
    isPaused.value = false
    startTimer()
  }

  /**
   * Завершить сессию и сохранить результат
   */
  async function finishSession() {
    stopTimer()
    isFinished.value = true
    endTime.value = Date.now()

    // Сохраняем результат на сервере
    if (exerciseId.value) {
      try {
        loading.value = true
        result.value = await resultItemAPI.saveSingleContestResult({
          durationSeconds: elapsedSeconds.value,
          speed: speed.value,
          accuracy: accuracy.value,
          idExercises: exerciseId.value,
        })
      } catch (err) {
        error.value = err.response?.data?.message || 'Ошибка сохранения результата'
      } finally {
        loading.value = false
      }
    }
  }

  /**
   * Сбросить состояние
   */
  function resetState() {
    stopTimer()

    exerciseId.value = null
    exerciseText.value = ''
    exerciseLanguage.value = 'RU'
    currentIndex.value = 0
    typedCorrectly.value = 0
    errors.value = 0
    errorPositions.value = new Set()
    isStarted.value = false
    isFinished.value = false
    isPaused.value = false
    startTime.value = null
    endTime.value = null
    elapsedSeconds.value = 0
    result.value = null
    error.value = null
  }

  /**
   * Перезапустить упражнение
   */
  async function restart() {
    const id = exerciseId.value
    if (id) {
      await initSession(id)
    }
  }

  return {
    // State
    exerciseId,
    exerciseText,
    exerciseLanguage,
    currentIndex,
    typedCorrectly,
    errors,
    isStarted,
    isFinished,
    isPaused,
    elapsedSeconds,
    loading,
    error,
    result,

    // Getters
    currentChar,
    typedText,
    remainingText,
    progress,
    accuracy,
    speed,
    formattedTime,

    // Actions
    initSession,
    processKey,
    finishSession,
    resetState,
    restart,
    pause,
    resume,
  }
})

