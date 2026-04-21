<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useStatisticsStore } from '@/stores/statistics'
import { useBalanceStore } from '@/stores/balance'
import BalanceDisplay from '@/components/BalanceDisplay.vue'

const router = useRouter()
const authStore = useAuthStore()
const statisticsStore = useStatisticsStore()
const balanceStore = useBalanceStore()

// Активная вкладка статистики
const activeTab = ref('leaderboard')

// При монтировании загружаем данные для первой вкладки
onMounted(() => {
  statisticsStore.fetchLeaderboard()
})

// При переключении вкладок — подгружаем соответствующие данные
watch(activeTab, (newTab) => {
  if (newTab === 'leaderboard' && !statisticsStore.hasLeaderboard) {
    statisticsStore.fetchLeaderboard()
  } else if (newTab === 'exercises' && !statisticsStore.hasExerciseRecords) {
    statisticsStore.fetchExerciseRecords()
  } else if (newTab === 'personal') {
    if (!authStore.isAuthenticated) {
      router.push({ name: 'login' })
      return
    }
    statisticsStore.fetchPersonalStatistics()
  }
})

/**
 * Переключение на вкладку personal с проверкой авторизации
 */
function switchToPersonal() {
  if (!authStore.isAuthenticated) {
    router.push({ name: 'login' })
    return
  }
  activeTab.value = 'personal'
}

/**
 * Форматирование длительности из секунд в читаемый формат
 */
function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return '—'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes > 0) {
    return `${minutes}м ${secs}с`
  }
  return `${secs}с`
}

/**
 * Иконка для ранга в лидерборде
 */
function getRankIcon(rank) {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return `#${rank}`
}

// Вычисляемые свойства для персональной статистики (карточки)
const personalCards = computed(() => {
  if (!statisticsStore.personalStats) return []
  const s = statisticsStore.personalStats
  return [
    { label: 'Место в рейтинге', value: s.rankPlace ? `#${s.rankPlace}` : '—', icon: '🏆' },
    { label: 'Средняя скорость', value: s.averageSpeed ? `${Number(s.averageSpeed).toFixed(1)} зн/мин` : '—', icon: '⚡' },
    { label: 'Всего соревнований', value: s.countContest ?? '—', icon: '🎯' },
    { label: 'Первых мест', value: s.firstPlacesCount ?? '—', icon: '🥇' },
  ]
})
</script>

