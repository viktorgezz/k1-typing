<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useBalanceStore } from '@/stores/balance'
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'
import GlassCard from '@/components/GlassCard.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import GenerateAvatarModal from '@/components/GenerateAvatarModal.vue'
import { userAPI } from '@/api/user'
import { avatarAPI } from '@/api/avatar'

const router = useRouter()
const authStore = useAuthStore()
const balanceStore = useBalanceStore()

// Состояния
const loading = ref(true)
const userData = ref({ username: '', role: '' })
const successMessage = ref('')
const errorMessage = ref('')
const showDeleteConfirm = ref(false)
const isDeleting = ref(false)
const isLoggingOut = ref(false)
const showGenerateModal = ref(false)

// Аватарка
const avatarPhoto = ref(null)
const avatarContentType = ref('image/png')

// Загрузка данных пользователя и аватарки при монтировании
onMounted(async () => {
  try {
    const [data, avatar] = await Promise.allSettled([
      userAPI.getMyself(),
      avatarAPI.getMyAvatar(),
    ])

    if (data.status === 'fulfilled') {
      userData.value = data.value
      username.value = data.value.username
    } else {
      errorMessage.value = 'Не удалось загрузить данные пользователя'
    }

    if (avatar.status === 'fulfilled' && avatar.value?.photo) {
      avatarPhoto.value = avatar.value.photo
      avatarContentType.value = avatar.value.contentType || 'image/png'
    }
    // Если аватарка не загрузилась — оставляем null, компонент покажет заглушку
  } catch (error) {
    console.error('Failed to load profile:', error)
    errorMessage.value = 'Не удалось загрузить данные пользователя'
  } finally {
    loading.value = false
  }
})

// Схема валидации для обновления данных
const schema = yup.object({
  username: yup
    .string()
    .required('Имя не должно быть пустым')
    .min(2, 'Минимальная длина имени должна быть 2')
    .max(255, 'Максимальная длина имени должна быть 255'),
  newPassword: yup
    .string()
    .required('Пароль не должен быть пустым')
    .min(6, 'Минимальная длина пароля должна быть 6'),
})

// Настройка формы с валидацией
const { handleSubmit, isSubmitting } = useForm({
  validationSchema: schema,
})

// Поля формы
const { value: username, errorMessage: usernameError } = useField('username')
const { value: newPassword, errorMessage: newPasswordError } = useField('newPassword')

// Обновление данных профиля
const onSubmit = handleSubmit(async (values) => {
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await userAPI.updateProfile({
      username: values.username,
      newPassword: values.newPassword,
    })
    
    // Обновляем данные в store
    authStore.updateUser({ ...userData.value, username: values.username })
    userData.value.username = values.username
    
    successMessage.value = 'Данные успешно обновлены'
    newPassword.value = ''
  } catch (error) {
    console.error('Failed to update profile:', error)
    errorMessage.value = error.response?.data?.message || 'Ошибка при обновлении данных'
  }
})

// Удаление аккаунта
const handleDeleteAccount = async () => {
  isDeleting.value = true
  errorMessage.value = ''

  try {
    await userAPI.deleteAccount()
    router.push('/login')
  } catch (error) {
    console.error('Failed to delete account:', error)
    errorMessage.value = error.response?.data?.message || 'Ошибка при удалении аккаунта'
    showDeleteConfirm.value = false
  } finally {
    isDeleting.value = false
  }
}

// Возврат на главную
const goBack = () => {
  router.push('/')
}

// Выход из аккаунта
const handleLogout = async () => {
  isLoggingOut.value = true
  const result = await authStore.logout()
  
  if (result.success) {
    balanceStore.resetBalance()
    router.push('/login')
  }
  isLoggingOut.value = false
}

// Маппинг ролей на русский язык
const getRoleDisplay = (role) => {
  const roles = {
    USER: 'Пользователь',
    ADMIN: 'Администратор',
  }
  return roles[role] || role
}
// Обработчик успешной отправки генерации
const onAvatarGenerated = () => {
  successMessage.value = 'Запрос на генерацию отправлен! Аватарка скоро появится.'
}
</script>

