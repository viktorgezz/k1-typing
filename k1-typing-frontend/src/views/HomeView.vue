<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useExercisesStore } from '@/stores/exercises'
import { useMultiplayerStore } from '@/stores/multiplayer'
import ExerciseCard from '@/components/ExerciseCard.vue'
import RoomCard from '@/components/RoomCard.vue'
import CreateRoomModal from '@/components/CreateRoomModal.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const exercisesStore = useExercisesStore()
const multiplayerStore = useMultiplayerStore()

// Активная вкладка
const activeTab = ref('exercises')

// Модалка создания комнаты
const showCreateRoomModal = ref(false)

// Загрузка данных при монтировании
onMounted(() => {
  exercisesStore.fetchExercises()

  // Проверяем query параметр для активации нужной вкладки
  if (route.query.tab === 'contests') {
    activeTab.value = 'contests'
    multiplayerStore.fetchRooms()
  }
})

// Загрузка комнат при переключении на таб соревнований
watch(activeTab, (newTab) => {
  if (newTab === 'contests') {
    multiplayerStore.fetchRooms()
  }
})

// Обработка клика по упражнению
const handleExerciseClick = (exercise) => {
  router.push(`/exercise/${exercise.id}`)
}

// Обработка клика по комнате - переход в комнату
const handleRoomClick = (room) => {
  router.push(`/contest/${room.idContest}`)
}

// Обработка создания комнаты - переход в созданную комнату
const handleRoomCreated = (roomData) => {
  if (roomData?.idContest) {
    router.push(`/contest/${roomData.idContest}`)
  }
}

