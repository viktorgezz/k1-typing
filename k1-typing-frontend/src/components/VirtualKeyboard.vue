<script setup>
import { computed } from 'vue'

const props = defineProps({
  activeKey: {
    type: String,
    default: '',
  },
  language: {
    type: String,
    default: 'RU', // RU или ENG
  },
})

// Цвета для пальцев (тепловая карта)
const fingerColors = {
  pinkyLeft: '#F59E0B', // Жёлтый — мизинец левый
  ringLeft: '#10B981', // Зелёный — безымянный левый
  middleLeft: '#3B82F6', // Синий — средний левый
  indexLeft: '#EF4444', // Красный — указательный левый
  indexRight: '#EF4444', // Красный — указательный правый
  middleRight: '#3B82F6', // Синий — средний правый
  ringRight: '#10B981', // Зелёный — безымянный правый
  pinkyRight: '#F59E0B', // Жёлтый — мизинец правый
  thumbs: '#8B5CF6', // Фиолетовый — большие пальцы
}

// Раскладки клавиатуры
const layouts = {
  RU: [
    [
      { key: 'ё', finger: 'pinkyLeft' },
      { key: '1', finger: 'pinkyLeft' },
      { key: '2', finger: 'ringLeft' },
      { key: '3', finger: 'middleLeft' },
      { key: '4', finger: 'indexLeft' },
      { key: '5', finger: 'indexLeft' },
      { key: '6', finger: 'indexRight' },
      { key: '7', finger: 'indexRight' },
      { key: '8', finger: 'middleRight' },
      { key: '9', finger: 'ringRight' },
      { key: '0', finger: 'pinkyRight' },
      { key: '-', finger: 'pinkyRight' },
      { key: '=', finger: 'pinkyRight' },
    ],
    [
      { key: 'й', finger: 'pinkyLeft' },
      { key: 'ц', finger: 'ringLeft' },
      { key: 'у', finger: 'middleLeft' },
      { key: 'к', finger: 'indexLeft' },
      { key: 'е', finger: 'indexLeft' },
      { key: 'н', finger: 'indexRight' },
      { key: 'г', finger: 'indexRight' },
      { key: 'ш', finger: 'middleRight' },
      { key: 'щ', finger: 'ringRight' },
      { key: 'з', finger: 'pinkyRight' },
      { key: 'х', finger: 'pinkyRight' },
      { key: 'ъ', finger: 'pinkyRight' },
    ],
    [
      { key: 'ф', finger: 'pinkyLeft' },
      { key: 'ы', finger: 'ringLeft' },
      { key: 'в', finger: 'middleLeft' },
      { key: 'а', finger: 'indexLeft' },
      { key: 'п', finger: 'indexLeft' },
      { key: 'р', finger: 'indexRight' },
      { key: 'о', finger: 'indexRight' },
      { key: 'л', finger: 'middleRight' },
      { key: 'д', finger: 'ringRight' },
      { key: 'ж', finger: 'pinkyRight' },
      { key: 'э', finger: 'pinkyRight' },
    ],
    [
      { key: 'shift-left', finger: 'pinkyLeft', label: 'Shift', isShift: true, shiftSide: 'left' },
      { key: 'я', finger: 'pinkyLeft' },
      { key: 'ч', finger: 'ringLeft' },
      { key: 'с', finger: 'middleLeft' },
      { key: 'м', finger: 'indexLeft' },
      { key: 'и', finger: 'indexLeft' },
      { key: 'т', finger: 'indexRight' },
      { key: 'ь', finger: 'indexRight' },
      { key: 'б', finger: 'middleRight' },
      { key: 'ю', finger: 'ringRight' },
      { key: '.', finger: 'pinkyRight' },
      { key: 'shift-right', finger: 'pinkyRight', label: 'Shift', isShift: true, shiftSide: 'right' },
    ],
    [{ key: ' ', finger: 'thumbs', label: 'Пробел', wide: true }],
  ],
  ENG: [
    [
      { key: '`', finger: 'pinkyLeft' },
      { key: '1', finger: 'pinkyLeft' },
      { key: '2', finger: 'ringLeft' },
      { key: '3', finger: 'middleLeft' },
      { key: '4', finger: 'indexLeft' },
      { key: '5', finger: 'indexLeft' },
      { key: '6', finger: 'indexRight' },
      { key: '7', finger: 'indexRight' },
      { key: '8', finger: 'middleRight' },
      { key: '9', finger: 'ringRight' },
      { key: '0', finger: 'pinkyRight' },
      { key: '-', finger: 'pinkyRight' },
      { key: '=', finger: 'pinkyRight' },
    ],
    [
      { key: 'q', finger: 'pinkyLeft' },
      { key: 'w', finger: 'ringLeft' },
      { key: 'e', finger: 'middleLeft' },
      { key: 'r', finger: 'indexLeft' },
      { key: 't', finger: 'indexLeft' },
      { key: 'y', finger: 'indexRight' },
      { key: 'u', finger: 'indexRight' },
      { key: 'i', finger: 'middleRight' },
      { key: 'o', finger: 'ringRight' },
      { key: 'p', finger: 'pinkyRight' },
      { key: '[', finger: 'pinkyRight' },
      { key: ']', finger: 'pinkyRight' },
    ],
    [
      { key: 'a', finger: 'pinkyLeft' },
      { key: 's', finger: 'ringLeft' },
      { key: 'd', finger: 'middleLeft' },
      { key: 'f', finger: 'indexLeft' },
      { key: 'g', finger: 'indexLeft' },
      { key: 'h', finger: 'indexRight' },
      { key: 'j', finger: 'indexRight' },
      { key: 'k', finger: 'middleRight' },
      { key: 'l', finger: 'ringRight' },
      { key: ';', finger: 'pinkyRight' },
      { key: "'", finger: 'pinkyRight' },
    ],
    [
      { key: 'shift-left', finger: 'pinkyLeft', label: 'Shift', isShift: true, shiftSide: 'left' },
      { key: 'z', finger: 'pinkyLeft' },
      { key: 'x', finger: 'ringLeft' },
      { key: 'c', finger: 'middleLeft' },
      { key: 'v', finger: 'indexLeft' },
      { key: 'b', finger: 'indexLeft' },
      { key: 'n', finger: 'indexRight' },
      { key: 'm', finger: 'indexRight' },
      { key: ',', finger: 'middleRight' },
      { key: '.', finger: 'ringRight' },
      { key: '/', finger: 'pinkyRight' },
      { key: 'shift-right', finger: 'pinkyRight', label: 'Shift', isShift: true, shiftSide: 'right' },
    ],
    [{ key: ' ', finger: 'thumbs', label: 'Space', wide: true }],
  ],
}

