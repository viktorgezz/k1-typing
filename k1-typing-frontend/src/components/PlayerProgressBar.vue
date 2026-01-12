<script setup>
import { computed } from 'vue'

const props = defineProps({
  username: {
    type: String,
    required: true,
  },
  progress: {
    type: Number,
    default: 0,
  },
  isReady: {
    type: Boolean,
    default: false,
  },
  isFinished: {
    type: Boolean,
    default: false,
  },
  place: {
    type: String,
    default: null,
  },
  speed: {
    type: Number,
    default: 0,
  },
  accuracy: {
    type: Number,
    default: 0,
  },
  isCurrentUser: {
    type: Boolean,
    default: false,
  },
  showStats: {
    type: Boolean,
    default: false,
  },
})

// Определяем цвет по месту
const placeColors = {
  FIRST: '#FFD700',
  SECOND: '#C0C0C0',
  THIRD: '#CD7F32',
  WITHOUT_PLACE: '#8B7355',
}

const placeLabels = {
  FIRST: '🥇 1-е место',
  SECOND: '🥈 2-е место',
  THIRD: '🥉 3-е место',
  WITHOUT_PLACE: '—',
}

const barColor = computed(() => {
  if (props.isFinished && props.place) {
    return placeColors[props.place] || placeColors.WITHOUT_PLACE
  }
  if (props.isCurrentUser) {
    return 'var(--coral)'
  }
  return 'var(--lemon)'
})

const placeLabel = computed(() => {
  return props.place ? placeLabels[props.place] : ''
})
</script>

<template>
  <div class="player-progress" :class="{ 'current-user': isCurrentUser, finished: isFinished }">
    <div class="player-info">
      <div class="player-name">
        <span v-if="isCurrentUser" class="you-badge">Вы</span>
        <span class="username">{{ username }}</span>
        <span v-if="isReady && !showStats" class="ready-badge">✓ Готов</span>
        <span v-if="isFinished && place" class="place-badge" :style="{ color: placeColors[place] }">
          {{ placeLabel }}
        </span>
      </div>
      <div v-if="showStats" class="player-stats">
        <span class="stat">{{ speed }} сим/мин</span>
        <span class="stat-divider">•</span>
        <span class="stat">{{ accuracy.toFixed(1) }}%</span>
      </div>
    </div>

    <div class="progress-track">
      <div
        class="progress-fill"
        :style="{
          width: progress + '%',
          background: barColor,
        }"
      >
        <span v-if="progress > 10" class="progress-label">{{ progress }}%</span>
      </div>
      <span v-if="progress <= 10 && progress > 0" class="progress-label-outside">{{ progress }}%</span>
    </div>
  </div>
</template>

<style scoped>
.player-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s ease;
}

.player-progress.current-user {
  background: rgba(255, 160, 122, 0.15);
  border-color: rgba(255, 160, 122, 0.3);
}

.player-progress.finished {
  opacity: 0.9;
}

.player-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 24px;
}

.player-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.you-badge {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-primary);
  background: var(--coral);
  border-radius: 4px;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ready-badge {
  font-size: 12px;
  color: #4ade80;
}

.place-badge {
  font-size: 13px;
  font-weight: 600;
}

.player-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-divider {
  opacity: 0.5;
}

.progress-track {
  position: relative;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
}

.progress-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-primary);
}

.progress-label-outside {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}
</style>
