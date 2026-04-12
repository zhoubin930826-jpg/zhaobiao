<template>
  <header class="header">
    <div class="inner">
      <router-link to="/" class="brand">
        <span class="logo" aria-hidden="true" />
        <span class="name">招投标信息公示</span>
      </router-link>
      <nav class="nav">
        <router-link to="/" exact-active-class="active">首页</router-link>
        <router-link to="/list" active-class="active">招标公告</router-link>
      </nav>
      <div class="actions">
        <template v-if="isLoggedIn">
          <span class="user" :title="username">{{ username }}</span>
          <button type="button" class="btn-text" @click="onLogout">退出</button>
        </template>
        <router-link v-else to="/login" class="btn-login">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuth, logout } from '@/auth'

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
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #fff;
  box-shadow: var(--shadow);
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.25rem;
  height: 56px;
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
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
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
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.92);
  max-width: 7rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  color: rgba(255, 255, 255, 0.88);
  text-decoration: none;
  font-size: 0.95rem;
  padding: 0.35rem 0;
  border-bottom: 2px solid transparent;
}

.nav a:hover {
  color: #fff;
  text-decoration: none;
}

.nav a.active {
  color: #fff;
  border-bottom-color: #fff;
}
</style>
