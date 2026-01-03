<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import GlassCard from '@/components/GlassCard.vue'

const router = useRouter()
const authStore = useAuthStore()

// Обработка выхода
const handleLogout = async () => {
  const result = await authStore.logout()
  
  if (result.success) {
    router.push('/login')
  }
}
</script>

<template>
  <div class="bg-neutral page-container">
    <GlassCard class="home-container">
      <div class="home-content">
        <div class="success-icon">✓</div>
        <h1 class="home-title">Вход успешен!</h1>
        <p class="home-subtitle">
          Добро пожаловать, <strong>{{ authStore.username || 'Пользователь' }}</strong>
        </p>
        
        <div class="home-info">
          <p>Вы успешно авторизовались в системе K1 Typing.</p>
          <p class="mt-2">Здесь будет размещен клавиатурный тренажер.</p>
        </div>

        <button
          class="btn btn-secondary mt-3"
          @click="handleLogout"
          :disabled="authStore.loading"
        >
          {{ authStore.loading ? 'Выход...' : 'Выйти из системы' }}
        </button>
      </div>
    </GlassCard>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 600px;
  text-align: center;
}

.home-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  background-color: var(--success);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
  margin-bottom: 24px;
  font-weight: bold;
}

.home-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.home-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.home-subtitle strong {
  color: var(--turquoise);
  font-weight: 600;
}

.home-info {
  background-color: rgba(0, 181, 163, 0.05);
  border: 1px solid rgba(0, 181, 163, 0.2);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  width: 100%;
}

.home-info p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}
</style>

