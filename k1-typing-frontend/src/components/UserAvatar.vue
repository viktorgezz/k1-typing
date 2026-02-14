<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** Base64-encoded photo data (null = show fallback) */
  photo: {
    type: String,
    default: null,
  },
  /** MIME content type, e.g. 'image/png' */
  contentType: {
    type: String,
    default: 'image/png',
  },
  /** Size variant: 'lg' (profile) or 'sm' (contest) */
  size: {
    type: String,
    default: 'lg',
    validator: (v) => ['lg', 'sm'].includes(v),
  },
})

const imgSrc = computed(() => {
  if (!props.photo) return null
  return `data:${props.contentType};base64,${props.photo}`
})
</script>

<template>
  <div class="user-avatar" :class="[`avatar-${size}`]">
    <!-- Фото пользователя -->
    <img
      v-if="imgSrc"
      :src="imgSrc"
      alt="Аватар"
      class="avatar-img"
    />
    <!-- SVG-заглушка: силуэт клавиатуры -->
    <svg
      v-else
      class="avatar-fallback"
      viewBox="0 0 64 64"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="32" cy="32" r="32" fill="rgba(255,255,255,0.12)" />
      <!-- Корпус клавиатуры -->
      <rect x="12" y="22" width="40" height="22" rx="3" fill="rgba(255,255,255,0.25)" stroke="rgba(255,255,255,0.4)" stroke-width="1.2" />
      <!-- Ряд 1 -->
      <rect x="15" y="25" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="20.5" y="25" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="26" y="25" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="31.5" y="25" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="37" y="25" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="42.5" y="25" width="5.5" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <!-- Ряд 2 -->
      <rect x="15" y="29.5" width="5" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="21.5" y="29.5" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="27" y="29.5" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="32.5" y="29.5" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="38" y="29.5" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="43.5" y="29.5" width="4.5" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <!-- Ряд 3 -->
      <rect x="15" y="34" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="20.5" y="34" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="26" y="34" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="31.5" y="34" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="37" y="34" width="4" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <rect x="42.5" y="34" width="5.5" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
      <!-- Пробел -->
      <rect x="20" y="38.5" width="24" height="3" rx="0.6" fill="rgba(255,255,255,0.5)" />
    </svg>
  </div>
</template>

<style scoped>
.user-avatar {
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.avatar-lg {
  width: 120px;
  height: 120px;
}

.avatar-sm {
  width: 28px;
  height: 28px;
  border-width: 1.5px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  width: 80%;
  height: 80%;
}
</style>
