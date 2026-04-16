<template>
  <div class="login-page">
    <div class="panel card-surface">
      <h1>登录</h1>
      <p class="hint">请先登录会员账号，登录后即可查看招标公告与附件下载。</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span>用户名</span>
          <input v-model="username" type="text" name="username" autocomplete="username" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            name="password"
            autocomplete="current-password"
            required
          />
        </label>
        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <button type="submit" class="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <router-link to="/" class="back">← 返回入口</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login as doLogin, useAuth } from '@/auth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn } = useAuth()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

watch(
  [isLoggedIn, () => route.name],
  () => {
    if (!isLoggedIn.value || route.name !== 'login') return
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect && redirect.startsWith('/') ? redirect : '/list')
  },
  { immediate: true }
)

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    const res = await doLogin(username.value, password.value)
    if (!res.ok) {
      error.value = res.message || '登录失败'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  padding: 2rem 1rem 3rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.card-surface {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.panel {
  width: 100%;
  max-width: 400px;
  padding: 1.75rem 1.5rem 2rem;
}

.panel h1 {
  margin: 0 0 0.5rem;
  font-size: 1.35rem;
}

.hint {
  margin: 0 0 1.25rem;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.field input {
  padding: 0.55rem 0.65rem;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 0.95rem;
}

.error {
  margin: 0;
  font-size: 0.85rem;
  color: #b91c1c;
}

.submit {
  margin-top: 0.25rem;
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 0.95rem;
  font-weight: 500;
}

.submit:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.back {
  display: inline-block;
  margin-top: 1rem;
  font-size: 0.9rem;
}
</style>
