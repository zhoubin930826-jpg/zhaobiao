<template>
  <div class="login-page">
    <div class="bg-decor" aria-hidden="true">
      <span class="bg-veil" />
      <span class="blob blob-a" />
      <span class="blob blob-b" />
      <span class="blob blob-c" />
      <span class="arc" />
      <span class="grid-fade" />
    </div>

    <div class="panel-wrap">
      <div class="panel">
        <div class="panel-accent" aria-hidden="true" />
        <header class="panel-head">
          <img class="brand-mark" :src="logoUrl" width="52" height="52" alt="" />
          <div class="head-lines">
            <h1>会员登录</h1>
            <p class="tagline">招投标信息公示平台</p>
          </div>
        </header>

        <p class="hint">登录后可浏览招标公告、查看详情与下载附件。</p>

        <form class="form" @submit.prevent="onSubmit">
          <label class="field">
            <span class="field-label">用户名</span>
            <input
              v-model="username"
              type="text"
              name="username"
              autocomplete="username"
              placeholder="请输入用户名"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">密码</span>
            <input
              v-model="password"
              type="password"
              name="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              required
            />
          </label>
          <p v-if="error" class="error" role="alert">{{ error }}</p>
          <button type="submit" class="submit" :disabled="loading">
            <span class="submit-inner">
              {{ loading ? '登录中…' : '进入平台' }}
            </span>
          </button>
        </form>
      </div>
      <p class="fine-print">请妥善保管账号信息，勿向他人泄露密码。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login as doLogin, useAuth } from '@/auth'

const logoUrl = `${import.meta.env.BASE_URL}logo.svg`

const route = useRoute()
const router = useRouter()
const { isLoggedIn } = useAuth()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

watch(
  [isLoggedIn, () => route.path],
  () => {
    if (!isLoggedIn.value || route.path !== '/') return
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
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 1rem 0.75rem 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: login-enter 0.45s ease;
}

@keyframes login-enter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bg-decor {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    185deg,
    rgba(255, 255, 255, 0.42) 0%,
    rgba(255, 255, 255, 0) 28%,
    rgba(255, 255, 255, 0) 62%,
    rgba(248, 250, 252, 0.55) 100%
  );
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(68px);
}

.blob-a {
  width: min(78vw, 460px);
  height: min(78vw, 460px);
  background: radial-gradient(circle at 28% 32%, #7eb8ea, #1a5fb4 52%, transparent 72%);
  top: -14%;
  right: -20%;
  opacity: 0.38;
}

.blob-b {
  width: min(64vw, 400px);
  height: min(64vw, 400px);
  background: radial-gradient(circle at 72% 68%, #bfdbfe, #0d3d7a 48%, transparent 74%);
  bottom: -12%;
  left: -24%;
  opacity: 0.3;
}

.blob-c {
  width: min(48vw, 280px);
  height: min(48vw, 280px);
  background: radial-gradient(circle at 50% 50%, #dbeafe, transparent 65%);
  top: 38%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.55;
  filter: blur(48px);
}

.arc {
  position: absolute;
  left: 50%;
  bottom: -35%;
  width: 160%;
  max-width: 1200px;
  height: 55vh;
  max-height: 520px;
  transform: translateX(-50%);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22) 0%, rgba(255, 255, 255, 0) 72%);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-bottom: none;
  box-shadow: 0 -20px 60px rgba(26, 95, 180, 0.06);
}

.grid-fade {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(26, 95, 180, 0.055) 1px, transparent 1px);
  background-size: 18px 18px;
  mask-image: radial-gradient(ellipse 72% 65% at 50% 42%, #000 18%, transparent 78%);
  -webkit-mask-image: radial-gradient(ellipse 72% 65% at 50% 42%, #000 18%, transparent 78%);
  opacity: 0.85;
}

.panel-wrap {
  position: relative;
  z-index: 1;
  width: 100%;
}

.panel {
  position: relative;
  width: 100%;
  padding: 2rem 1.75rem 1.85rem;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, #ffffff 38%, #fafbfd 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 16px;
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.06),
    0 24px 48px -12px rgba(26, 95, 180, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset;
}

.panel-accent {
  position: absolute;
  left: 1.25rem;
  right: 1.25rem;
  top: 0;
  height: 4px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, var(--color-primary-dark), var(--color-primary) 45%, #3b82f6);
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 0.35rem;
  margin-bottom: 1.25rem;
}

.brand-mark {
  flex-shrink: 0;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(26, 95, 180, 0.2);
}

.head-lines {
  min-width: 0;
}

.panel-head h1 {
  margin: 0 0 0.2rem;
  font-size: 1.42rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-primary-dark);
  line-height: 1.25;
}

.tagline {
  margin: 0;
  font-size: 0.82rem;
  color: var(--text-muted);
  font-weight: 500;
}

.hint {
  margin: 0 0 1.35rem;
  padding: 0.75rem 0.9rem;
  font-size: 0.88rem;
  line-height: 1.55;
  color: var(--text-muted);
  background: linear-gradient(135deg, #f0f6ff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  border-left: 3px solid var(--color-primary);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.05rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  letter-spacing: 0.02em;
}

.field input {
  min-height: 2.55rem;
  padding: 0.55rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  background: #f8fafc;
  color: var(--text);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.field input::placeholder {
  color: #94a3b8;
}

.field input:hover {
  border-color: #cbd5e1;
  background: #fff;
}

.field input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(26, 95, 180, 0.14);
}

.error {
  margin: -0.15rem 0 0;
  padding: 0.55rem 0.65rem;
  font-size: 0.84rem;
  color: #b91c1c;
  background: #fef2f2;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.submit {
  margin-top: 0.35rem;
  width: 100%;
  padding: 0;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  background: linear-gradient(180deg, #2563eb 0%, var(--color-primary) 40%, var(--color-primary-dark) 100%);
  color: #fff;
  font-size: 0.98rem;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(13, 61, 122, 0.25), 0 8px 20px rgba(26, 95, 180, 0.22);
  transition: transform 0.12s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.submit-inner {
  display: block;
  padding: 0.72rem 1rem;
}

.submit:hover:not(:disabled) {
  filter: brightness(1.05);
  box-shadow: 0 4px 8px rgba(13, 61, 122, 0.28), 0 12px 28px rgba(26, 95, 180, 0.26);
}

.submit:active:not(:disabled) {
  transform: translateY(1px);
}

.submit:disabled {
  opacity: 0.72;
  cursor: not-allowed;
  filter: grayscale(0.08);
}

.fine-print {
  margin: 1.15rem 0 0;
  text-align: center;
  font-size: 0.75rem;
  color: #64748b;
  line-height: 1.45;
  letter-spacing: 0.02em;
}

@media (max-width: 380px) {
  .panel-head {
    flex-direction: column;
    text-align: center;
  }

  .head-lines {
    text-align: center;
  }
}
</style>