const currentLayout = computed(() => layouts[props.language] || layouts.RU)

// Проверяем, заглавная ли буква
const isUpperCase = computed(() => {
  const char = props.activeKey
  if (!char || char.length !== 1) return false
  return char !== char.toLowerCase() && char === char.toUpperCase()
})

// Найти информацию о клавише для текущего символа
const activeKeyInfo = computed(() => {
  const char = props.activeKey.toLowerCase()
  for (const row of currentLayout.value) {
    for (const keyObj of row) {
      if (keyObj.key === char && !keyObj.isShift) {
        return keyObj
      }
    }
  }
  return null
})

// Определяем, какую сторону Shift нужно нажать (противоположную руке с буквой)
const activeShiftSide = computed(() => {
  if (!isUpperCase.value || !activeKeyInfo.value) return null
  
  const finger = activeKeyInfo.value.finger
  // Если буква нажимается левой рукой — используем правый Shift и наоборот
  if (finger.endsWith('Left')) {
    return 'right'
  } else if (finger.endsWith('Right')) {
    return 'left'
  }
  // Для thumbs (пробел) не нужен shift
  return null
})

const activeFinger = computed(() => activeKeyInfo.value?.finger || null)

function isKeyActive(keyObj) {
  // Для Shift — проверяем нужен ли он и та ли это сторона
  if (keyObj.isShift) {
    return isUpperCase.value && keyObj.shiftSide === activeShiftSide.value
  }
  // Для обычных клавиш
  return keyObj.key === props.activeKey.toLowerCase()
}