<template>
  <div class="statistics-page bg-gradient">
    <!-- Хедер -->
    <header class="page-header">
      <div class="header-content">
        <router-link to="/" class="logo">
          <span class="logo-icon">⌨</span>
          <span class="logo-text">k1typing</span>
        </router-link>

        <div class="user-section">
          <!-- Для авторизованных пользователей -->
          <template v-if="authStore.isAuthenticated">
            <BalanceDisplay :balance="balanceStore.userBalance" />
            <router-link to="/profile" class="user-greeting user-greeting-link">
              Профиль
            </router-link>
          </template>
          <!-- Для гостей -->
          <template v-else>
            <router-link to="/login" class="auth-card">
              <div class="auth-card-glass">
                <div class="auth-card-refraction"></div>
              </div>
              <span class="auth-card-label">Войти</span>
            </router-link>
            <router-link to="/register" class="auth-card">
              <div class="auth-card-glass">
                <div class="auth-card-refraction"></div>
              </div>
              <span class="auth-card-label">Регистрация</span>
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- Табы переключения видов статистики -->
    <div class="tabs-container">
      <div class="tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'leaderboard' }"
          @click="activeTab = 'leaderboard'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
            <path d="M4 22h16" />
            <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
            <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
            <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
          </svg>
          Лидерборд
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'exercises' }"
          @click="activeTab = 'exercises'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          </svg>
          Рекорды упражнений
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'personal', locked: !authStore.isAuthenticated }"
          @click="switchToPersonal"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          Моя статистика
          <span v-if="!authStore.isAuthenticated" class="lock-icon">🔒</span>
        </button>
      </div>
    </div>

    <!-- Основной контент -->
    <main class="main-content">
      <!-- ========== ЛИДЕРБОРД ========== -->
      <template v-if="activeTab === 'leaderboard'">
        <div class="section-header">
          <h1 class="page-title">Лидерборд</h1>
          <p class="page-subtitle">Топ-10 лучших игроков по скорости печати</p>
        </div>

        <!-- Загрузка -->
        <div v-if="statisticsStore.loadingLeaderboard" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Загрузка лидерборда...</p>
        </div>

        <!-- Ошибка -->
        <div v-else-if="statisticsStore.errorLeaderboard" class="error-state">
          <div class="error-icon">⚠</div>
          <p>{{ statisticsStore.errorLeaderboard }}</p>
          <button class="btn btn-primary retry-btn" @click="statisticsStore.fetchLeaderboard()">
            Попробовать снова
          </button>
        </div>

        <!-- Пусто -->
        <div v-else-if="!statisticsStore.hasLeaderboard" class="empty-state">
          <div class="empty-icon">🏆</div>
          <p>Пока нет данных для лидерборда</p>
          <p class="empty-subtitle">Примите участие в соревновании, чтобы попасть в рейтинг!</p>
        </div>

        <!-- Таблица лидерборда -->
        <div v-else class="leaderboard-table-wrap">
          <div class="glass-table">
            <div class="table-header">
              <div class="th th-rank">Место</div>
              <div class="th th-user">Игрок</div>
              <div class="th th-speed">Ср. скорость</div>
              <div class="th th-contests">Соревнований</div>
              <div class="th th-wins">Побед</div>
            </div>
            <div
              v-for="entry in statisticsStore.leaderboard"
              :key="entry.rankPlace"
              class="table-row"
              :class="{ 'top-three': entry.rankPlace <= 3 }"
            >
              <div class="td td-rank">
                <span class="rank-badge" :class="`rank-${entry.rankPlace}`">
                  {{ getRankIcon(entry.rankPlace) }}
                </span>
              </div>
              <div class="td td-user">
                <span class="user-name">{{ entry.username }}</span>
              </div>
              <div class="td td-speed">
                <span class="speed-value">{{ Number(entry.averageSpeed).toFixed(1) }}</span>
                <span class="speed-unit">зн/мин</span>
              </div>
              <div class="td td-contests">{{ entry.countContest }}</div>
              <div class="td td-wins">{{ entry.firstPlacesCount }}</div>
            </div>
          </div>
        </div>
      </template>

      <!-- ========== РЕКОРДЫ УПРАЖНЕНИЙ ========== -->
      <template v-else-if="activeTab === 'exercises'">
        <div class="section-header">
          <h1 class="page-title">Рекорды упражнений</h1>
          <p class="page-subtitle">Лучшие результаты по каждому упражнению</p>
        </div>

        <!-- Загрузка -->
        <div v-if="statisticsStore.loadingExercises" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Загрузка рекордов...</p>
        </div>

        <!-- Ошибка -->
        <div v-else-if="statisticsStore.errorExercises" class="error-state">
          <div class="error-icon">⚠</div>
          <p>{{ statisticsStore.errorExercises }}</p>
          <button class="btn btn-primary retry-btn" @click="statisticsStore.fetchExerciseRecords()">
            Попробовать снова
          </button>
        </div>

        <!-- Пусто -->
        <div v-else-if="!statisticsStore.hasExerciseRecords" class="empty-state">
          <div class="empty-icon">📊</div>
          <p>Пока нет рекордов по упражнениям</p>
          <p class="empty-subtitle">Начните тренировку, чтобы установить первый рекорд!</p>
        </div>

        <!-- Карточки рекордов -->
        <div v-else class="records-grid">
          <div
            v-for="(record, index) in statisticsStore.exerciseRecords"
            :key="index"
            class="record-card"
          >
            <div class="record-card-glass"></div>
            <div class="record-card-content">
              <div class="record-title">{{ record.title }}</div>
              <div class="record-holder">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="record-icon">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                {{ record.username }}
              </div>
              <div class="record-stats">
                <div class="record-stat">
                  <span class="record-stat-value">{{ record.maxSpeed }}</span>
                  <span class="record-stat-label">зн/мин</span>
                </div>
                <div class="record-stat-divider"></div>
                <div class="record-stat">
                  <span class="record-stat-value">{{ formatDuration(record.minDuration) }}</span>
                  <span class="record-stat-label">время</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- ========== ПЕРСОНАЛЬНАЯ СТАТИСТИКА ========== -->
      <template v-else-if="activeTab === 'personal'">
        <div class="section-header">
          <h1 class="page-title">Моя статистика</h1>
          <p class="page-subtitle">Ваши личные достижения и показатели</p>
        </div>

        <!-- Загрузка -->
        <div v-if="statisticsStore.loadingPersonal" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Загрузка вашей статистики...</p>
        </div>

        <!-- Ошибка -->
        <div v-else-if="statisticsStore.errorPersonal" class="error-state">
          <div class="error-icon">⚠</div>
          <p>{{ statisticsStore.errorPersonal }}</p>
          <button class="btn btn-primary retry-btn" @click="statisticsStore.fetchPersonalStatistics()">
            Попробовать снова
          </button>
        </div>

        <!-- Нет данных -->
        <div v-else-if="!statisticsStore.hasPersonalStats" class="empty-state">
          <div class="empty-icon">📈</div>
          <p>Статистика пока недоступна</p>
          <p class="empty-subtitle">Примите участие в соревновании, чтобы увидеть вашу статистику</p>
        </div>

        <!-- Карточки персональной статистики -->
        <div v-else class="personal-grid">
          <div
            v-for="card in personalCards"
            :key="card.label"
            class="personal-card"
          >
            <div class="personal-card-glass"></div>
            <div class="personal-card-content">
              <div class="personal-card-icon">{{ card.icon }}</div>
              <div class="personal-card-value">{{ card.value }}</div>
              <div class="personal-card-label">{{ card.label }}</div>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped>
