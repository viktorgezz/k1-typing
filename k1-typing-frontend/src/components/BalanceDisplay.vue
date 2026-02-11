<script setup>
import { computed } from 'vue'

const props = defineProps({
  balance: {
    type: Number,
    default: 0,
  },
})

const formattedBalance = computed(() => {
  return props.balance.toLocaleString('ru-RU')
})
</script>

<template>
  <div class="balance-display">
    <div class="balance-icon">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <circle cx="12" cy="12" r="10"/>
      </svg>
    </div>
    <span class="balance-amount">{{ formattedBalance }}</span>
  </div>
</template>

<style scoped>
.balance-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(12px) saturate(120%);
  -webkit-backdrop-filter: blur(12px) saturate(120%);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.15);
}

.balance-display:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 215, 0, 0.5);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.25);
  transform: translateY(-1px);
}

.balance-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  color: #ffd700;
  filter: drop-shadow(0 0 4px rgba(255, 215, 0, 0.5));
}

.balance-icon svg {
  width: 100%;
  height: 100%;
}

.balance-amount {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}
</style>
