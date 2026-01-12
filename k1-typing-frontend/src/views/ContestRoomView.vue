<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useContestStore } from '@/stores/contest'
import { useAuthStore } from '@/stores/auth'
import VirtualKeyboard from '@/components/VirtualKeyboard.vue'
import HandDiagram from '@/components/HandDiagram.vue'
import PlayerProgressBar from '@/components/PlayerProgressBar.vue'

const route = useRoute()
const router = useRouter()
const contestStore = useContestStore()
const authStore = useAuthStore()

// Refs
const hasError = ref(false)
const showEarlyFinishModal = ref(false)

// Следим за финишем текущего пользователя (но гонка ещё идёт)
watch(
  () => contestStore.currentUserFinished && contestStore.isInProgress,
  (finishedEarly) => {
    if (finishedEarly) {
      showEarlyFinishModal.value = true
    }
  }
)

// Следим за завершением всей гонки — закрываем мини-окно
watch(() => contestStore.isFinished, (finished) => {
  if (finished) {
    showEarlyFinishModal.value = false
  }
})

// Подключение к комнате
onMounted(async () => {
  const contestId = Number(route.params.id)
  if (!contestId) {
    router.push('/')
    return
  }

  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  const result = await contestStore.joinRoom(contestId, authStore.userId)
  if (!result.success) {
    console.error('Failed to join room:', result.error)
  }

  document.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  contestStore.leaveRoom()
})

// Обработчик нажатия клавиши
function handleKeyDown(event) {
  // Игнорируем специальные клавиши
  if (event.ctrlKey || event.altKey || event.metaKey) return
  if (event.key === 'Tab' || event.key === 'Escape') return

  // Только во время гонки
  if (!contestStore.isInProgress) return

  event.preventDefault()

  // Обрабатываем только печатные символы
  if (event.key.length === 1 || event.key === ' ') {
    const { correct } = contestStore.processKey(event.key)

    if (!correct) {
      hasError.value = true
      setTimeout(() => {
        hasError.value = false
      }, 200)
    }
  }
}

// Определяем палец для клавиши
const isUpperCase = computed(() => {
  const char = contestStore.currentChar
  if (!char || char.length !== 1) return false
  return char !== char.toLowerCase() && char === char.toUpperCase()
})

const activeFinger = computed(() => {
  const char = contestStore.currentChar?.toLowerCase()
  if (!char) return null
  const layout = contestStore.exerciseLanguage === 'ENG' ? 'ENG' : 'RU'
  return getFingerForChar(char, layout)
})

const activeShiftSide = computed(() => {
  if (!isUpperCase.value || !activeFinger.value) return null
  if (activeFinger.value.endsWith('Left')) {
    return 'right'
  } else if (activeFinger.value.endsWith('Right')) {
    return 'left'
  }
  return null
})

function getFingerForChar(char, layout) {
  const fingerMaps = {
    RU: {
      ё: 'pinkyLeft', й: 'pinkyLeft', ф: 'pinkyLeft', я: 'pinkyLeft', '1': 'pinkyLeft',
      ц: 'ringLeft', ы: 'ringLeft', ч: 'ringLeft', '2': 'ringLeft',
      у: 'middleLeft', в: 'middleLeft', с: 'middleLeft', '3': 'middleLeft',
      к: 'indexLeft', а: 'indexLeft', м: 'indexLeft', е: 'indexLeft', п: 'indexLeft', и: 'indexLeft', '4': 'indexLeft', '5': 'indexLeft',
      н: 'indexRight', р: 'indexRight', т: 'indexRight', г: 'indexRight', о: 'indexRight', ь: 'indexRight', '6': 'indexRight', '7': 'indexRight',
      ш: 'middleRight', л: 'middleRight', б: 'middleRight', '8': 'middleRight',
      щ: 'ringRight', д: 'ringRight', ю: 'ringRight', '9': 'ringRight',
      з: 'pinkyRight', ж: 'pinkyRight', '.': 'pinkyRight', х: 'pinkyRight', э: 'pinkyRight', ъ: 'pinkyRight', '0': 'pinkyRight', '-': 'pinkyRight', '=': 'pinkyRight',
      ' ': 'thumbs',
    },
    ENG: {
      '`': 'pinkyLeft', q: 'pinkyLeft', a: 'pinkyLeft', z: 'pinkyLeft', '1': 'pinkyLeft',
      w: 'ringLeft', s: 'ringLeft', x: 'ringLeft', '2': 'ringLeft',
      e: 'middleLeft', d: 'middleLeft', c: 'middleLeft', '3': 'middleLeft',
      r: 'indexLeft', f: 'indexLeft', v: 'indexLeft', t: 'indexLeft', g: 'indexLeft', b: 'indexLeft', '4': 'indexLeft', '5': 'indexLeft',
      y: 'indexRight', h: 'indexRight', n: 'indexRight', u: 'indexRight', j: 'indexRight', m: 'indexRight', '6': 'indexRight', '7': 'indexRight',
      i: 'middleRight', k: 'middleRight', ',': 'middleRight', '8': 'middleRight',
      o: 'ringRight', l: 'ringRight', '.': 'ringRight', '9': 'ringRight',
      p: 'pinkyRight', ';': 'pinkyRight', '/': 'pinkyRight', '[': 'pinkyRight', "'": 'pinkyRight', ']': 'pinkyRight', '0': 'pinkyRight', '-': 'pinkyRight', '=': 'pinkyRight',
      ' ': 'thumbs',
    },
  }
  return fingerMaps[layout]?.[char] || null
}

