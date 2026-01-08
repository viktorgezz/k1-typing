<script setup>
import { computed } from 'vue'

const props = defineProps({
  maxWidth: {
    type: String,
    default: '420px',
  },
  paddingX: {
    type: String,
    default: '48px',
  },
  paddingY: {
    type: String,
    default: '56px',
  },
})

const cardStyle = computed(() => ({
  maxWidth: props.maxWidth,
  paddingLeft: props.paddingX,
  paddingRight: props.paddingX,
  paddingTop: props.paddingY,
  paddingBottom: props.paddingY,
}))
</script>

<template>
  <div 
    class="glass-card w-full rounded-xl shadow-2xl"
    :style="cardStyle"
  >
    <slot></slot>
  </div>
</template>

<style scoped>
.glass-card {
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  background-color: rgba(74, 55, 40, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 8px 32px rgba(74, 55, 40, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}
</style>
