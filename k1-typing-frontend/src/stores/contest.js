import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { multiplayerAPI } from '@/api/multiplayer'
import { avatarAPI } from '@/api/avatar'
import { contestWebSocket } from '@/services/websocket'

/**
 * Store для управления состоянием соревновательного режима
 */
export const useContestStore = defineStore('contest', () => {
  // === STATE ===

  // Информация о комнате
  const contestId = ref(null)
  const exerciseId = ref(null)
  const exerciseTitle = ref('')
  const exerciseText = ref('')
  const exerciseLanguage = ref('RU')
  const maxPlayers = ref(2)

  // Состояние соревнования (синхронизировано с бэкендом)
  // CREATED = лобби, WAITING = обратный отсчёт, PROGRESS = гонка, FINISHED = результаты
  const status = ref('CREATED')
  const countdownSeconds = ref(0)
  const startTimestamp = ref(null)

  // Участники: Map<userId, { username, progress, isReady, isFinished, place, speed, accuracy, duration }>
  const participants = ref(new Map())

  // Текущий пользователь
  const currentUserId = ref(null)
  const currentUserReady = ref(false)
  const currentUserFinished = ref(false)

  // Прогресс набора текущего пользователя
  const currentIndex = ref(0)
  const typedCorrectly = ref(0)
  const errors = ref(0)
  const errorPositions = ref(new Set())

  // Таймер
  const elapsedSeconds = ref(0)
  let timerInterval = null

  // Состояния загрузки и ошибок
  const loading = ref(false)
  const error = ref(null)
  const isConnected = ref(false)

  // Финальные результаты
  const leaderboard = ref([])
  const myPlace = ref(null)

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

  const speed = computed(() => {
    if (elapsedSeconds.value === 0) return 0
    return Math.round((typedCorrectly.value / elapsedSeconds.value) * 60)
  })

  const formattedTime = computed(() => {
    const mins = Math.floor(elapsedSeconds.value / 60)
    const secs = elapsedSeconds.value % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  })

  const participantsList = computed(() => {
    return Array.from(participants.value.entries()).map(([id, data]) => ({
      id,
      ...data,
    }))
  })

  const readyCount = computed(() => {
    return participantsList.value.filter((p) => p.isReady).length
  })

  const totalParticipants = computed(() => participants.value.size)

  // Статусы синхронизированы с бэкендом
  const isWaiting = computed(() => status.value === 'CREATED') // Лобби - ожидание участников
  const isCountdown = computed(() => status.value === 'WAITING') // Обратный отсчёт
  const isInProgress = computed(() => status.value === 'PROGRESS') // Гонка
  const isFinished = computed(() => status.value === 'FINISHED') // Результаты

  const canStart = computed(() => {
    return readyCount.value >= 2 && readyCount.value === totalParticipants.value
  })

  // === ACTIONS ===

  /**
   * Присоединиться к комнате
   */
  async function joinRoom(idContest, userId) {
    loading.value = true
    error.value = null

    try {
      // Присоединяемся через REST
      const joinResult = await multiplayerAPI.joinRoom(idContest)

      if (joinResult.status !== 'SUCCESS') {
        throw new Error(joinResult.message || 'Не удалось присоединиться к комнате')
      }

      contestId.value = joinResult.idContest
      currentUserId.value = userId

      // Загружаем информацию о комнате
      const roomInfo = await multiplayerAPI.getRoomInfo(idContest)
      applyRoomInfo(roomInfo)

      // Подключаемся к WebSocket
      await connectWebSocket()

      // Загружаем аватарки участников (не блокируем основной поток)
      fetchParticipantAvatars()

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Ошибка подключения к комнате'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Создать комнату и присоединиться
   */
  async function createAndJoinRoom(idExercise, maxParticipants, userId) {
    loading.value = true
    error.value = null

    try {
      // Создаём комнату
      const createResult = await multiplayerAPI.createRoom({
        idExercise,
        maxParticipants,
      })

      contestId.value = createResult.idContest
      currentUserId.value = userId

      // Загружаем информацию о комнате
      const roomInfo = await multiplayerAPI.getRoomInfo(createResult.idContest)
      applyRoomInfo(roomInfo)

      // Подключаемся к WebSocket
      await connectWebSocket()

      // Загружаем аватарки участников (не блокируем основной поток)
      fetchParticipantAvatars()

      return { success: true, contestId: createResult.idContest }
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Ошибка создания комнаты'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * Применить информацию о комнате
   */
  function applyRoomInfo(roomInfo) {
    exerciseId.value = roomInfo.idExercise
    exerciseTitle.value = roomInfo.titleExercise
    exerciseLanguage.value = roomInfo.exerciseLanguage || 'RU'
    maxPlayers.value = roomInfo.maxPlayers
    status.value = roomInfo.status

    // Если соревнование уже идёт и есть текст - восстанавливаем
    if (roomInfo.exerciseText && (roomInfo.status === 'PROGRESS' || roomInfo.status === 'FINISHED')) {
      exerciseText.value = roomInfo.exerciseText
      // Запускаем таймер если соревнование в процессе
      if (roomInfo.status === 'PROGRESS') {
        startTimer()
      }
    }

    // Очищаем и заполняем участников
    participants.value.clear()
    roomInfo.participants.forEach((p) => {
      participants.value.set(p.idUser, {
        username: p.username,
        progress: p.progress ?? 0,
        isReady: p.isReady,
        isFinished: false,
        place: null,
        speed: p.speed ?? 0,
        accuracy: p.accuracy ?? 0,
        duration: 0,
        avatarPhoto: null,
        avatarContentType: 'image/png',
      })
    })

    // Обновляем статус готовности текущего пользователя
    if (currentUserId.value && participants.value.has(currentUserId.value)) {
      currentUserReady.value = participants.value.get(currentUserId.value).isReady
    }
  }

  /**
   * Подключиться к WebSocket
   */
  async function connectWebSocket() {
    if (!contestId.value) return

    try {
      await contestWebSocket.connect(contestId.value, {
        onProgress: handleProgress,
        onPlayerJoined: handlePlayerJoined,
        onPlayerLeft: handlePlayerLeft,
        onPlayerReady: handlePlayerReady,
        onPlayerFinished: handlePlayerFinished,
        onCountdown: handleCountdown,
        onStart: handleStart,
        onFinished: handleFinished,
        onDisconnect: handleDisconnect,
      })
      isConnected.value = true
    } catch (err) {
      error.value = 'Ошибка WebSocket подключения'
      isConnected.value = false
    }
  }

  // === WebSocket Handlers ===

  function handleProgress(data) {
    if (data.usersProgress) {
      Object.entries(data.usersProgress).forEach(([id, progressData]) => {
        const userId = Number(id)
        if (participants.value.has(userId)) {
          const participant = participants.value.get(userId)
          // progressData теперь содержит { progress, speed, accuracy }
          participant.progress = progressData.progress ?? 0
          participant.speed = progressData.speed ?? 0
          participant.accuracy = progressData.accuracy ?? 0
        }
      })
    }
  }

  function handlePlayerJoined(data) {
    if (!participants.value.has(data.userId)) {
      participants.value.set(data.userId, {
        username: data.userName,
        progress: 0,
        isReady: false,
        isFinished: false,
        place: null,
        speed: 0,
        accuracy: 0,
        duration: 0,
        avatarPhoto: null,
        avatarContentType: 'image/png',
      })

      // Загружаем аватарку нового участника
      fetchSingleAvatar(data.userId)
    }
  }

  /**
   * Загрузить аватарки всех текущих участников
   */
  async function fetchParticipantAvatars() {
    const ids = Array.from(participants.value.keys())
    if (ids.length === 0) return

    try {
      const avatars = await avatarAPI.getAvatarsByUserIds(ids)
      avatars.forEach((avatar) => {
        if (participants.value.has(avatar.idUser)) {
          const participant = participants.value.get(avatar.idUser)
          participant.avatarPhoto = avatar.photo
          participant.avatarContentType = avatar.contentType || 'image/png'
        }
      })
    } catch (err) {
      // Аватарки не критичны — молча игнорируем ошибку
      console.warn('Failed to load participant avatars:', err)
    }
  }

  /**
   * Загрузить аватарку одного участника
   */
  async function fetchSingleAvatar(userId) {
    try {
      const avatars = await avatarAPI.getAvatarsByUserIds([userId])
      if (avatars.length > 0 && participants.value.has(userId)) {
        const participant = participants.value.get(userId)
        participant.avatarPhoto = avatars[0].photo
        participant.avatarContentType = avatars[0].contentType || 'image/png'
      }
    } catch (err) {
      console.warn(`Failed to load avatar for user ${userId}:`, err)
    }
  }

  function handlePlayerLeft(data) {
    participants.value.delete(data.userId)
  }

  function handlePlayerReady(data) {
    // Обновляем статус готовности участника
    if (participants.value.has(data.idUser)) {
      const participant = participants.value.get(data.idUser)
      participant.isReady = true
    }

    // Обновляем статус готовности текущего пользователя
    if (data.idUser === currentUserId.value) {
      currentUserReady.value = true
    }
  }

  function handlePlayerFinished(data) {
    if (participants.value.has(data.idUser)) {
      const participant = participants.value.get(data.idUser)
      participant.isFinished = true
      participant.progress = 100
      participant.place = data.place
      participant.speed = data.speed
      participant.accuracy = data.accuracy
      participant.duration = data.durationSeconds
    }
  }

  function handleCountdown(data) {
    status.value = 'WAITING' // Обратный отсчёт = WAITING на бэкенде
    countdownSeconds.value = data.seconds
  }

  function handleStart(data) {
    status.value = 'PROGRESS' // Гонка = PROGRESS на бэкенде
    exerciseText.value = data.text
    startTimestamp.value = data.startTimestamp
    startTimer()
  }

  function handleFinished(data) {
    status.value = 'FINISHED'
    stopTimer()
    leaderboard.value = data.leaderboard || []

    // Найти место текущего пользователя
    const myEntry = leaderboard.value.find((e) => e.idUser === currentUserId.value)
    if (myEntry) {
      myPlace.value = myEntry.place
    }
  }

  function handleDisconnect() {
    isConnected.value = false
  }

  // === Timer ===

  function startTimer() {
    if (timerInterval) return
    timerInterval = setInterval(() => {
      elapsedSeconds.value++
    }, 1000)
  }

  function stopTimer() {
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }

  // === Typing Actions ===

  /**
   * Обработать нажатие клавиши
   */
  function processKey(key) {
    if (status.value !== 'PROGRESS' || !exerciseText.value) {
      return { correct: false, finished: false }
    }

    const expectedChar = exerciseText.value[currentIndex.value]
    const isCorrect = key === expectedChar

    if (isCorrect) {
      typedCorrectly.value++
      currentIndex.value++

      // Отправляем прогресс (каждые 5%)
      const newProgress = progress.value
      if (newProgress % 5 === 0 || newProgress === 100) {
        contestWebSocket.sendProgress({
          progress: newProgress,
          speed: speed.value,
          accuracy: accuracy.value,
        })
      }

      // Проверяем завершение
      if (currentIndex.value >= exerciseText.value.length) {
        finishRace()
        return { correct: true, finished: true }
      }

      return { correct: true, finished: false }
    } else {
      if (!errorPositions.value.has(currentIndex.value)) {
        errors.value++
        errorPositions.value.add(currentIndex.value)
      }
      return { correct: false, finished: false }
    }
  }

  /**
   * Завершить гонку
   */
  function finishRace() {
    stopTimer()

    contestWebSocket.sendFinish({
      speed: speed.value,
      accuracy: accuracy.value,
      durationSeconds: elapsedSeconds.value,
    })

    // Отмечаем себя как финишировавшего
    currentUserFinished.value = true

    if (participants.value.has(currentUserId.value)) {
      const me = participants.value.get(currentUserId.value)
      me.isFinished = true
      me.progress = 100
      me.speed = speed.value
      me.accuracy = accuracy.value
      me.duration = elapsedSeconds.value
    }
  }

  /**
   * Отметить готовность
   */
  function setReady() {
    if (!isConnected.value || status.value !== 'CREATED') return

    contestWebSocket.sendReady()
    currentUserReady.value = true

    if (participants.value.has(currentUserId.value)) {
      participants.value.get(currentUserId.value).isReady = true
    }
  }

  /**
   * Покинуть комнату
   */
  async function leaveRoom() {
    try {
      if (contestId.value) {
        await multiplayerAPI.leaveRoom(contestId.value)
      }
    } catch (e) {
      console.error('Error leaving room:', e)
    } finally {
      disconnect()
    }
  }

  /**
   * Отключиться от WebSocket и сбросить состояние
   */
  function disconnect() {
    contestWebSocket.disconnect()
    resetState()
  }

  /**
   * Сбросить состояние
   */
  function resetState() {
    stopTimer()

    contestId.value = null
    exerciseId.value = null
    exerciseTitle.value = ''
    exerciseText.value = ''
    exerciseLanguage.value = 'RU'
    maxPlayers.value = 2
    status.value = 'CREATED'
    countdownSeconds.value = 0
    startTimestamp.value = null
    participants.value.clear()
    currentUserId.value = null
    currentUserReady.value = false
    currentUserFinished.value = false
    currentIndex.value = 0
    typedCorrectly.value = 0
    errors.value = 0
    errorPositions.value.clear()
    elapsedSeconds.value = 0
    loading.value = false
    error.value = null
    isConnected.value = false
    leaderboard.value = []
    myPlace.value = null
  }

  return {
    // State
    contestId,
    exerciseId,
    exerciseTitle,
    exerciseText,
    exerciseLanguage,
    maxPlayers,
    status,
    countdownSeconds,
    participants,
    currentUserId,
    currentUserReady,
    currentUserFinished,
    currentIndex,
    typedCorrectly,
    errors,
    elapsedSeconds,
    loading,
    error,
    isConnected,
    leaderboard,
    myPlace,

    // Getters
    currentChar,
    typedText,
    remainingText,
    progress,
    accuracy,
    speed,
    formattedTime,
    participantsList,
    readyCount,
    totalParticipants,
    isWaiting,
    isCountdown,
    isInProgress,
    isFinished,
    canStart,

    // Actions
    joinRoom,
    createAndJoinRoom,
    connectWebSocket,
    processKey,
    setReady,
    leaveRoom,
    disconnect,
    resetState,
  }
})
