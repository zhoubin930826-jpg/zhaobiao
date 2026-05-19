<template>
  <div class="register-page">
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
            <h1>会员注册</h1>
            <p class="tagline">招投标信息公示平台</p>
          </div>
        </header>

        <p class="hint">注册成为会员后，可浏览更多招标公告、查看详情与下载附件。</p>

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
            <span class="field-label">手机号</span>
            <input
              v-model="phone"
              type="tel"
              name="phone"
              autocomplete="tel"
              placeholder="请输入手机号"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">电子邮箱</span>
            <input
              v-model="email"
              type="email"
              name="email"
              autocomplete="email"
              placeholder="请输入电子邮箱"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">企业名称</span>
            <input
              v-model="companyName"
              type="text"
              name="companyName"
              autocomplete="organization"
              placeholder="请输入企业名称"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">联系人</span>
            <input
              v-model="contactPerson"
              type="text"
              name="contactPerson"
              autocomplete="name"
              placeholder="请输入联系人姓名"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">统一社会信用代码</span>
            <input
              v-model="unifiedSocialCreditCode"
              type="text"
              name="unifiedSocialCreditCode"
              autocomplete="off"
              placeholder="18 位数字或大写字母"
              maxlength="18"
              pattern="[0-9A-Z]{18}"
              title="请输入 18 位统一社会信用代码（数字或大写英文字母）"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">真实姓名</span>
            <input
              v-model="realName"
              type="text"
              name="realName"
              autocomplete="name"
              placeholder="选填"
            />
          </label>
          <label class="field">
            <span class="field-label">密码</span>
            <input
              v-model="password"
              type="password"
              name="password"
              autocomplete="new-password"
              placeholder="请输入密码"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">确认密码</span>
            <input
              v-model="confirmPassword"
              type="password"
              name="confirmPassword"
              autocomplete="new-password"
              placeholder="请再次输入密码"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">营业执照</span>
            <input
              ref="licenseInput"
              type="file"
              name="businessLicenseFile"
              class="file-input"
              accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">近三年业绩证明</span>
            <input
              ref="performanceInput"
              type="file"
              name="threeYearPerformanceFile"
              class="file-input"
              accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
              required
            />
          </label>
          <label class="field">
            <span class="field-label">验证码</span>
            <div class="captcha-group">
              <input
                v-model="captchaCode"
                type="text"
                name="captchaCode"
                autocomplete="off"
                placeholder="请输入验证码"
                required
              />
              <img
                :key="captchaId"
                :src="captchaUrl"
                alt="验证码"
                class="captcha-img"
                @click="refreshCaptcha"
                title="点击刷新验证码"
              />
            </div>
          </label>
          <label class="agreement">
            <input type="checkbox" v-model="agreed" required />
            <span>
              我同意
              <a href="#" @click.prevent="openTerms">服务条款</a>
              和
              <a href="#" @click.prevent="openPrivacy">隐私政策</a>
            </span>
          </label>
          <p v-if="error" class="error" role="alert">{{ error }}</p>
          <p v-if="success" class="success" role="alert">{{ success }}</p>
          <button type="submit" class="submit" :disabled="loading">
            <span class="submit-inner">
              {{ loading ? '注册中…' : '立即注册' }}
            </span>
          </button>
        </form>
        <div class="login-link">
          已有账号？ <router-link to="/login">立即登录</router-link>
        </div>
      </div>
      <p class="fine-print">请妥善保管账号信息，勿向他人泄露密码。</p>
    </div>

    <LegalDocModal
      v-model:visible="termsVisible"
      title="服务条款"
      :sections="serviceTermsSections"
    />
    <LegalDocModal
      v-model:visible="privacyVisible"
      title="隐私政策"
      :sections="privacyPolicySections"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { buildPortalCaptchaUrl, portalRegister } from '@/api/portal'
import LegalDocModal from '@/components/LegalDocModal.vue'
import { serviceTermsSections } from '@/content/service-terms'
import { privacyPolicySections } from '@/content/privacy-policy'

const logoUrl = `${import.meta.env.BASE_URL}logo.jpg`
const router = useRouter()