.statistics-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ========== Хедер (идентичный HomeView) ========== */
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  font-size: 28px;
  filter: drop-shadow(0 2px 4px rgba(74, 55, 40, 0.2));
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-greeting {
  font-size: 15px;
  color: var(--text-primary);
}

.user-greeting-link {
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid transparent;
}

.user-greeting-link:hover {
  background: rgba(255, 255, 255, 0.4);
  border-color: rgba(74, 55, 40, 0.15);
}

/* Кнопки авторизации — стиль карточек */
.auth-card {
  position: relative;
  padding: 8px 18px;
  border-radius: 12px;
  text-decoration: none;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-card:hover {
  transform: translateY(-3px) scale(1.04);
  box-shadow:
    0 12px 28px -6px rgba(74, 55, 40, 0.45),
    0 0 16px rgba(255, 160, 122, 0.12);
}

.auth-card-glass {
  position: absolute;
  inset: 0;
  background-color: rgba(74, 55, 40, 0.666);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow:
    0 4px 16px rgba(74, 55, 40, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.auth-card-refraction {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse at 30% 0%,
    rgba(255, 200, 160, 0.1) 0%,
    transparent 50%
  );
  opacity: 0.6;
  pointer-events: none;
}

.auth-card-label {
  position: relative;
  z-index: 1;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.01em;
}

/* ========== Табы ========== */
.tabs-container {
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.tabs {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: center;
  gap: 4px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.tab-btn svg {
  width: 20px;
  height: 20px;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.tab-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.1);
}

.tab-btn:hover svg {
  opacity: 1;
}

.tab-btn.active {
  color: var(--text-primary);
  border-bottom-color: var(--coral);
}

.tab-btn.active svg {
  opacity: 1;
  color: var(--coral);
}

.tab-btn.locked {
  opacity: 0.65;
}

.lock-icon {
  font-size: 14px;
  margin-left: 2px;
}

/* ========== Main Content ========== */
.main-content {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px 60px;
  width: 100%;
}

.section-header {
  margin-bottom: 40px;
  text-align: center;
}

.page-title {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin: 0;
}

/* ========== Loading / Error / Empty ========== */
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
  to { transform: rotate(360deg); }
}

.loading-state p { font-size: 16px; margin: 0; }

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.error-icon { font-size: 48px; margin-bottom: 16px; }

.error-state p {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0 0 24px;
}

.retry-btn {
  width: auto;
  padding: 12px 24px;
  font-size: 15px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-secondary);
}

.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.8; }
.empty-state p { font-size: 18px; margin: 0; }
.empty-subtitle { font-size: 14px; color: var(--text-secondary); margin-top: 8px; opacity: 0.8; }