// Готовность к старту
function markReady() {
  contestStore.setReady()
}

// Выход из комнаты
function leaveRoom() {
  contestStore.leaveRoom()
  router.push('/')
}

// Вернуться на главную в раздел соревнований
function goToContests() {
  contestStore.leaveRoom()
  router.push({ path: '/', query: { tab: 'contests' } })
}

// Вернуться на главную после завершения
function goHome() {
  router.push({ path: '/', query: { tab: 'contests' } })
}

// Плейсхолдер для пустых слотов
const emptySlots = computed(() => {
  const filled = contestStore.participantsList.length
  const max = Math.max(contestStore.maxPlayers, 15)
  return Math.max(0, max - filled)
})

// Сортировка участников: текущий пользователь сверху, затем по прогрессу
const sortedParticipants = computed(() => {
  return [...contestStore.participantsList].sort((a, b) => {
    // Текущий пользователь всегда первый
    if (a.id === contestStore.currentUserId) return -1
    if (b.id === contestStore.currentUserId) return 1
    // Затем по прогрессу (убывание)
    return b.progress - a.progress
  })
})

// Метки мест для результатов
const placeEmoji = {
  FIRST: '🥇',
  SECOND: '🥈',
  THIRD: '🥉',
  WITHOUT_PLACE: '',
}
</script>

