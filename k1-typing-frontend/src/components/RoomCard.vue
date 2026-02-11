<script setup>
defineProps({
  idContest: {
    type: Number,
    required: true,
  },
  titleExercise: {
    type: String,
    required: true,
  },
  language: {
    type: String,
    required: true,
  },
  currentPlayers: {
    type: Number,
    required: true,
  },
  maxPlayers: {
    type: Number,
    required: true,
  },
  createdAt: {
    type: String,
    required: true,
  },
})

defineEmits(['click'])

const languageLabel = {
  RU: 'Русский',
  ENG: 'English',
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'только что'
  if (diffMins < 60) return `${diffMins} мин назад`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)} ч назад`
  return date.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })
}
</script>

<template>
  <article class="room-card" @click="$emit('click')">
    <!-- Жидкое стекло с переливанием -->
    <div class="liquid-glass">
      <div class="glass-shine"></div>
      <div class="glass-refraction"></div>
    </div>

    <!-- Контент карточки -->
    <div class="card-content">
      <div class="card-header">
        <div class="card-icon">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
            <path d="M4 22h16" />
            <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
            <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
            <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
          </svg>
        </div>
        <div class="room-badge" :class="`language-${language.toLowerCase()}`">
          {{ languageLabel[language] || language }}
        </div>
      </div>

      <h3 class="card-title">{{ titleExercise }}</h3>

      <div class="card-footer">
        <div class="players-info">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="players-icon"
          >
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          <span class="players-count">
            {{ currentPlayers }} / {{ maxPlayers }}
          </span>
        </div>
        <span class="room-time">{{ formatDate(createdAt) }}</span>
      </div>
    </div>

    <!-- Hover эффект -->
    <div class="card-hover-effect"></div>
  </article>
</template>

<style scoped>
.room-card {
  position: relative;
  border-radius: 20px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
  perspective: 1000px;
}

.room-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow:
    0 25px 50px -12px rgba(74, 55, 40, 0.5),
    0 0 30px rgba(255, 160, 122, 0.15);
}

/* Жидкое стекло — тёмный стиль как у формы логина */
.liquid-glass {
  position: absolute;
  inset: 0;
  background-color: rgba(74, 55, 40, 0.666);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(74, 55, 40, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

/* Блик — всегда плавно скользит */
.glass-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 40%,
    rgba(255, 255, 255, 0.08) 45%,
    rgba(255, 255, 255, 0.14) 50%,
    rgba(255, 255, 255, 0.08) 55%,
    transparent 60%
  );
  animation: shine 15s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shine {
  0% {
    transform: translateX(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) rotate(45deg);
  }
}

/* Эффект преломления */
.glass-refraction {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse at 30% 0%,
    rgba(255, 200, 160, 0.1) 0%,
    transparent 50%
  );
  opacity: 0.6;
  pointer-events: none;
}

/* Контент карточки */
.card-content {
  position: relative;
  z-index: 1;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.card-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(255, 160, 122, 0.35);
}

.card-icon svg {
  width: 24px;
  height: 24px;
  color: #4a3728;
}

.room-badge {
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.room-badge.language-ru {
  background: linear-gradient(135deg, rgba(255, 160, 122, 0.25) 0%, rgba(237, 229, 116, 0.25) 100%);
  border-color: rgba(255, 160, 122, 0.35);
  color: var(--lemon);
}

.room-badge.language-eng {
  background: linear-gradient(135deg, rgba(255, 160, 122, 0.25) 0%, rgba(237, 229, 116, 0.25) 100%);
  border-color: rgba(255, 160, 122, 0.35);
  color: var(--lemon);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 auto;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.players-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.players-icon {
  width: 18px;
  height: 18px;
  opacity: 0.7;
}

.players-count {
  color: #fff;
  font-weight: 600;
}

.room-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* Hover эффект подсветки */
.card-hover-effect {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    rgba(255, 184, 140, 0.1) 0%,
    transparent 50%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: 20px;
}

.room-card:hover .card-hover-effect {
  opacity: 1;
}
</style>
