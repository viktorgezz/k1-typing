<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTypingStore } from '@/stores/typing'
import VirtualKeyboard from '@/components/VirtualKeyboard.vue'
import HandDiagram from '@/components/HandDiagram.vue'

const route = useRoute()
const router = useRouter()
const typingStore = useTypingStore()

// Refs
const typingAreaRef = ref(null)
const hasError = ref(false)

// Загрузка упражнения
onMounted(async () => {
  const exerciseId = Number(route.params.id)
  if (!exerciseId) {
    router.push('/')
    return
  }

  const result = await typingStore.initSession(exerciseId)
  if (!result.success) {
    console.error('Failed to load exercise:', result.error)
  }

  // Фокус на область ввода
  document.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  typingStore.resetState()
})

// Обработчик нажатия клавиши
function handleKeyDown(event) {
  // Игнорируем специальные клавиши
  if (event.ctrlKey || event.altKey || event.metaKey) return
  if (event.key === 'Tab' || event.key === 'Escape') return

  // Игнорируем, если упражнение завершено
  if (typingStore.isFinished) return

  event.preventDefault()

  // Обрабатываем только печатные символы
  if (event.key.length === 1 || event.key === ' ') {
    const { correct } = typingStore.processKey(event.key)

    // Визуальная обратная связь при ошибке
    if (!correct) {
      hasError.value = true
      setTimeout(() => {
        hasError.value = false
      }, 200)
    }
  }
}

// Проверяем, заглавная ли буква
const isUpperCase = computed(() => {
  const char = typingStore.currentChar
  if (!char || char.length !== 1) return false
  return char !== char.toLowerCase() && char === char.toUpperCase()
})

// Информация об активном пальце для виртуальной клавиатуры
const activeFinger = computed(() => {
  const char = typingStore.currentChar?.toLowerCase()
  if (!char) return null

  // Получаем информацию о пальце из раскладки
  const layout = typingStore.exerciseLanguage === 'ENG' ? 'ENG' : 'RU'
  return getFingerForChar(char, layout)
})

// Определяем, какую сторону Shift нужно нажать (противоположную руке с буквой)
const activeShiftSide = computed(() => {
  if (!isUpperCase.value || !activeFinger.value) return null
  
  // Если буква нажимается левой рукой — используем правый Shift и наоборот
  if (activeFinger.value.endsWith('Left')) {
    return 'right'
  } else if (activeFinger.value.endsWith('Right')) {
    return 'left'
  }
  return null
})

// Получить палец для символа
function getFingerForChar(char, layout) {
  const fingerMaps = {
    RU: {
      // Мизинец левый
      ё: 'pinkyLeft', й: 'pinkyLeft', ф: 'pinkyLeft', я: 'pinkyLeft', '1': 'pinkyLeft',
      // Безымянный левый
      ц: 'ringLeft', ы: 'ringLeft', ч: 'ringLeft', '2': 'ringLeft',
      // Средний левый
      у: 'middleLeft', в: 'middleLeft', с: 'middleLeft', '3': 'middleLeft',
      // Указательный левый
      к: 'indexLeft', а: 'indexLeft', м: 'indexLeft', е: 'indexLeft', п: 'indexLeft', и: 'indexLeft', '4': 'indexLeft', '5': 'indexLeft',
      // Указательный правый
      н: 'indexRight', р: 'indexRight', т: 'indexRight', г: 'indexRight', о: 'indexRight', ь: 'indexRight', '6': 'indexRight', '7': 'indexRight',
      // Средний правый
      ш: 'middleRight', л: 'middleRight', б: 'middleRight', '8': 'middleRight',
      // Безымянный правый
      щ: 'ringRight', д: 'ringRight', ю: 'ringRight', '9': 'ringRight',
      // Мизинец правый
      з: 'pinkyRight', ж: 'pinkyRight', '.': 'pinkyRight', х: 'pinkyRight', э: 'pinkyRight', ъ: 'pinkyRight', '0': 'pinkyRight', '-': 'pinkyRight', '=': 'pinkyRight',
      // Пробел
      ' ': 'thumbs',
    },
    ENG: {
      // Мизинец левый
      '`': 'pinkyLeft', q: 'pinkyLeft', a: 'pinkyLeft', z: 'pinkyLeft', '1': 'pinkyLeft',
      // Безымянный левый
      w: 'ringLeft', s: 'ringLeft', x: 'ringLeft', '2': 'ringLeft',
      // Средний левый
      e: 'middleLeft', d: 'middleLeft', c: 'middleLeft', '3': 'middleLeft',
      // Указательный левый
      r: 'indexLeft', f: 'indexLeft', v: 'indexLeft', t: 'indexLeft', g: 'indexLeft', b: 'indexLeft', '4': 'indexLeft', '5': 'indexLeft',
      // Указательный правый
      y: 'indexRight', h: 'indexRight', n: 'indexRight', u: 'indexRight', j: 'indexRight', m: 'indexRight', '6': 'indexRight', '7': 'indexRight',
      // Средний правый
      i: 'middleRight', k: 'middleRight', ',': 'middleRight', '8': 'middleRight',
      // Безымянный правый
      o: 'ringRight', l: 'ringRight', '.': 'ringRight', '9': 'ringRight',
      // Мизинец правый
      p: 'pinkyRight', ';': 'pinkyRight', '/': 'pinkyRight', '[': 'pinkyRight', "'": 'pinkyRight', ']': 'pinkyRight', '0': 'pinkyRight', '-': 'pinkyRight', '=': 'pinkyRight',
      // Пробел
      ' ': 'thumbs',
    },
  }

  return fingerMaps[layout]?.[char] || null
}