<template>
  <div class="contest-page bg-gradient">
    <!-- Хедер -->
    <header class="contest-header">
      <button class="back-btn" @click="leaveRoom">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6" />
        </svg>
        <span>Выйти</span>
      </button>

      <div class="header-center">
        <div class="logo">
          <span class="logo-icon">🏆</span>
          <span class="logo-text">Соревнование</span>
        </div>
        <div v-if="contestStore.exerciseTitle" class="exercise-title">
          {{ contestStore.exerciseTitle }}
        </div>
      </div>

      <div class="header-stats">
        <div v-if="contestStore.isInProgress || contestStore.isFinished" class="stat-item">
          <span class="stat-label">Время</span>
          <span class="stat-value">{{ contestStore.formattedTime }}</span>
        </div>
        <div v-if="contestStore.isInProgress || contestStore.isFinished" class="stat-item">
          <span class="stat-label">Скорость</span>
          <span class="stat-value">{{ contestStore.speed }} <small>сим/мин</small></span>
        </div>
        <div v-if="contestStore.isInProgress || contestStore.isFinished" class="stat-item">
          <span class="stat-label">Точность</span>
          <span class="stat-value">{{ contestStore.accuracy.toFixed(1) }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Игроки</span>
          <span class="stat-value">{{ contestStore.totalParticipants }}/{{ contestStore.maxPlayers }}</span>
        </div>
      </div>
    </header>

    <main class="contest-main">
      <!-- Состояние загрузки -->
      <div v-if="contestStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Подключение к комнате...</p>
      </div>

      <!-- Ошибка -->
      <div v-else-if="contestStore.error" class="error-state">
        <div class="error-icon">⚠</div>
        <p>{{ contestStore.error }}</p>
        <button class="btn btn-primary" @click="goHome">Вернуться на главную</button>
      </div>

      <!-- Лобби (ожидание игроков) -->
      <template v-else-if="contestStore.isWaiting">
        <div class="lobby-container">
          <div class="lobby-card">
            <h2 class="lobby-title">Ожидание игроков</h2>
            <p class="lobby-subtitle">
              Готово: {{ contestStore.readyCount }}/{{ contestStore.totalParticipants }}
            </p>

            <!-- Список участников -->
            <div class="participants-list">
              <PlayerProgressBar
                v-for="participant in sortedParticipants"
                :key="participant.id"
                :username="participant.username"
                :progress="0"
                :is-ready="participant.isReady"
                :is-current-user="participant.id === contestStore.currentUserId"
              />

              <!-- Пустые слоты -->
              <div v-for="i in Math.min(emptySlots, 5)" :key="'empty-' + i" class="empty-slot">
                <span class="empty-slot-text">Ожидание игрока...</span>
              </div>
            </div>

            <!-- Кнопка готовности -->
            <div class="lobby-actions">
              <button
                v-if="!contestStore.currentUserReady"
                class="btn btn-primary btn-ready"
                @click="markReady"
              >
                Я готов!
              </button>
              <div v-else class="ready-status">
                <span class="ready-check">✓</span>
                <span>Вы готовы. Ожидание остальных...</span>
              </div>
            </div>

            <p class="lobby-hint">
              Минимум 2 игрока должны быть готовы для старта
            </p>
          </div>
        </div>
      </template>

      <!-- Обратный отсчёт -->
      <template v-else-if="contestStore.isCountdown">
        <div class="countdown-container">
          <div class="countdown-card">
            <p class="countdown-label">Гонка начнётся через</p>
            <div class="countdown-number">{{ contestStore.countdownSeconds }}</div>
            <p class="countdown-hint">Приготовьтесь печатать!</p>
          </div>
        </div>
      </template>

      <!-- Гонка (набор текста) -->
      <template v-else-if="contestStore.isInProgress">
        <div class="race-container">
          <!-- Прогресс-бары участников -->
          <div class="players-panel">
            <h3 class="panel-title">Участники</h3>
            <div class="players-list">
              <PlayerProgressBar
                v-for="participant in sortedParticipants"
                :key="participant.id"
                :username="participant.username"
                :progress="participant.progress"
                :is-finished="participant.isFinished"
                :place="participant.place"
                :speed="participant.speed"
                :accuracy="participant.accuracy"
                :is-current-user="participant.id === contestStore.currentUserId"
                :show-stats="true"
              />
            </div>
          </div>

          <!-- Область набора текста -->
          <div class="typing-section">
            <!-- Прогресс -->
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: contestStore.progress + '%' }"></div>
            </div>

            <!-- Область ввода текста -->
            <div class="typing-area" :class="{ error: hasError }">
              <div class="typing-mask typing-mask-left"></div>
              <div class="typing-mask typing-mask-right"></div>

              <div class="typing-track">
                <div
                  class="typing-content"
                  :style="{ transform: `translateX(calc(-${contestStore.currentIndex}ch - 0.5ch))` }"
                >
                  <span class="typed-text">
                    <template v-for="(char, index) in contestStore.typedText" :key="'typed-' + index">
                      <span v-if="char === ' '" class="space-indicator-typed">·</span>
                      <template v-else>{{ char }}</template>
                    </template>
                  </span>

                  <span class="current-char" :class="{ space: contestStore.currentChar === ' ' }">
                    {{ contestStore.currentChar === ' ' ? '␣' : contestStore.currentChar }}
                  </span>

                  <span class="remaining-text">
                    <template v-for="(char, index) in contestStore.remainingText" :key="index">
                      <span v-if="char === ' '" class="space-indicator">·</span>
                      <template v-else>{{ char }}</template>
                    </template>
                  </span>
                </div>
              </div>
            </div>

            <!-- Визуальные подсказки -->
            <div class="visual-hints">
              <HandDiagram :active-finger="activeFinger" :shift-side="activeShiftSide" />
              <VirtualKeyboard :active-key="contestStore.currentChar" :language="contestStore.exerciseLanguage" />
            </div>
          </div>

          <!-- Мини-окно результатов (ранний финиш) -->
          <Teleport to="body">
            <div v-if="showEarlyFinishModal" class="early-finish-overlay">
              <div class="early-finish-modal">
                <div class="early-finish-header">
                  <div class="early-finish-icon">🎉</div>
                  <h3 class="early-finish-title">Вы финишировали!</h3>
                </div>

                <!-- Моё место -->
                <div v-if="contestStore.myPlace" class="early-finish-place">
                  <span class="early-place-emoji">{{ placeEmoji[contestStore.myPlace] }}</span>
                  <span class="early-place-text">
                    {{ contestStore.myPlace === 'FIRST' ? '1 место' :
                       contestStore.myPlace === 'SECOND' ? '2 место' :
                       contestStore.myPlace === 'THIRD' ? '3 место' : 'Участие засчитано' }}
                  </span>
                </div>

                <!-- Статистика -->
                <div class="early-finish-stats">
                  <div class="early-stat">
                    <span class="early-stat-value">{{ contestStore.formattedTime }}</span>
                    <span class="early-stat-label">Время</span>
                  </div>
                  <div class="early-stat">
                    <span class="early-stat-value">{{ contestStore.speed }}</span>
                    <span class="early-stat-label">Сим/мин</span>
                  </div>
                  <div class="early-stat">
                    <span class="early-stat-value">{{ contestStore.accuracy.toFixed(1) }}%</span>
                    <span class="early-stat-label">Точность</span>
                  </div>
                </div>

                <p class="early-finish-hint">Ожидание остальных участников...</p>

                <div class="early-finish-actions">
                  <button class="btn btn-secondary" @click="showEarlyFinishModal = false">
                    Смотреть гонку
                  </button>
                  <button class="btn btn-primary" @click="goToContests">
                    К соревнованиям
                  </button>
                </div>
              </div>
            </div>
          </Teleport>
        </div>
      </template>

      <!-- Результаты -->
      <template v-else-if="contestStore.isFinished">
        <div class="results-container">
          <div class="results-card">
            <div class="results-header">
              <div class="results-icon">🏁</div>
              <h2 class="results-title">Гонка завершена!</h2>
            </div>

            <!-- Мое место -->
            <div v-if="contestStore.myPlace" class="my-result">
              <span class="my-place-emoji">{{ placeEmoji[contestStore.myPlace] }}</span>
              <span class="my-place-text">
                {{ contestStore.myPlace === 'FIRST' ? 'Вы победили!' :
                   contestStore.myPlace === 'SECOND' ? 'Второе место!' :
                   contestStore.myPlace === 'THIRD' ? 'Третье место!' :
                   'Участие засчитано' }}
              </span>
            </div>

            <!-- Мои статы -->
            <div class="my-stats">
              <div class="my-stat">
                <span class="my-stat-value">{{ contestStore.formattedTime }}</span>
                <span class="my-stat-label">Время</span>
              </div>
              <div class="my-stat">
                <span class="my-stat-value">{{ contestStore.speed }}</span>
                <span class="my-stat-label">Символов/мин</span>
              </div>
              <div class="my-stat">
                <span class="my-stat-value">{{ contestStore.accuracy.toFixed(1) }}%</span>
                <span class="my-stat-label">Точность</span>
              </div>
            </div>

            <!-- Таблица лидеров -->
            <div class="leaderboard">
              <h3 class="leaderboard-title">Таблица результатов</h3>
              <div class="leaderboard-list">
                <div
                  v-for="(entry, idx) in contestStore.leaderboard"
                  :key="entry.idUser"
                  class="leaderboard-entry"
                  :class="{
                    'current-user': entry.idUser === contestStore.currentUserId,
                    first: entry.place === 'FIRST',
                    second: entry.place === 'SECOND',
                    third: entry.place === 'THIRD',
                  }"
                >
                  <div class="entry-place">
                    <span class="place-number">{{ idx + 1 }}</span>
                    <span v-if="placeEmoji[entry.place]" class="place-emoji">{{ placeEmoji[entry.place] }}</span>
                  </div>
                  <div class="entry-name">{{ entry.username }}</div>
                  <div class="entry-stats">
                    <span>{{ entry.speed }} сим/мин</span>
                    <span class="entry-accuracy">{{ entry.accuracy.toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="results-actions">
              <button class="btn btn-primary" @click="goHome">
                К соревнованиям
              </button>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.contest-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Хедер */
.contest-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(74, 55, 40, 0.15);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn svg {
  width: 18px;
  height: 18px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.5);
}

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 22px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.exercise-title {
  font-size: 13px;
  color: var(--text-secondary);
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-label {
  font-size: 10px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-value small {
  font-size: 10px;
  font-weight: 500;
  opacity: 0.7;
}

/* Main */
.contest-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(74, 55, 40, 0.1);
  border-top-color: var(--coral);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Error */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state p {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0 0 24px;
}

/* Lobby */
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.lobby-card {
  max-width: 500px;
  width: 100%;
  padding: 40px;
  background: rgba(74, 55, 40, 0.85);
  border-radius: 24px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 16px 48px rgba(74, 55, 40, 0.4);
}

.lobby-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  text-align: center;
  margin: 0 0 8px;
}

.lobby-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
  margin: 0 0 24px;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
  max-height: 320px;
  overflow-y: auto;
}

.empty-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px dashed rgba(255, 255, 255, 0.15);
  border-radius: 10px;
}

