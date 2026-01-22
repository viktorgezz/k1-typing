<script setup>
import { ref, computed, watch } from 'vue'
import { useExercisesStore } from '@/stores/exercises'
import { useMultiplayerStore } from '@/stores/multiplayer'

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'created'])

const exercisesStore = useExercisesStore()
const multiplayerStore = useMultiplayerStore()

const selectedExerciseId = ref(null)
const maxParticipants = ref(2)
const loading = ref(false)
const error = ref(null)

// Загружаем упражнения при открытии модалки
watch(() => props.show, (isOpen) => {
  if (isOpen && exercisesStore.exercises.length === 0) {
    exercisesStore.fetchExercises()
  }
  if (isOpen) {
    resetForm()
  }
})

const resetForm = () => {
  selectedExerciseId.value = null
  maxParticipants.value = 2
  error.value = null
}

const availableExercises = computed(() => exercisesStore.exercises)

const canSubmit = computed(() => {
  return selectedExerciseId.value !== null && maxParticipants.value >= 2 && maxParticipants.value <= 15
})

const handleSubmit = async () => {
  if (!canSubmit.value || loading.value) return

  loading.value = true
  error.value = null

  try {
    const result = await multiplayerStore.createRoom({
      idExercise: selectedExerciseId.value,
      maxParticipants: maxParticipants.value,
    })

    if (result.success) {
      emit('created', result.data)
      emit('close')
    } else {
      error.value = result.error
    }
  } catch (err) {
    error.value = err.message || 'Ошибка создания комнаты'
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  if (!loading.value) {
    emit('close')
  }
}
</script>

<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="handleClose">
      <div class="modal-container">
        <div class="modal-header">
          <h2 class="modal-title">Создать комнату</h2>
          <button class="modal-close" @click="handleClose" :disabled="loading">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <!-- Выбор упражнения -->
          <div class="form-group">
            <label class="form-label">Выберите упражнение</label>
            <div v-if="exercisesStore.loading" class="loading-exercises">
              <div class="loading-spinner-small"></div>
              <span>Загрузка упражнений...</span>
            </div>
            <select
              v-else
              v-model="selectedExerciseId"
              class="form-select"
              :disabled="loading"
            >
              <option :value="null" disabled>-- Выберите упражнение --</option>
              <option
                v-for="exercise in availableExercises"
                :key="exercise.id"
                :value="exercise.id"
              >
                {{ exercise.title }} ({{ exercise.language === 'RU' ? 'Русский' : 'English' }})
              </option>
            </select>
            <p v-if="!exercisesStore.hasExercises && !exercisesStore.loading" class="form-hint">
              Нет доступных упражнений
            </p>
          </div>

          <!-- Количество участников -->
          <div class="form-group">
            <label class="form-label">
              Максимальное количество участников
              <span class="form-label-hint">(от 2 до 15)</span>
            </label>
            <div class="participants-input-group">
              <button
                class="participants-btn"
                @click="maxParticipants = Math.max(2, maxParticipants - 1)"
                :disabled="loading || maxParticipants <= 2"
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
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
              </button>
              <input
                v-model.number="maxParticipants"
                type="number"
                min="2"
                max="15"
                class="participants-input"
                :disabled="loading"
              />
              <button
                class="participants-btn"
                @click="maxParticipants = Math.min(15, maxParticipants + 1)"
                :disabled="loading || maxParticipants >= 15"
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
              </button>
            </div>
          </div>

          <!-- Ошибка -->
          <div v-if="error" class="form-error-message">
            {{ error }}
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="handleClose" :disabled="loading">
            Отмена
          </button>
          <button
            class="btn btn-primary"
            @click="handleSubmit"
            :disabled="!canSubmit || loading"
          >
            <span v-if="loading">Создание...</span>
            <span v-else>Создать комнату</span>
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(74, 55, 40, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: rgba(74, 55, 40, 0.95);
  backdrop-filter: blur(16px) saturate(200%);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(74, 55, 40, 0.5);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-light);
  margin: 0;
}

.modal-close {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-light);
}

.modal-close:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.modal-close:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 28px;
  flex: 1;
}

.form-group {
  margin-bottom: 24px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.9);
}

.form-label-hint {
  font-size: 13px;
  font-weight: 400;
  opacity: 0.7;
}

.form-select {
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-light);
  transition: all 0.3s ease;
  outline: none;
  cursor: pointer;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

.form-select:focus {
  border-color: var(--coral);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 4px rgba(255, 160, 122, 0.25);
}

.form-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Стили для option элементов - явно задаем цвета для кроссбраузерной совместимости */
.form-select option {
  background-color: #f5f5f5 !important;
  color: #333333 !important;
  padding: 10px;
  font-size: 16px;
}

/* Стили для выбранного option (при наведении в выпадающем списке) */
.form-select option:checked,
.form-select option:hover,
.form-select option:focus {
  background-color: #4A90E2 !important;
  color: #ffffff !important;
}

/* Стили для disabled option */
.form-select option:disabled {
  background-color: #e0e0e0 !important;
  color: #999999 !important;
}

/* Дополнительные стили для лучшей совместимости в разных браузерах */
.form-select::-ms-expand {
  display: none; /* Скрываем стрелку в IE/Edge */
}

/* Для WebKit браузеров (Chrome, Safari) */
.form-select::-webkit-select-placeholder {
  color: rgba(255, 255, 255, 0.45);
}

/* Убеждаемся, что цвет текста в select всегда белый */
.form-select,
.form-select:focus,
.form-select:active {
  color: var(--text-light) !important;
}

.loading-exercises {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.loading-spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top-color: var(--coral);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.form-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8px;
}

.participants-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.participants-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-light);
}

.participants-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: var(--coral);
}

.participants-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.participants-btn svg {
  width: 20px;
  height: 20px;
}

.participants-input {
  flex: 1;
  padding: 14px 18px;
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-light);
  transition: all 0.3s ease;
  outline: none;
}

.participants-input:focus {
  border-color: var(--coral);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 4px rgba(255, 160, 122, 0.25);
}

.participants-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-error-message {
  padding: 12px 16px;
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.4);
  border-radius: 8px;
  color: #ff6b6b;
  font-size: 14px;
  margin-top: 16px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 24px 28px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-footer .btn {
  flex: 1;
  margin-top: 0;
}

/* Transition */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9) translateY(-20px);
  opacity: 0;
}
</style>
