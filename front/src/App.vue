<template>
  <div class="app-root" :class="{ 'app-root-bare': bare }">
    <AppHeader v-if="!bare" />
    <main class="main" :class="{ 'main-bare': bare }">
      <router-view />
    </main>
    <AppFooter v-if="!bare" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()
const bare = computed(() => route.meta.bare === true)
</script>

<style scoped>
.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}

.app-root-bare {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  background-color: #e4ebf4;
  background-image:
    radial-gradient(ellipse 130% 95% at 50% -28%, rgba(26, 95, 180, 0.32) 0%, transparent 58%),
    radial-gradient(ellipse 75% 58% at 108% 42%, rgba(59, 130, 246, 0.22) 0%, transparent 54%),
    radial-gradient(ellipse 70% 52% at -12% 72%, rgba(13, 61, 122, 0.18) 0%, transparent 50%),
    radial-gradient(ellipse 90% 45% at 50% 108%, rgba(148, 163, 184, 0.15) 0%, transparent 55%),
    linear-gradient(168deg, #fafcfe 0%, #eef3fb 32%, #e2eaf5 62%, #eef2f8 100%);
}

.app-root-bare::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.5) 1.2px, transparent 1.2px);
  background-size: 20px 20px;
  opacity: 0.65;
  mix-blend-mode: soft-light;
  pointer-events: none;
}

.app-root-bare::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 85% 75% at 50% 48%, transparent 42%, rgba(15, 23, 42, 0.045) 100%);
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
  background: radial-gradient(ellipse 90% 55% at 50% -8%, rgba(26, 95, 180, 0.06) 0%, transparent 52%),
    radial-gradient(ellipse 60% 40% at 100% 30%, rgba(59, 130, 246, 0.04) 0%, transparent 45%);
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