/* ========== LEADERBOARD TABLE ========== */
.leaderboard-table-wrap {
  max-width: 900px;
  margin: 0 auto;
}

.glass-table {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow:
    0 8px 32px rgba(74, 55, 40, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1fr 140px 140px 100px;
  padding: 16px 24px;
  background: rgba(74, 55, 40, 0.08);
  border-bottom: 1px solid rgba(74, 55, 40, 0.08);
}

.th {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.table-row {
  display: grid;
  grid-template-columns: 80px 1fr 140px 140px 100px;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.2s ease;
  align-items: center;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: rgba(255, 255, 255, 0.15);
}

.table-row.top-three {
  background: rgba(255, 200, 160, 0.08);
}

.table-row.top-three:hover {
  background: rgba(255, 200, 160, 0.18);
}

.td { font-size: 15px; color: var(--text-primary); }

.rank-badge {
  font-size: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
}

.rank-badge.rank-1,
.rank-badge.rank-2,
.rank-badge.rank-3 {
  font-size: 24px;
}

.user-name {
  font-weight: 600;
}

.speed-value {
  font-weight: 700;
  font-size: 17px;
  color: var(--neutral-dark);
}

.speed-unit {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 4px;
}

/* ========== EXERCISE RECORDS GRID ========== */
.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.record-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.record-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow:
    0 16px 40px -8px rgba(74, 55, 40, 0.3),
    0 0 20px rgba(255, 160, 122, 0.1);
}

.record-card-glass {
  position: absolute;
  inset: 0;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 16px;
  box-shadow:
    0 4px 16px rgba(74, 55, 40, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.record-card-content {
  position: relative;
  z-index: 1;
  padding: 24px;
}

.record-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--neutral-dark);
  margin-bottom: 12px;
  line-height: 1.3;
}

.record-holder {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  font-weight: 500;
}

.record-icon {
  width: 16px;
  height: 16px;
  opacity: 0.7;
}

.record-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.record-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.record-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--neutral-dark);
}

.record-stat-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.record-stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(74, 55, 40, 0.15);
}

/* ========== PERSONAL STATS GRID ========== */
.personal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
  max-width: 1040px;
  margin: 0 auto;
}

.personal-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.personal-card:hover {
  transform: translateY(-6px) scale(1.03);
  box-shadow:
    0 20px 48px -10px rgba(74, 55, 40, 0.25),
    0 0 24px rgba(255, 160, 122, 0.12);
}

.personal-card-glass {
  position: absolute;
  inset: 0;
  backdrop-filter: blur(20px) saturate(200%);
  -webkit-backdrop-filter: blur(20px) saturate(200%);
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow:
    0 4px 20px rgba(74, 55, 40, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.personal-card-content {
  position: relative;
  z-index: 1;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}

.personal-card-icon {
  font-size: 40px;
  line-height: 1;
}

.personal-card-value {
  font-size: 32px;
  font-weight: 800;
  color: var(--neutral-dark);
  line-height: 1.1;
}

.personal-card-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .header-content {
    padding: 12px 16px;
  }

  .logo-text {
    display: none;
  }

  .tabs {
    padding: 0 8px;
    gap: 0;
  }

  .tab-btn {
    padding: 14px 12px;
    font-size: 13px;
    gap: 6px;
  }

  .tab-btn svg {
    width: 18px;
    height: 18px;
  }

  .main-content {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .page-subtitle {
    font-size: 16px;
  }

  /* Таблица → мобильные карточки */
  .table-header { display: none; }

  .table-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 16px;
    padding: 16px;
  }

  .td-rank { order: 1; }
  .td-user { order: 2; flex: 1; }
  .td-speed { order: 3; width: 100%; }
  .td-contests { order: 4; }
  .td-wins { order: 5; }

  .records-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .personal-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .personal-card-value { font-size: 24px; }
  .personal-card-icon { font-size: 32px; }
}

@media (max-width: 480px) {
  .personal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