// Генерация номеров страниц для пагинации упражнений
const pageNumbers = computed(() => {
  const total = exercisesStore.pagination.totalPages
  const current = exercisesStore.pagination.page
  const pages = []

  // Показываем максимум 5 страниц
  let start = Math.max(0, current - 2)
  let end = Math.min(total - 1, start + 4)

  // Корректируем start если end близко к концу
  if (end - start < 4) {
    start = Math.max(0, end - 4)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// Генерация номеров страниц для пагинации комнат
const roomPageNumbers = computed(() => {
  const total = multiplayerStore.pagination.totalPages
  const current = multiplayerStore.pagination.page
  const pages = []

  // Показываем максимум 5 страниц
  let start = Math.max(0, current - 2)
  let end = Math.min(total - 1, start + 4)

  // Корректируем start если end близко к концу
  if (end - start < 4) {
    start = Math.max(0, end - 4)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})
</script>

<template>
  <div class="home-page bg-gradient">
    <!-- Хедер -->
    <header class="page-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">⌨</span>
          <span class="logo-text">k1typing</span>
        </div>

        <div class="user-section">
          <!-- Для авторизованных пользователей -->
          <template v-if="authStore.isAuthenticated">
            <router-link to="/profile" class="user-greeting user-greeting-link">
              Привет, <strong>{{ authStore.username || 'Пользователь' }}</strong>
            </router-link>
          </template>
          <!-- Для гостей -->
          <template v-else>
            <router-link to="/login" class="auth-btn auth-btn-secondary">
              Войти
            </router-link>
            <router-link to="/register" class="auth-btn auth-btn-primary">
              Регистрация
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- Табы переключения режимов -->
    <div class="tabs-container">
      <div class="tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'exercises' }"
          @click="activeTab = 'exercises'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          </svg>
          Упражнения
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'contests' }"
          @click="activeTab = 'contests'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
            <path d="M4 22h16" />
            <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
            <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
            <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
          </svg>
          Соревнования
        </button>
      </div>
    </div>

    <!-- Основной контент -->
    <main class="main-content">
      <!-- Контент упражнений -->
      <template v-if="activeTab === 'exercises'">
        <div class="section-header">
          <h1 class="page-title">Упражнения</h1>
          <p class="page-subtitle">
            Выберите упражнение для тренировки скорости печати
          </p>
        </div>

        <!-- Состояние загрузки -->
        <div v-if="exercisesStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Загрузка упражнений...</p>
      </div>

      <!-- Ошибка -->
      <div v-else-if="exercisesStore.error" class="error-state">
        <div class="error-icon">⚠</div>
        <p>{{ exercisesStore.error }}</p>
        <button class="btn btn-primary" @click="exercisesStore.fetchExercises()">
          Попробовать снова
        </button>
      </div>

      <!-- Пустое состояние -->
      <div
        v-else-if="!exercisesStore.hasExercises"
        class="empty-state"
      >
        <div class="empty-icon">📚</div>
        <p>Упражнения пока не добавлены</p>
      </div>

      <!-- Сетка упражнений -->
      <div v-else class="exercises-grid">
        <ExerciseCard
          v-for="exercise in exercisesStore.exercises"
          :key="exercise.id"
          :title="exercise.title"
          :author="exercise.nameTile"
          :data-id="exercise.id"
          @click="handleExerciseClick(exercise)"
        />
      </div>

      <!-- Пагинация -->
      <nav
        v-if="exercisesStore.pagination.totalPages > 1"
        class="pagination"
      >
        <button
          class="pagination-btn pagination-arrow"
          :disabled="!exercisesStore.hasPrevPage || exercisesStore.loading"
          @click="exercisesStore.prevPage()"
        >
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
        </button>

        <div class="pagination-pages">
          <button
            v-for="pageNum in pageNumbers"
            :key="pageNum"
            class="pagination-btn"
            :class="{ active: pageNum === exercisesStore.pagination.page }"
            :disabled="exercisesStore.loading"
            @click="exercisesStore.goToPage(pageNum)"
          >
            {{ pageNum + 1 }}
          </button>
        </div>

        <button
          class="pagination-btn pagination-arrow"
          :disabled="!exercisesStore.hasNextPage || exercisesStore.loading"
          @click="exercisesStore.nextPage()"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="9 18 15 12 9 6" />
          </svg>
        </button>
      </nav>

        <!-- Информация о пагинации -->
        <p v-if="exercisesStore.hasExercises" class="pagination-info">
          Показано {{ exercisesStore.exercises.length }} из
          {{ exercisesStore.pagination.totalElements }} упражнений
        </p>
      </template>

      <!-- Контент соревнований -->
      <template v-else-if="activeTab === 'contests'">
        <div class="section-header">
          <h1 class="page-title">Соревнования</h1>
          <p class="page-subtitle">
            Присоединяйтесь к соревнованиям или создайте свою комнату
          </p>
        </div>

        <!-- Кнопка создания комнаты -->
        <div class="contests-actions">
          <button
            v-if="authStore.isAuthenticated"
            class="btn-create-room"
            @click="showCreateRoomModal = true"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Создать комнату
          </button>
          <p v-else class="create-room-hint">
            <router-link to="/login" class="create-room-link">Войдите</router-link>
            , чтобы создать комнату для соревнований
          </p>
        </div>

        <!-- Состояние загрузки -->
        <div v-if="multiplayerStore.loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Загрузка комнат...</p>
        </div>

        <!-- Ошибка -->
        <div v-else-if="multiplayerStore.error" class="error-state">
          <div class="error-icon">⚠</div>
          <p>{{ multiplayerStore.error }}</p>
          <button class="btn btn-primary" @click="multiplayerStore.fetchRooms()">
            Попробовать снова
          </button>
        </div>

        <!-- Пустое состояние -->
        <div v-else-if="!multiplayerStore.hasRooms" class="empty-state">
          <div class="empty-icon">🏆</div>
          <p>Сейчас нет доступных комнат</p>
          <p class="empty-subtitle">Создайте свою комнату, чтобы начать соревнование!</p>
        </div>

        <!-- Сетка комнат -->
        <div v-else class="rooms-grid">
          <RoomCard
            v-for="room in multiplayerStore.rooms"
            :key="room.idContest"
            :id-contest="room.idContest"
            :title-exercise="room.titleExercise"
            :language="room.language"
            :current-players="room.currentPlayers"
            :max-players="room.maxPlayers"
            :created-at="room.createdAt"
            @click="handleRoomClick(room)"
          />
        </div>

        <!-- Пагинация комнат -->
        <nav
          v-if="multiplayerStore.pagination.totalPages > 1"
          class="pagination"
        >
          <button
            class="pagination-btn pagination-arrow"
            :disabled="!multiplayerStore.hasPrevPage || multiplayerStore.loading"
            @click="multiplayerStore.prevPage()"
          >
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
          </button>

          <div class="pagination-pages">
            <button
              v-for="pageNum in roomPageNumbers"
              :key="pageNum"
              class="pagination-btn"
              :class="{ active: pageNum === multiplayerStore.pagination.page }"
              :disabled="multiplayerStore.loading"
              @click="multiplayerStore.goToPage(pageNum)"
            >
              {{ pageNum + 1 }}
            </button>
          </div>

          <button
            class="pagination-btn pagination-arrow"
            :disabled="!multiplayerStore.hasNextPage || multiplayerStore.loading"
            @click="multiplayerStore.nextPage()"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="9 18 15 12 9 6" />
            </svg>
          </button>
        </nav>

        <!-- Информация о пагинации комнат -->
        <p v-if="multiplayerStore.hasRooms" class="pagination-info">
          Показано {{ multiplayerStore.rooms.length }} из
          {{ multiplayerStore.pagination.totalElements }} комнат
        </p>
      </template>
    </main>

    <!-- Модалка создания комнаты -->
    <CreateRoomModal
      :show="showCreateRoomModal"
      @close="showCreateRoomModal = false"
      @created="handleRoomCreated"
    />
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Хедер */
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

/* Табы */
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

/* Действия для соревнований */
.contests-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
}

.btn-create-room {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 160, 122, 0.35);
  }

.btn-create-room:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 160, 122, 0.45);
}

