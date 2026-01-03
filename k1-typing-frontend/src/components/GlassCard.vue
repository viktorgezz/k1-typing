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
  backdrop-filter: blur(8px) saturate(162%);
  -webkit-backdrop-filter: blur(8px) saturate(162%);
  background-color: rgba(5, 35, 45, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.125);
}
</style>
