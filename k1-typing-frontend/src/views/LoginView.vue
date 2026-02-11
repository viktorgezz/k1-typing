<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useBalanceStore } from '@/stores/balance'
import { useForm, useField } from 'vee-validate'
import * as yup from 'yup'
import GlassCard from '@/components/GlassCard.vue'

const router = useRouter()
const authStore = useAuthStore()
const balanceStore = useBalanceStore()

const errorMessage = ref('')

// Схема валидации на основе AuthenticationRequest.java
const schema = yup.object({
  name: yup
    .string()
    .required('Имя не должно быть пустым')
    .min(2, 'Минимальная длина имени должна быть 2')
    .max(255, 'Максимальная длина имени должна быть 255'),
  password: yup
    .string()
    .required('Пароль не должен быть пустым'),
})

// Настройка формы с валидацией
const { handleSubmit, isSubmitting } = useForm({
  validationSchema: schema,
})

// Поля формы
const { value: name, errorMessage: nameError } = useField('name')
const { value: password, errorMessage: passwordError } = useField('password')

// Обработка отправки формы
const onSubmit = handleSubmit(async (values) => {
  errorMessage.value = ''

  const result = await authStore.login(values)

  if (result.success) {
    // Загружаем баланс после успешного входа
    await balanceStore.fetchBalance()
    // Перенаправляем на главную страницу после успешного входа
    await router.push('/')
  } else {
    errorMessage.value = result.error || 'Неверное имя пользователя или пароль'
  }
})
</script>

<template>
  <div class="bg-gradient page-container">
    <GlassCard>
      <h1 class="form-title">Вход</h1>
      <p class="form-subtitle">Войдите в свой аккаунт</p>

      <!-- Сообщение об ошибке -->
      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <form @submit="onSubmit">
        <!-- Имя пользователя -->
        <div class="form-group">
          <label for="name" class="form-label">Имя пользователя</label>
          <input
            id="name"
            v-model="name"
            type="text"
            class="form-input"
            :class="{ error: nameError }"
            placeholder="Введите имя пользователя"
            autocomplete="username"
          />
          <span v-if="nameError" class="form-error">{{ nameError }}</span>
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
            autocomplete="current-password"
          />
          <span v-if="passwordError" class="form-error">{{ passwordError }}</span>
        </div>

        <!-- Кнопка отправки -->
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="isSubmitting || authStore.loading"
        >
          {{ isSubmitting || authStore.loading ? 'Вход...' : 'Войти' }}
        </button>

        <!-- Ссылка на регистрацию -->
        <div class="form-link">
          Нет аккаунта? <router-link to="/register">Зарегистрироваться</router-link>
        </div>
      </form>
    </GlassCard>
  </div>
</template>

<style scoped>
/* Дополнительные стили для страницы входа */
</style>