function getKeyColor(finger) {
  return fingerColors[finger] || '#6B7280'
}

// Экспортируем для использования в HandDiagram
defineExpose({
  activeFinger,
  activeShiftSide,
  isUpperCase,
})
</script>

<template>
  <div class="keyboard-container">
    <div class="keyboard">
      <div v-for="(row, rowIndex) in currentLayout" :key="rowIndex" class="keyboard-row">
        <div
          v-for="keyObj in row"
          :key="keyObj.key"
          class="key"
          :class="{
            active: isKeyActive(keyObj),
            wide: keyObj.wide,
            shift: keyObj.isShift,
          }"
          :style="{
            '--finger-color': getKeyColor(keyObj.finger),
            '--finger-color-light': getKeyColor(keyObj.finger) + '30',
          }"
        >
          <span class="key-label">{{ keyObj.label || keyObj.key }}</span>
          <span v-if="isKeyActive(keyObj)" class="key-indicator"></span>
        </div>
      </div>
    </div>

    <!-- Легенда -->
    <div class="keyboard-legend">
      <div class="legend-item">
        <span class="legend-color" :style="{ background: fingerColors.pinkyLeft }"></span>
        <span>Мизинец</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" :style="{ background: fingerColors.ringLeft }"></span>
        <span>Безымянный</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" :style="{ background: fingerColors.middleLeft }"></span>
        <span>Средний</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" :style="{ background: fingerColors.indexLeft }"></span>
        <span>Указательный</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" :style="{ background: fingerColors.thumbs }"></span>
        <span>Большой</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.keyboard-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.keyboard {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 20px;
  background: rgba(74, 55, 40, 0.85);
  border-radius: 16px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(74, 55, 40, 0.3);
}

.keyboard-row {
  display: flex;
  justify-content: center;
  gap: 5px;
}

.key {
  position: relative;
  min-width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--finger-color-light);
  border: 2px solid var(--finger-color);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  text-transform: uppercase;
  transition: all 0.15s ease;
  cursor: default;
  user-select: none;
}

.key.wide {
  min-width: 240px;
}

.key.shift {
  min-width: 70px;
  font-size: 12px;
}

.key.active {
  background: var(--finger-color);
  transform: scale(1.1);
  box-shadow: 0 0 20px var(--finger-color), 0 0 40px var(--finger-color);
  z-index: 10;
}

.key.active .key-indicator {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 0.8s ease infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.7;
  }
}

.key-label {
  position: relative;
  z-index: 1;
}

/* Легенда */
.keyboard-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  padding: 12px 20px;
  background: rgba(74, 55, 40, 0.6);
  border-radius: 12px;
  backdrop-filter: blur(8px);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 4px;
}

/* Адаптивность */
@media (max-width: 768px) {
  .keyboard {
    padding: 12px;
    gap: 4px;
  }

  .keyboard-row {
    gap: 3px;
  }

  .key {
    min-width: 28px;
    height: 36px;
    font-size: 11px;
    border-radius: 6px;
  }

  .key.wide {
    min-width: 140px;
  }

  .key.shift {
    min-width: 44px;
    font-size: 9px;
  }

  .keyboard-legend {
    gap: 10px;
    padding: 10px 16px;
  }

  .legend-item {
    font-size: 10px;
  }

  .legend-color {
    width: 10px;
    height: 10px;
  }
}
</style>