.empty-slot-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.lobby-actions {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.btn-ready {
  padding: 16px 48px;
  font-size: 18px;
}

.ready-status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: rgba(74, 222, 128, 0.15);
  border: 1px solid rgba(74, 222, 128, 0.3);
  border-radius: 10px;
  font-size: 15px;
  color: #4ade80;
}

.ready-check {
  font-size: 18px;
}

.lobby-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  margin: 0;
}

/* Countdown */
.countdown-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.countdown-card {
  text-align: center;
  padding: 60px;
  background: rgba(74, 55, 40, 0.9);
  border-radius: 32px;
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 60px rgba(74, 55, 40, 0.5);
}

.countdown-label {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 20px;
}

.countdown-number {
  font-size: 120px;
  font-weight: 800;
  color: var(--lemon);
  line-height: 1;
  text-shadow: 0 0 40px rgba(237, 229, 116, 0.5);
  animation: pulse-countdown 1s ease infinite;
}

@keyframes pulse-countdown {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.countdown-hint {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin: 24px 0 0;
}

/* Race */
.race-container {
  display: flex;
  gap: 24px;
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.players-panel {
  width: 320px;
  flex-shrink: 0;
  background: rgba(74, 55, 40, 0.85);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  max-height: calc(100vh - 180px);
  overflow-y: auto;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.players-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.typing-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* Progress bar */
.progress-bar {
  width: 100%;
  max-width: 900px;
  height: 6px;
  background: rgba(74, 55, 40, 0.15);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--coral) 0%, var(--lemon) 100%);
  border-radius: 3px;
  transition: width 0.2s ease;
}

/* Typing area */
.typing-area {
  position: relative;
  width: 100%;
  max-width: 900px;
  height: 80px;
  background: rgba(74, 55, 40, 0.85);
  border-radius: 20px;
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(74, 55, 40, 0.3);
  overflow: hidden;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.typing-area.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.2);
  animation: shake 0.2s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.typing-mask {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 80px;
  z-index: 10;
  pointer-events: none;
}

.typing-mask-left {
  left: 0;
  background: linear-gradient(90deg, rgba(74, 55, 40, 1) 0%, rgba(74, 55, 40, 0.8) 40%, rgba(74, 55, 40, 0) 100%);
}

.typing-mask-right {
  right: 0;
  background: linear-gradient(270deg, rgba(74, 55, 40, 1) 0%, rgba(74, 55, 40, 0.8) 40%, rgba(74, 55, 40, 0) 100%);
}

.typing-track {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.typing-content {
  position: absolute;
  left: 50%;
  display: flex;
  align-items: center;
  font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', 'Consolas', monospace;
  font-size: 32px;
  line-height: 1;
  white-space: nowrap;
  transition: transform 0.1s cubic-bezier(0.22, 0.61, 0.36, 1);
  will-change: transform;
}

.typed-text {
  color: rgba(255, 255, 255, 0.18);
}

.space-indicator-typed {
  color: rgba(255, 255, 255, 0.12);
}

.current-char {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  min-width: 1ch;
  height: 48px;
  padding: 0 10px;
  margin: 0;
  border-radius: 10px;
  font-weight: 700;
  box-shadow: 0 0 24px rgba(255, 160, 122, 0.7), 0 0 48px rgba(255, 160, 122, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.current-char.space {
  min-width: 1.5ch;
  font-size: 20px;
}

.remaining-text {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 500;
}

.space-indicator {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 400;
}

.visual-hints {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 900px;
}

/* Results */
.results-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.results-card {
  max-width: 600px;
  width: 100%;
  padding: 40px;
  background: rgba(74, 55, 40, 0.9);
  border-radius: 24px;
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 60px rgba(74, 55, 40, 0.5);
}

.results-header {
  text-align: center;
  margin-bottom: 24px;
}

.results-icon {
  font-size: 56px;
  margin-bottom: 12px;
}

.results-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}

.my-result {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(135deg, rgba(255, 160, 122, 0.2) 0%, rgba(237, 229, 116, 0.2) 100%);
  border: 1px solid rgba(255, 160, 122, 0.3);
  border-radius: 16px;
  margin-bottom: 24px;
}

.my-place-emoji {
  font-size: 32px;
}

.my-place-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--lemon);
}

.my-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.my-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
}

