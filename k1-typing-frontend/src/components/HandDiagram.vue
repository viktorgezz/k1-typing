<script setup>
import { computed } from 'vue'

const props = defineProps({
  activeFinger: {
    type: String,
    default: null, // pinkyLeft, ringLeft, middleLeft, indexLeft, indexRight, middleRight, ringRight, pinkyRight, thumbs
  },
  shiftSide: {
    type: String,
    default: null, // 'left' или 'right' — какой Shift нужно нажать
  },
})

// Цвета для пальцев (должны совпадать с VirtualKeyboard)
const fingerColors = {
  pinkyLeft: '#F59E0B',
  ringLeft: '#10B981',
  middleLeft: '#3B82F6',
  indexLeft: '#EF4444',
  indexRight: '#EF4444',
  middleRight: '#3B82F6',
  ringRight: '#10B981',
  pinkyRight: '#F59E0B',
  thumbs: '#8B5CF6',
}

// Определяем какой палец активен для каждой руки
const leftHandFingers = computed(() => ({
  pinky: props.activeFinger === 'pinkyLeft' || props.shiftSide === 'left',
  ring: props.activeFinger === 'ringLeft',
  middle: props.activeFinger === 'middleLeft',
  index: props.activeFinger === 'indexLeft',
  thumb: props.activeFinger === 'thumbs',
}))

const rightHandFingers = computed(() => ({
  pinky: props.activeFinger === 'pinkyRight' || props.shiftSide === 'right',
  ring: props.activeFinger === 'ringRight',
  middle: props.activeFinger === 'middleRight',
  index: props.activeFinger === 'indexRight',
  thumb: props.activeFinger === 'thumbs',
}))

function getFingerStyle(finger, hand) {
  const fingerKey = `${finger}${hand === 'left' ? 'Left' : 'Right'}`
  const color = finger === 'thumb' ? fingerColors.thumbs : fingerColors[fingerKey]
  const isActive =
    hand === 'left'
      ? leftHandFingers.value[finger]
      : rightHandFingers.value[finger]

  return {
    '--finger-color': color,
    '--finger-glow': isActive ? `0 0 20px ${color}, 0 0 40px ${color}` : 'none',
  }
}

function isFingerActive(finger, hand) {
  return hand === 'left'
    ? leftHandFingers.value[finger]
    : rightHandFingers.value[finger]
}
</script>

<template>
  <div class="hands-container">
    <!-- Левая рука -->
    <div class="hand left-hand">
      <svg viewBox="0 0 200 280" class="hand-svg">
        <!-- Ладонь -->
        <path
          class="palm"
          d="M40 180 Q30 140 50 100 L150 100 Q170 140 160 180 L160 240 Q160 270 130 270 L70 270 Q40 270 40 240 Z"
        />

        <!-- Мизинец -->
        <path
          class="finger pinky"
          :class="{ active: isFingerActive('pinky', 'left') }"
          :style="getFingerStyle('pinky', 'left')"
          d="M45 105 Q35 90 40 50 Q45 20 55 20 Q65 20 68 50 Q72 85 65 105 Z"
        />

        <!-- Безымянный -->
        <path
          class="finger ring"
          :class="{ active: isFingerActive('ring', 'left') }"
          :style="getFingerStyle('ring', 'left')"
          d="M70 105 Q65 70 70 35 Q75 10 85 10 Q95 10 98 35 Q102 70 95 105 Z"
        />

        <!-- Средний -->
        <path
          class="finger middle"
          :class="{ active: isFingerActive('middle', 'left') }"
          :style="getFingerStyle('middle', 'left')"
          d="M100 105 Q95 65 100 25 Q105 0 115 0 Q125 0 128 25 Q132 65 125 105 Z"
        />

        <!-- Указательный -->
        <path
          class="finger index"
          :class="{ active: isFingerActive('index', 'left') }"
          :style="getFingerStyle('index', 'left')"
          d="M130 105 Q125 75 130 40 Q135 15 145 15 Q155 15 158 40 Q162 75 155 105 Z"
        />

        <!-- Большой палец -->
        <path
          class="finger thumb"
          :class="{ active: isFingerActive('thumb', 'left') }"
          :style="getFingerStyle('thumb', 'left')"
          d="M160 140 Q180 130 190 150 Q200 175 185 195 Q170 210 155 195 Q145 180 155 160 Z"
        />
      </svg>
      <span class="hand-label">Левая</span>
    </div>

    <!-- Правая рука -->
    <div class="hand right-hand">
      <svg viewBox="0 0 200 280" class="hand-svg">
        <!-- Ладонь -->
        <path
          class="palm"
          d="M160 180 Q170 140 150 100 L50 100 Q30 140 40 180 L40 240 Q40 270 70 270 L130 270 Q160 270 160 240 Z"
        />

        <!-- Мизинец -->
        <path
          class="finger pinky"
          :class="{ active: isFingerActive('pinky', 'right') }"
          :style="getFingerStyle('pinky', 'right')"
          d="M155 105 Q165 90 160 50 Q155 20 145 20 Q135 20 132 50 Q128 85 135 105 Z"
        />

        <!-- Безымянный -->
        <path
          class="finger ring"
          :class="{ active: isFingerActive('ring', 'right') }"
          :style="getFingerStyle('ring', 'right')"
          d="M130 105 Q135 70 130 35 Q125 10 115 10 Q105 10 102 35 Q98 70 105 105 Z"
        />

        <!-- Средний -->
        <path
          class="finger middle"
          :class="{ active: isFingerActive('middle', 'right') }"
          :style="getFingerStyle('middle', 'right')"
          d="M100 105 Q105 65 100 25 Q95 0 85 0 Q75 0 72 25 Q68 65 75 105 Z"
        />

        <!-- Указательный -->
        <path
          class="finger index"
          :class="{ active: isFingerActive('index', 'right') }"
          :style="getFingerStyle('index', 'right')"
          d="M70 105 Q75 75 70 40 Q65 15 55 15 Q45 15 42 40 Q38 75 45 105 Z"
        />

        <!-- Большой палец -->
        <path
          class="finger thumb"
          :class="{ active: isFingerActive('thumb', 'right') }"
          :style="getFingerStyle('thumb', 'right')"
          d="M40 140 Q20 130 10 150 Q0 175 15 195 Q30 210 45 195 Q55 180 45 160 Z"
        />
      </svg>
      <span class="hand-label">Правая</span>
    </div>
  </div>
</template>

<style scoped>
.hands-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 40px;
  padding: 20px;
}

.hand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.hand-svg {
  width: 120px;
  height: 168px;
  filter: drop-shadow(0 4px 12px rgba(74, 55, 40, 0.3));
}

.palm {
  fill: rgba(255, 224, 195, 0.9);
  stroke: rgba(74, 55, 40, 0.3);
  stroke-width: 2;
}

.finger {
  fill: var(--finger-color);
  stroke: rgba(74, 55, 40, 0.4);
  stroke-width: 2;
  opacity: 0.6;
  transition: all 0.2s ease;
}

.finger.active {
  opacity: 1;
  filter: drop-shadow(var(--finger-glow));
  animation: fingerPulse 0.8s ease infinite;
}

@keyframes fingerPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.hand-label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Адаптивность */
@media (max-width: 768px) {
  .hands-container {
    gap: 20px;
    padding: 12px;
  }

  .hand-svg {
    width: 80px;
    height: 112px;
  }

  .hand-label {
    font-size: 11px;
  }
}
</style>