<template>
  <div class="bg-gradient page-container">
    <GlassCard max-width="800px">
      <!-- Навигация -->
      <div class="profile-nav">
        <button class="back-btn" @click="goBack">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          На главную
        </button>

        <button
          class="logout-btn"
          @click="handleLogout"
          :disabled="isLoggingOut"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          {{ isLoggingOut ? 'Выход...' : 'Выйти' }}
        </button>
      </div>

      <h1 class="form-title">Личный кабинет</h1>
      <p class="form-subtitle">Управление вашим аккаунтом</p>

      <!-- Загрузка -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Загрузка данных...</p>
      </div>

      <template v-else>
        <!-- Двухколоночная сетка -->
        <div class="profile-grid">
          <!-- Левая колонка: Аватарка -->
          <div class="avatar-section">
            <h2 class="section-title">Аватарка</h2>
            <div class="avatar-wrapper">
              <UserAvatar
                :photo="avatarPhoto"
                :content-type="avatarContentType"
                size="lg"
              />
            </div>
            <p class="avatar-hint">
              {{ avatarPhoto ? 'Ваша аватарка' : 'Аватарка не установлена' }}
            </p>
            <button
              class="btn-generate"
              @click="showGenerateModal = true"
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 3l1.912 5.813a2 2 0 0 0 1.272 1.278L21 12l-5.816 1.909a2 2 0 0 0-1.275 1.278L12 21l-1.912-5.813a2 2 0 0 0-1.272-1.278L3 12l5.816-1.909a2 2 0 0 0 1.275-1.278L12 3z" />
              </svg>
              Сгенерировать
            </button>
          </div>

          <!-- Правая колонка: Информация и формы -->
          <div class="info-section">
            <!-- Информация о пользователе -->
            <div class="user-info-section">
              <div class="user-info-item">
                <span class="user-info-label">Имя пользователя</span>
                <span class="user-info-value">{{ userData.username }}</span>
              </div>
              <div class="user-info-item">
                <span class="user-info-label">Роль</span>
                <span class="user-info-value role-badge" :class="userData.role?.toLowerCase()">
                  {{ getRoleDisplay(userData.role) }}
                </span>
              </div>
            </div>

            <div class="divider"></div>

            <!-- Сообщение об успехе -->
            <div v-if="successMessage" class="message message-success">
              {{ successMessage }}
            </div>

            <!-- Сообщение об ошибке -->
            <div v-if="errorMessage" class="message message-error">
              {{ errorMessage }}
            </div>

            <!-- Форма обновления данных -->
            <h2 class="section-title">Обновить данные</h2>
            
            <form @submit="onSubmit">
              <!-- Имя пользователя -->
              <div class="form-group">
                <label for="username" class="form-label">Новое имя пользователя</label>
                <input
                  id="username"
                  v-model="username"
                  type="text"
                  class="form-input"
                  :class="{ error: usernameError }"
                  placeholder="Введите новое имя"
                  autocomplete="username"
                />
                <span v-if="usernameError" class="form-error">{{ usernameError }}</span>
              </div>

              <!-- Новый пароль -->
              <div class="form-group">
                <label for="newPassword" class="form-label">Новый пароль</label>
                <input
                  id="newPassword"
                  v-model="newPassword"
                  type="password"
                  class="form-input"
                  :class="{ error: newPasswordError }"
                  placeholder="Введите новый пароль"
                  autocomplete="new-password"
                />
                <span v-if="newPasswordError" class="form-error">{{ newPasswordError }}</span>
              </div>

              <!-- Кнопка обновления -->
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="isSubmitting"
              >
                {{ isSubmitting ? 'Сохранение...' : 'Сохранить изменения' }}
              </button>
            </form>

            <div class="divider"></div>

            <!-- Секция удаления аккаунта -->
            <div class="danger-zone">
              <h2 class="section-title danger">Опасная зона</h2>
              <p class="danger-text">
                Удаление аккаунта необратимо. Все ваши данные будут удалены.
              </p>
              
              <button
                v-if="!showDeleteConfirm"
                class="btn btn-danger"
                @click="showDeleteConfirm = true"
              >
                Удалить аккаунт
              </button>

              <!-- Подтверждение удаления -->
              <div v-else class="confirm-delete">
                <p class="confirm-text">Вы уверены? Это действие нельзя отменить.</p>
                <div class="confirm-buttons">
                  <button
                    class="btn btn-danger"
                    :disabled="isDeleting"
                    @click="handleDeleteAccount"
                  >
                    {{ isDeleting ? 'Удаление...' : 'Да, удалить' }}
                  </button>
                  <button
                    class="btn btn-secondary"
                    :disabled="isDeleting"
                    @click="showDeleteConfirm = false"
                  >
                    Отмена
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </GlassCard>

    <!-- Модалка генерации аватарки -->
    <GenerateAvatarModal
      v-model="showGenerateModal"
      @generated="onAvatarGenerated"
    />
  </div>
</template>

<style scoped>
.profile-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.back-btn,
.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn {
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.logout-btn {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.logout-btn:hover:not(:disabled) {
  background: rgba(255, 107, 107, 0.25);
  border-color: rgba(255, 107, 107, 0.5);
  color: #fff;
}

.logout-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.back-btn svg,
.logout-btn svg {
  width: 18px;
  height: 18px;
}

/* Двухколоночная сетка */
.profile-grid {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  align-items: start;
}

/* Левая колонка — аватарка */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 24px;
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
}

.avatar-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  line-height: 1.4;
}

.btn-generate {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  color: #4A3728;
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-generate svg {
  width: 16px;
  height: 16px;
}

.btn-generate:hover {
  background: linear-gradient(135deg, var(--peach-light) 0%, var(--coral) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 160, 122, 0.4);
}

/* Правая колонка */
.info-section {
  min-width: 0;
}

.user-info-section {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.user-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.user-info-item:not(:last-child) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.user-info-value {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-badge.user {
  background: rgba(237, 229, 116, 0.2);
  color: var(--lemon);
}

.role-badge.admin {
  background: rgba(255, 160, 122, 0.3);
  color: var(--coral);
}

.divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
  margin: 28px 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
}

.section-title.danger {
  color: #ff6b6b;
}

.danger-zone {
  padding-top: 4px;
}

.danger-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 16px;
  line-height: 1.5;
}

.btn-danger {
  width: 100%;
  padding: 14px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff5252 0%, #d63031 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-delete {
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: 12px;
  padding: 20px;
}

.confirm-text {
  font-size: 15px;
  color: #ff6b6b;
  margin-bottom: 16px;
  text-align: center;
  font-weight: 500;
}

.confirm-buttons {
  display: flex;
  gap: 12px;
}

.confirm-buttons .btn {
  flex: 1;
  margin-top: 0;
  padding: 12px 20px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
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
  font-size: 15px;
  margin: 0;
}

/* Адаптивность */
@media (max-width: 640px) {
  .profile-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .avatar-section {
    position: static;
    flex-direction: row;
    padding: 16px 20px;
    gap: 16px;
  }

  .avatar-hint {
    text-align: left;
  }
}

@media (max-width: 480px) {
  .confirm-buttons {
    flex-direction: column;
  }

  .user-info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
