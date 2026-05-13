<template>
  <header class="header">
    <!-- 顶部登录条 -->
    <div class="top-bar">
      <div class="inner top-inner">
        <div class="welcome">欢迎您来到招投标信息公示平台</div>
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
          <div v-else class="guest-actions">
            <router-link to="/login" class="btn-text">登录</router-link>
            <span class="divider">|</span>
            <router-link to="/register" class="btn-text">注册</router-link>
          </div>
        </div>
      </div>
    </div>
    <div class="nav-bar">
      <div class="inner nav-inner">
        <nav class="nav">
          <router-link to="/list" active-class="active" class="menu-link">
            <span>招标公告</span>
          </router-link>
        </nav>
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
    router.replace({ path: '/list' })
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
}

/* 顶部条 */
.top-bar {
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  height: 36px;
  font-size: 0.85rem;
  color: #666;
}

.top-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.welcome {
  color: #666;
}

.actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.guest-actions {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.divider {
  color: #ddd;
  font-size: 0.8rem;
}

.btn-text {
  color: #666 !important;
  text-decoration: none !important;
  transition: color 0.2s;
}

.btn-text:hover {
  color: #1a5fb4 !important;
}

.user-menu {
  position: relative;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  background: transparent;
  border: none;
  color: #666;
  padding: 0;
}

.user-trigger:hover {
  color: #1a5fb4;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 0.6rem;
  transition: transform 0.2s;
}

.caret.open {
  transform: rotate(180deg);
}

.menu-panel {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 120px;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-radius: 4px;
  z-index: 100;
  padding: 0.5rem 0;
}

.menu-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 0.5rem 1rem;
  color: #333;
  text-decoration: none;
  font-size: 0.9rem;
  background: transparent;
  border: none;
}

.menu-item:hover {
  background: #f8f8f8;
  color: #1a5fb4;
}

/* 主头部 */
.main-header {
  height: 100px;
}

.main-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.brand {
  display: flex;
  align-items: center;
  gap: 1rem;
  text-decoration: none;
}

.brand:hover {
  text-decoration: none;
}

.logo {
  border-radius: 4px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 1.8rem;
  font-weight: bold;
  color: #1a5fb4;
  letter-spacing: 2px;
}

/* 导航栏 */
.nav-bar {
  background: #1a5fb4;
  height: 46px;
}

.nav-inner {
  height: 100%;
  display: flex;
  align-items: center;
}

.nav {
  display: flex;
  height: 100%;
}

.menu-link {
  display: inline-flex;
  align-items: center;
  padding: 0 2rem;
  color: #fff !important;
  text-decoration: none !important;
  font-size: 1.05rem;
  font-weight: 500;
  height: 100%;
  transition: background 0.2s;
}

.menu-link:hover,
.menu-link.active {
  background: #0d3d7a;
}
</style>