function createCaptchaId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`
}

const username = ref('')
const phone = ref('')
const email = ref('')
const realName = ref('')
const companyName = ref('')
const contactPerson = ref('')
const unifiedSocialCreditCode = ref('')
const captchaId = ref(createCaptchaId())
const captchaCode = ref('')
const password = ref('')
const confirmPassword = ref('')
const agreed = ref(false)
const termsVisible = ref(false)
const privacyVisible = ref(false)
const error = ref('')
const success = ref('')
const loading = ref(false)

const licenseInput = ref(null)
const performanceInput = ref(null)

const captchaUrl = computed(() => buildPortalCaptchaUrl('register', captchaId.value))

function refreshCaptcha() {
  captchaId.value = createCaptchaId()
  captchaCode.value = ''
}

function openTerms() {
  termsVisible.value = true
}

function openPrivacy() {
  privacyVisible.value = true
}

async function onSubmit() {
  error.value = ''
  success.value = ''

  if (!agreed.value) {
    error.value = '请先阅读并同意服务条款和隐私政策'
    return
  }

  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }

  const licenseFile = licenseInput.value && licenseInput.value.files ? licenseInput.value.files[0] : null
  const performanceFile =
    performanceInput.value && performanceInput.value.files ? performanceInput.value.files[0] : null
  if (!licenseFile || !performanceFile) {
    error.value = '请上传营业执照与近三年业绩证明文件'
    return
  }

  loading.value = true
  try {
    const fd = new FormData()
    fd.append('username', username.value.trim())
    fd.append('phone', phone.value.trim())
    fd.append('email', email.value.trim())
    fd.append('companyName', companyName.value.trim())
    fd.append('contactPerson', contactPerson.value.trim())
    fd.append('unifiedSocialCreditCode', unifiedSocialCreditCode.value.trim().toUpperCase())
    const rn = realName.value.trim()
    if (rn) {
      fd.append('realName', rn)
    }
    fd.append('password', password.value)
    fd.append('confirmPassword', confirmPassword.value)
    fd.append('captchaId', captchaId.value)
    fd.append('captchaCode', captchaCode.value.trim())
    fd.append('businessLicenseFile', licenseFile)
    fd.append('threeYearPerformanceFile', performanceFile)

    const reg = await portalRegister(fd)

    success.value =
      (reg && reg.message) ||
      '注册成功！请联系管理员开通账号权限（分配业务类型、设置有效期并启用账号）后再登录。'
    setTimeout(() => {
      router.push('/login')
    }, 3500)
  } catch (err) {
    error.value = (err && err.message) || '注册失败，请检查后重试'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  width: 100%;
  max-width: 480px;
  padding: 1rem 0.75rem 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: register-enter 0.45s ease;
  margin: 0 auto;
}

@keyframes register-enter {
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
  gap: 0.85rem;
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
  min-height: 2.35rem;
  padding: 0.45rem 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.95rem;
  background: #f8fafc;
  color: var(--text);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.field input.file-input {
  padding: 0.35rem 0.5rem;
  cursor: pointer;
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

.captcha-group {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}

.captcha-group input {
  flex: 1;
  min-width: 0;
}

.captcha-img {
  width: 110px;
  height: 2.35rem;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  object-fit: cover;
  background-color: #f8fafc;
  transition: opacity 0.2s;
}

.captcha-img:hover {
  opacity: 0.85;
}

.agreement {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 0.25rem;
}

.agreement input[type="checkbox"] {
  width: 1rem;
  height: 1rem;
  cursor: pointer;
  accent-color: var(--color-primary);
}

.agreement a {
  color: var(--color-primary);
  text-decoration: none;
}

.agreement a {
  cursor: pointer;
}

.agreement a:hover {
  text-decoration: underline;
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

.success {
  margin: -0.15rem 0 0;
  padding: 0.55rem 0.65rem;
  font-size: 0.84rem;
  color: #15803d;
  background: #f0fdf4;
  border-radius: 8px;
  border: 1px solid #bbf7d0;
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

.login-link {
  margin-top: 1.25rem;
  text-align: center;
  font-size: 0.88rem;
  color: #64748b;
}

.login-link a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
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