.btn-create-room svg {
  width: 20px;
  height: 20px;
}

.create-room-hint {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

.create-room-link {
  color: var(--coral);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.create-room-link:hover {
  color: var(--lemon);
  text-decoration: underline;
}

/* Сетка комнат */
.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
}

.empty-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
  opacity: 0.8;
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

.user-greeting strong {
  font-weight: 600;
  color: var(--neutral-dark);
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

.user-greeting-link:hover strong {
  color: var(--coral);
}

/* Кнопки авторизации для гостей */
.auth-btn {
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.auth-btn-secondary {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(74, 55, 40, 0.15);
}

.auth-btn-secondary:hover {
  background: rgba(255, 255, 255, 0.5);
}

.auth-btn-primary {
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  border: 1px solid transparent;
  box-shadow: 0 2px 8px rgba(255, 160, 122, 0.3);
}

.auth-btn-primary:hover {
  box-shadow: 0 4px 12px rgba(255, 160, 122, 0.4);
  transform: translateY(-1px);
}

/* Основной контент */
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

/* Сетка упражнений */
.exercises-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
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

.loading-state p {
  font-size: 16px;
  margin: 0;
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

.error-state .btn {
  width: auto;
  padding: 12px 24px;
  font-size: 15px;
}

/* Пустое состояние */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.empty-state p {
  font-size: 18px;
  margin: 0;
}

/* Пагинация */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.pagination-pages {
  display: flex;
  gap: 4px;
}

.pagination-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(74, 55, 40, 0.1);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}

.pagination-btn:hover:not(:disabled):not(.active) {
  background: rgba(255, 255, 255, 0.6);
  border-color: rgba(74, 55, 40, 0.2);
}

.pagination-btn.active {
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  color: var(--text-primary);
  border-color: transparent;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 160, 122, 0.35);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-arrow svg {
  width: 18px;
  height: 18px;
}

.pagination-info {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* Адаптивность */
@media (max-width: 768px) {
  .header-content {
    padding: 12px 16px;
  }

  .logo-text {
    display: none;
  }

  .user-greeting {
    display: none;
  }

  .tabs {
    padding: 0 16px;
  }

  .tab-btn {
    padding: 14px 16px;
    font-size: 14px;
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

  .exercises-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .placeholder-icon {
    font-size: 60px;
  }

  .placeholder-title {
    font-size: 20px;
  }

  .placeholder-text {
    font-size: 14px;
  }
}
</style>
