<template>
  <header class="header">
    <div class="inner">
      <router-link to="/" class="brand" aria-label="招投标信息公示 首页">
        <img class="logo" :src="logoUrl" width="36" height="36" alt="" />
        <span class="name">招投标信息公示</span>
      </router-link>
      <nav class="nav">
        <router-link to="/list" active-class="active" class="menu-link">
          <span class="menu-dot" aria-hidden="true">▦</span>
          <span>招标公告</span>
        </router-link>
      </nav>
      <div class="actions">
        <div v-if="isLoggedIn" ref="menuRef" class="user-menu">
          <button type="button" class="user-trigger" @click="toggleMenu">
            <span class="user-name" :title="displayName">{{ displayName }}</span>
            <span class="caret" :class="{ open: menuOpen }">▼</span>
          </button>
          <div v-if="menuOpen" class="menu-panel">
            <router-link to="/setting" class="menu-item" @click="closeMenu">账户设置</router-link>
            <button type="button" class="menu-item menu-item-btn" @click="onLogout">退出登录</button>
          </div>
        </div>
        <router-link v-else to="/" class="btn-login">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth, logout } from '@/auth'
import { getPortalProfile } from '@/api/portal'

const logoUrl = `${import.meta.env.BASE_URL}logo.jpg`

const route = useRoute()
const router = useRouter()
const { isLoggedIn, username } = useAuth()
const displayName = ref('用户')
const menuOpen = ref(false)
const menuRef = ref(null)

async function loadDisplayName() {
  if (!isLoggedIn.value) {
    displayName.value = '用户'
    return
  }
  try {
    const profile = await getPortalProfile()
    displayName.value = profile.realName || profile.username || username.value || '用户'
  } catch (_) {
    displayName.value = username.value || '用户'
  }
}

watch(isLoggedIn, value => {
  if (!value) {
    menuOpen.value = false
    displayName.value = '用户'
    return
  }
  loadDisplayName()
}, { immediate: true })

watch(() => route.fullPath, () => {
  closeMenu()
})

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

function onDocClick(event) {
  if (!menuOpen.value || !menuRef.value) return
  if (!menuRef.value.contains(event.target)) {
    closeMenu()
  }
}

document.addEventListener('click', onDocClick)
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})

function onLogout() {
  closeMenu()
  logout()
  if (route.meta.requiresAuth) {
    router.replace({ path: '/' })
  }
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #1e6bc9 0%, var(--color-primary) 42%, var(--color-primary-dark) 100%);
  color: #fff;
  box-shadow: 0 4px 18px rgba(13, 61, 122, 0.22);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  color: inherit;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.05rem;
}

.brand:hover {
  text-decoration: none;
  opacity: 0.95;
}

.logo {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 9px;
  display: block;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
}

.nav {
  display: flex;
  gap: 1.5rem;
  flex: 1;
  justify-content: center;
}

.menu-link {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

.menu-dot {
  font-size: 0.82rem;
  opacity: 0.92;
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex-shrink: 0;
}

.user-menu {
  position: relative;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  max-width: 180px;
  padding: 0.36rem 0.7rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.34);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.23), rgba(255, 255, 255, 0.12));
  color: #fff;
  cursor: pointer;
}

.user-name {
  font-size: 0.84rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 0.65rem;
  opacity: 0.85;
  transition: transform 0.15s ease;
}

.caret.open {
  transform: rotate(180deg);
}

.menu-panel {
  position: absolute;
  right: 0;
  top: calc(100% + 0.45rem);
  min-width: 150px;
  padding: 0.35rem;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.14);
  z-index: 12;
}

.menu-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 0.5rem 0.6rem;
  border-radius: 8px;
  color: #1f2937;
  text-decoration: none;
  font-size: 0.86rem;
}

.menu-item:hover {
  background: #f1f5f9;
}

.menu-item-btn {
  border: none;
  background: transparent;
  cursor: pointer;
}

.btn-login {
  font-size: 0.9rem;
  color: #fff !important;
  text-decoration: none !important;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  font-weight: 500;
}

.btn-login:hover {
  background: rgba(255, 255, 255, 0.28);
  text-decoration: none !important;
}

.nav a {
  color: rgba(255, 255, 255, 0.94);
  text-decoration: none;
  font-size: 0.92rem;
  font-weight: 600;
  padding: 0.45rem 1rem;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.06));
  box-shadow: 0 2px 6px rgba(13, 61, 122, 0.18);
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.nav a:hover {
  color: #fff;
  text-decoration: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.1));
  border-color: rgba(255, 255, 255, 0.35);
}

.nav a.active {
  color: #fff;
  font-weight: 600;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.26), rgba(255, 255, 255, 0.13));
  border-color: rgba(255, 255, 255, 0.45);
  transform: translateY(-1px);
}
</style>
