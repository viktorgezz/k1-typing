<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  author: {
    type: String,
    default: 'Неизвестный автор',
  },
})

defineEmits(['click'])
</script>

<template>
  <article class="exercise-card" @click="$emit('click')">
    <!-- Жидкое стекло с переливанием -->
    <div class="liquid-glass">
      <div class="glass-shine"></div>
      <div class="glass-refraction"></div>
    </div>

    <!-- Контент карточки -->
    <div class="card-content">
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
          <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20" />
          <polyline points="10 6 10 10 14 10" />
        </svg>
      </div>

      <h3 class="card-title">{{ title }}</h3>

      <div class="card-footer">
        <span class="card-author">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="author-icon"
          >
            <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          {{ author }}
        </span>
      </div>
    </div>

    <!-- Hover эффект -->
    <div class="card-hover-effect"></div>
  </article>
</template>

<style scoped>
.exercise-card {
  position: relative;
  border-radius: 20px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
  perspective: 1000px;
}

.exercise-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 25px 50px -12px rgba(74, 55, 40, 0.35);
}

/* Жидкое стекло эффект */
.liquid-glass {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.25) 0%,
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 184, 140, 0.15) 100%
  );
  backdrop-filter: blur(16px) saturate(200%);
  -webkit-backdrop-filter: blur(16px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
}

/* Блик на стекле */
.glass-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 40%,
    rgba(255, 255, 255, 0.25) 45%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0.25) 55%,
    transparent 60%
  );
  transform: rotate(45deg);
  transition: all 0.6s ease;
  pointer-events: none;
}

.exercise-card:hover .glass-shine {
  animation: shine 1.5s ease forwards;
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
    rgba(255, 224, 195, 0.2) 0%,
    transparent 50%
  );
  opacity: 0.8;
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

.card-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--coral) 0%, var(--lemon) 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 20px rgba(255, 160, 122, 0.35);
}

.card-icon svg {
  width: 24px;
  height: 24px;
  color: #4a3728;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
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
  border-top: 1px solid rgba(74, 55, 40, 0.1);
}

.card-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.author-icon {
  width: 16px;
  height: 16px;
  opacity: 0.7;
}

/* Hover эффект подсветки */
.card-hover-effect {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    rgba(255, 184, 140, 0.15) 0%,
    transparent 50%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: 20px;
}

.exercise-card:hover .card-hover-effect {
  opacity: 1;
}
</style>

