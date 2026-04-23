<template>
  <header class="header">
    <div class="inner">
      <router-link to="/" class="brand" aria-label="招投标信息公示 首页">
        <img class="logo" :src="logoUrl" width="36" height="36" alt="" />
        <span class="name">招投标信息公示</span>
      </router-link>
      <nav class="nav">
        <router-link to="/list" active-class="active">招标公告</router-link>
      </nav>
      <div class="actions">
        <template v-if="isLoggedIn">
          <span class="user" :title="username">{{ username }}</span>
          <button type="button" class="btn-text" @click="onLogout">退出</button>
        </template>
        <router-link v-else to="/" class="btn-login">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuth, logout } from '@/auth'

const logoUrl = `${import.meta.env.BASE_URL}logo.svg`

const route = useRoute()
const router = useRouter()
const { isLoggedIn, username } = useAuth()

function onLogout() {
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

.actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.user {
  font-size: 0.8rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  max-width: 7.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0.28rem 0.65rem;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
}

.btn-text {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
  font-size: 0.85rem;
  padding: 0.3rem 0.65rem;
  border-radius: 6px;
}

.btn-text:hover {
  background: rgba(255, 255, 255, 0.12);
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
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 0.92rem;
  font-weight: 500;
  padding: 0.42rem 1rem;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.nav a:hover {
  color: #fff;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.1);
}

.nav a.active {
  color: #fff;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.28);
}
</style>