.my-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--lemon);
}

.my-stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.leaderboard {
  margin-bottom: 24px;
}

.leaderboard-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leaderboard-entry {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  transition: background 0.2s ease;
}

.leaderboard-entry.current-user {
  background: rgba(255, 160, 122, 0.15);
  border: 1px solid rgba(255, 160, 122, 0.3);
}

.leaderboard-entry.first {
  background: rgba(255, 215, 0, 0.1);
}

.leaderboard-entry.second {
  background: rgba(192, 192, 192, 0.1);
}

.leaderboard-entry.third {
  background: rgba(205, 127, 50, 0.1);
}

.entry-place {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 50px;
}

.place-number {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.place-emoji {
  font-size: 18px;
}

.entry-name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.entry-stats {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.entry-accuracy {
  color: var(--lemon);
}

.results-actions {
  display: flex;
  justify-content: center;
}

.results-actions .btn {
  padding: 14px 40px;
}

/* Responsive */
@media (max-width: 1024px) {
  .race-container {
    flex-direction: column;
  }

  .players-panel {
    width: 100%;
    max-height: 200px;
  }

  .players-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .contest-header {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px 16px;
  }

  .back-btn span {
    display: none;
  }

  .header-center {
    order: 1;
    width: 100%;
  }

  .header-stats {
    order: 2;
    width: 100%;
    justify-content: space-around;
    gap: 8px;
  }

  .contest-main {
    padding: 16px;
  }

  .lobby-card,
  .results-card {
    padding: 24px;
  }

  .typing-area {
    height: 60px;
    border-radius: 16px;
  }

  .typing-content {
    font-size: 22px;
  }

  .typing-mask {
    width: 50px;
  }

  .current-char {
    height: 38px;
    padding: 0 8px;
    border-radius: 8px;
  }

  .my-stats {
    grid-template-columns: 1fr;
  }

  .countdown-number {
    font-size: 80px;
  }
}

/* Early Finish Modal */
.early-finish-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.early-finish-modal {
  background: rgba(74, 55, 40, 0.95);
  border-radius: 24px;
  padding: 32px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.early-finish-header {
  margin-bottom: 20px;
}

.early-finish-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.early-finish-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}

.early-finish-place {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(135deg, rgba(255, 160, 122, 0.2) 0%, rgba(237, 229, 116, 0.2) 100%);
  border: 1px solid rgba(255, 160, 122, 0.3);
  border-radius: 14px;
  margin-bottom: 20px;
}

.early-place-emoji {
  font-size: 28px;
}

.early-place-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--lemon);
}

.early-finish-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.early-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 10px;
}

.early-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--lemon);
}

.early-stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.early-finish-hint {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 20px;
}

.early-finish-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.early-finish-actions .btn {
  padding: 12px 24px;
  font-size: 14px;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.25);
}

@media (max-width: 480px) {
  .early-finish-modal {
    padding: 24px;
  }

  .early-finish-stats {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .early-finish-actions {
    flex-direction: column;
  }
}
</style>