// Возврат на главную
function goHome() {
  router.push('/')
}

// Перезапуск упражнения
async function restartExercise() {
  await typingStore.restart()
}
</script>

<template>
  <div class="exercise-page bg-gradient">
    <!-- Хедер -->
    <header class="exercise-header">
      <button class="back-btn" @click="goHome">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <polyline points="15 18 9 12 15 6" />
        </svg>
        <span>Назад</span>
      </button>

      <div class="logo">
        <span class="logo-icon">⌨</span>
        <span class="logo-text">k1typing</span>
      </div>

      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-label">Время</span>
          <span class="stat-value">{{ typingStore.formattedTime }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Скорость</span>
          <span class="stat-value">{{ typingStore.speed }} <small>сим/мин</small></span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Точность</span>
          <span class="stat-value">{{ typingStore.accuracy }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Ошибки</span>
          <span class="stat-value error-count">{{ typingStore.errors }}</span>
        </div>
      </div>
    </header>

    <!-- Основной контент -->
    <main class="exercise-main">
      <!-- Состояние загрузки -->
      <div v-if="typingStore.loading && !typingStore.exerciseText" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Загрузка упражнения...</p>
      </div>

      <!-- Ошибка -->
      <div v-else-if="typingStore.error && !typingStore.exerciseText" class="error-state">
        <div class="error-icon">⚠</div>
        <p>{{ typingStore.error }}</p>
        <button class="btn btn-primary" @click="goHome">Вернуться на главную</button>
      </div>

      <!-- Экран завершения -->
      <div v-else-if="typingStore.isFinished" class="completion-screen">
        <div class="completion-card">
          <div class="completion-icon">🎉</div>
          <h2 class="completion-title">Упражнение завершено!</h2>

          <div class="completion-stats">
            <div class="completion-stat">
              <span class="completion-stat-value">{{ typingStore.formattedTime }}</span>
              <span class="completion-stat-label">Время</span>
            </div>
            <div class="completion-stat">
              <span class="completion-stat-value">{{ typingStore.speed }}</span>
              <span class="completion-stat-label">Символов/мин</span>
            </div>
            <div class="completion-stat">
              <span class="completion-stat-value">{{ typingStore.accuracy }}%</span>
              <span class="completion-stat-label">Точность</span>
            </div>
            <div class="completion-stat">
              <span class="completion-stat-value">{{ typingStore.errors }}</span>
              <span class="completion-stat-label">Ошибок</span>
            </div>
          </div>

          <div v-if="typingStore.result?.place" class="completion-place">
            Ваше место: <strong>{{ typingStore.result.place }}</strong>
          </div>

          <div class="completion-actions">
            <button class="btn btn-primary" @click="restartExercise">
              Повторить
            </button>
            <button class="btn btn-secondary" @click="goHome">
              К упражнениям
            </button>
          </div>
        </div>
      </div>

      <!-- Основной интерфейс набора -->
      <template v-else>
        <!-- Прогресс -->
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: typingStore.progress + '%' }"></div>
        </div>

        <!-- Область ввода текста (бесконечная лента) -->
        <div
          ref="typingAreaRef"
          class="typing-area"
          :class="{ error: hasError }"
        >
          <!-- Маска для затухания краёв -->
          <div class="typing-mask typing-mask-left"></div>
          <div class="typing-mask typing-mask-right"></div>
          
          <div class="typing-track">
            <div 
              class="typing-content"
              :style="{ transform: `translateX(calc(-${typingStore.currentIndex}ch - 0.5ch))` }"
            >
              <!-- Набранный текст (пробелы показаны как точки) -->
              <span class="typed-text">
                <template v-for="(char, index) in typingStore.typedText" :key="'typed-' + index">
                  <span v-if="char === ' '" class="space-indicator-typed">·</span>
                  <template v-else>{{ char }}</template>
                </template>
              </span>

              <!-- Текущий символ (всегда в центре) -->
              <span class="current-char" :class="{ space: typingStore.currentChar === ' ' }">
                {{ typingStore.currentChar === ' ' ? '␣' : typingStore.currentChar }}
              </span>

              <!-- Оставшийся текст (пробелы показаны как точки) -->
              <span class="remaining-text">
                <template v-for="(char, index) in typingStore.remainingText" :key="index">
                  <span v-if="char === ' '" class="space-indicator">·</span>
                  <template v-else>{{ char }}</template>
                </template>
              </span>
            </div>
          </div>
        </div>

        <!-- Подсказка -->
        <p v-if="!typingStore.isStarted" class="typing-hint">
          Начните печатать для старта упражнения
        </p>

        <!-- Визуальные подсказки -->
        <div class="visual-hints">
          <!-- Схема рук -->
          <HandDiagram 
            :active-finger="activeFinger"
            :shift-side="activeShiftSide"
          />

          <!-- Виртуальная клавиатура -->
          <VirtualKeyboard
            :active-key="typingStore.currentChar"
            :language="typingStore.exerciseLanguage"
          />
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.exercise-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Хедер */
.exercise-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
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

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-value small {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.7;
}

.stat-value.error-count {
  color: #ef4444;
}

/* Основной контент */
.exercise-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px;
  gap: 32px;
}

