<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBalanceStore } from '@/stores/balance'

const authStore = useAuthStore()
const balanceStore = useBalanceStore()

// Инициализация при загрузке приложения
onMounted(() => {
  // Проверяем аутентификацию
  authStore.checkAuth()
  
  // Загружаем баланс если пользователь авторизован
  if (authStore.isAuthenticated) {
    balanceStore.fetchBalance()
  }
})
</script>

<template>
  <RouterView />
</template>

<style>
@import '@/assets/styles/global.css';
</style>
