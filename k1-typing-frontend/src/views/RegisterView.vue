<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'
import GlassCard from '@/components/GlassCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const successMessage = ref('')
const errorMessage = ref('')

// Схема валидации на основе RegistrationRequest.java
const schema = yup.object({
  username: yup
    .string()
    .required('Имя не должно быть пустым')
    .min(2, 'Минимальная длина имени должна быть 2')
    .max(255, 'Максимальная длина имени должна быть 255'),
  password: yup
    .string()
    .required('Пароль не должен быть пустым')
    .min(8, 'Минимальная длина пароля должна быть 8')
    .max(255, 'Максимальная длина пароля должна быть 255'),
  confirmPassword: yup
    .string()
    .required('Повторный пароль не должен быть пустым')
    .min(8, 'Минимальная длина повторного пароля должна быть 8')
    .max(255, 'Максимальная длина повторного пароля должна быть 255')
    .oneOf([yup.ref('password')], 'Пароли не совпадают'),
})

// Настройка формы с валидацией
const { handleSubmit, isSubmitting } = useForm({
  validationSchema: schema,
})

// Поля формы
const { value: username, errorMessage: usernameError } = useField('username')
const { value: password, errorMessage: passwordError } = useField('password')
const { value: confirmPassword, errorMessage: confirmPasswordError } = useField('confirmPassword')

// Обработка отправки формы
const onSubmit = handleSubmit(async (values) => {
  errorMessage.value = ''
  successMessage.value = ''

  const result = await authStore.register(values)

  if (result.success) {
    successMessage.value = 'Регистрация успешна! Перенаправление на страницу входа...'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } else {
    errorMessage.value = result.error || 'Произошла ошибка при регистрации'
  }
})
</script>

<template>
  <div class="bg-gradient page-container">
    <GlassCard>
      <h1 class="form-title">Регистрация</h1>
      <p class="form-subtitle">Создайте аккаунт для начала работы</p>

      <!-- Сообщение об успехе -->
      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <!-- Сообщение об ошибке -->
      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <form @submit="onSubmit">
        <!-- Имя пользователя -->
        <div class="form-group">
          <label for="username" class="form-label">Имя пользователя</label>
          <input
            id="username"
            v-model="username"
            type="text"
            class="form-input"
            :class="{ error: usernameError }"
            placeholder="Введите имя пользователя"
            autocomplete="username"
          />
          <span v-if="usernameError" class="form-error">{{ usernameError }}</span>
        </div>

        <!-- Пароль -->
        <div class="form-group">
          <label for="password" class="form-label">Пароль</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="form-input"
            :class="{ error: passwordError }"
            placeholder="Введите пароль"
            autocomplete="new-password"
          />
          <span v-if="passwordError" class="form-error">{{ passwordError }}</span>
        </div>

        <!-- Подтверждение пароля -->
        <div class="form-group">
          <label for="confirmPassword" class="form-label">Подтверждение пароля</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            class="form-input"
            :class="{ error: confirmPasswordError }"
            placeholder="Повторите пароль"
            autocomplete="new-password"
          />
          <span v-if="confirmPasswordError" class="form-error">{{ confirmPasswordError }}</span>
        </div>

        <!-- Кнопка отправки -->
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="isSubmitting || authStore.loading"
        >
          {{ isSubmitting || authStore.loading ? 'Регистрация...' : 'Зарегистрироваться' }}
        </button>

        <!-- Ссылка на вход -->
        <div class="form-link">
          Уже есть аккаунт? <router-link to="/login">Войти</router-link>
        </div>
      </form>
    </GlassCard>
  </div>
</template>

<style scoped>
/* Дополнительные стили для страницы регистрации */
</style>