/* Прогресс бар */
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

/* Область ввода — бесконечная лента */
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
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}

/* Маски для затухания на краях */
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
  background: linear-gradient(
    90deg,
    rgba(74, 55, 40, 1) 0%,
    rgba(74, 55, 40, 0.8) 40%,
    rgba(74, 55, 40, 0) 100%
  );
}

.typing-mask-right {
  right: 0;
  background: linear-gradient(
    270deg,
    rgba(74, 55, 40, 1) 0%,
    rgba(74, 55, 40, 0.8) 40%,
    rgba(74, 55, 40, 0) 100%
  );
}

/* Трек для текста */
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

/* Контент текста — движется влево при каждом нажатии */
.typing-content {
  position: absolute;
  left: 50%; /* Базовая позиция — центр контейнера */
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

/* Индикатор пробела в набранном тексте */
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
  box-shadow: 
    0 0 24px rgba(255, 160, 122, 0.7),
    0 0 48px rgba(255, 160, 122, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.current-char.space {
  min-width: 1.5ch;
  font-size: 20px;
}

.remaining-text {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 500;
}

/* Индикатор пробела в оставшемся тексте */
.space-indicator {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 400;
}

/* Подсказка */
.typing-hint {
  font-size: 16px;
  color: var(--text-secondary);
  text-align: center;
  animation: fadeInOut 2s ease infinite;
}

@keyframes fadeInOut {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

/* Визуальные подсказки */
.visual-hints {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 900px;
}

/* Состояние загрузки */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
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

/* Состояние ошибки */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
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

/* Экран завершения */
.completion-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  width: 100%;
}

.completion-card {
  max-width: 500px;
  width: 100%;
  padding: 48px;
  background: rgba(74, 55, 40, 0.85);
  border-radius: 24px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 16px 48px rgba(74, 55, 40, 0.4);
  text-align: center;
}

.completion-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.completion-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 32px;
}

.completion-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.completion-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
}

.completion-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--lemon);
}

.completion-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.completion-place {
  padding: 16px;
  background: linear-gradient(135deg, rgba(255, 160, 122, 0.2) 0%, rgba(237, 229, 116, 0.2) 100%);
  border: 1px solid rgba(255, 160, 122, 0.3);
  border-radius: 12px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 24px;
}

.completion-place strong {
  color: var(--lemon);
  font-weight: 700;
}

.completion-actions {
  display: flex;
  gap: 12px;
}

.completion-actions .btn {
  flex: 1;
  margin-top: 0;
  padding: 14px 24px;
}

/* Адаптивность */
@media (max-width: 768px) {
  .exercise-header {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px 16px;
  }

  .back-btn span {
    display: none;
  }

  .logo {
    order: 1;
  }

  .back-btn {
    order: 0;
  }

  .header-stats {
    order: 2;
    width: 100%;
    justify-content: space-between;
    gap: 8px;
  }

  .stat-value {
    font-size: 16px;
  }

  .exercise-main {
    padding: 24px 16px;
    gap: 24px;
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

  .completion-card {
    padding: 32px 24px;
  }

  .completion-stats {
    gap: 12px;
  }

  .completion-stat-value {
    font-size: 22px;
  }

  .completion-actions {
    flex-direction: column;
  }
}
</style>

