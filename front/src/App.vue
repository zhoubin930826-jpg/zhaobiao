<template>
  <div class="app-root" :class="{ 'app-root-bare': bare }">
    <AppHeader v-if="!bare" />
    <main class="main" :class="{ 'main-bare': bare }">
      <router-view />
    </main>
    <AppFooter v-if="!bare" />
    <ContactFloat v-if="bare" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import ContactFloat from '@/components/ContactFloat.vue'

const route = useRoute()
const bare = computed(() => route.meta.bare === true)
</script>

<style scoped>
.app-root {
  position: relative;
  isolation: isolate;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
}

/* 门户主站：正式、克制的蓝灰渐变 + 淡网格纹理 */
.app-root:not(.app-root-bare)::before {
  content: '';
  position: fixed;
  inset: 0;
  z-index: -2;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(0, 78, 140, 0.07) 0%, rgba(0, 104, 183, 0.025) 140px, transparent 300px),
    linear-gradient(165deg, var(--bg-page-top) 0%, var(--bg-page-mid) 45%, var(--bg-page-bottom) 100%);
}

.app-root:not(.app-root-bare)::after {
  content: '';
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  opacity: 0.4;
  background-image:
    linear-gradient(rgba(0, 78, 140, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 78, 140, 0.035) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: radial-gradient(ellipse 85% 75% at 50% 35%, #000 15%, transparent 72%);
  -webkit-mask-image: radial-gradient(ellipse 85% 75% at 50% 35%, #000 15%, transparent 72%);
}

.app-root-bare {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  background-color: #e8edf3;
  background-image:
    linear-gradient(180deg, rgba(0, 78, 140, 0.06) 0%, transparent 220px),
    linear-gradient(168deg, #fafbfc 0%, #f2f5f9 38%, #e8edf3 100%);
}

.app-root-bare::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(0, 78, 140, 0.04) 1px, transparent 1px);
  background-size: 22px 22px;
  opacity: 0.5;
  pointer-events: none;
}

.app-root-bare::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 90% 80% at 50% 50%, transparent 50%, rgba(15, 23, 42, 0.03) 100%);
  pointer-events: none;
}

.main {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem 3rem;
}

.main:not(.main-bare) {
  background: transparent;
}

.main-bare {
  max-width: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.25rem 3rem;
}
</style>
