<script setup>
import { ref, watch } from 'vue'
import { avatarAPI } from '@/api/avatar'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'generated'])

const prompt = ref('')
const errorMessage = ref('')
const submitted = ref(false)

// Сброс состояния при открытии/закрытии
watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      prompt.value = ''
      errorMessage.value = ''
      submitted.value = false
    }
  }
)

const close = () => {
  emit('update:modelValue', false)
}

const handleOverlayClick = (event) => {
  if (event.target === event.currentTarget) {
    close()
  }
}

const handleSubmit = async () => {
  if (!prompt.value.trim()) {
    errorMessage.value = 'Введите описание для генерации'
    return
  }

  errorMessage.value = ''

  try {
    await avatarAPI.generateAvatar(prompt.value.trim())
    submitted.value = true
    emit('generated')
  } catch (error) {
    console.error('Failed to generate avatar:', error)
    errorMessage.value =
      error.response?.data?.message || 'Ошибка при отправке запроса на генерацию'
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="modelValue"
        class="modal-overlay"
        @click="handleOverlayClick"
      >
        <div class="modal-card">
          <h2 class="modal-title">Генерация аватарки</h2>

          <!-- До отправки -->
          <template v-if="!submitted">
            <p class="modal-description">
              Опишите, какую аватарку вы хотите сгенерировать
            </p>

            <div class="form-group">
              <textarea
                v-model="prompt"
                class="form-input modal-textarea"
                :class="{ error: errorMessage }"
                placeholder="Например: Кот в космическом шлеме на фоне звёзд"
                rows="3"
                @keydown.enter.ctrl="handleSubmit"
              />
              <span v-if="errorMessage" class="form-error">{{ errorMessage }}</span>
            </div>

            <button
              class="btn btn-primary"
              @click="handleSubmit"
            >
              Сгенерировать
            </button>

            <button
              class="btn btn-secondary"
              @click="close"
            >
              Отмена
            </button>
          </template>

          <!-- После отправки -->
          <template v-else>
            <div class="success-block">
              <div class="success-icon">✨</div>
              <p class="success-text">
                Запрос отправлен! Изображение сгенерируется и скоро появится в вашем профиле.
              </p>
              <button class="btn btn-primary" @click="close">
                Вернуться в профиль
              </button>
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.modal-card {
  width: 100%;
  max-width: 480px;
  padding: 36px 32px;
  border-radius: 20px;
  background: rgba(74, 55, 40, 0.92);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 24px 48px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  text-align: center;
  margin-bottom: 8px;
}

.modal-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
  text-align: center;
  margin-bottom: 20px;
  line-height: 1.5;
}

.modal-textarea {
  resize: vertical;
  min-height: 80px;
  max-height: 200px;
  line-height: 1.5;
  font-family: inherit;
}

/* Успешная отправка */
.success-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.success-icon {
  font-size: 48px;
  line-height: 1;
}

.success-text {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  line-height: 1.6;
}

/* Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}

.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-card {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}

.modal-leave-to .modal-card {
  transform: scale(0.95) translateY(10px);
  opacity: 0;
}
</style>
