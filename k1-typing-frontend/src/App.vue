<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// Проверяем аутентификацию при загрузке приложения
onMounted(() => {
  authStore.checkAuth()
})
</script>

<template>
  <RouterView />
</template>

<style>
@import '@/assets/styles/global.css';
</style>
