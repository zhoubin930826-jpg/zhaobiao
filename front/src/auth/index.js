import { reactive, computed } from 'vue'
import { getPortalProfile, portalLogin } from '@/api/portal'

const STORAGE_KEY = 'zb_portal_auth'

export const authState = reactive({
  token: null,
  tokenType: 'Bearer',
  username: null,
  firstLoginAt: null
})

function persist() {
  if (authState.token) {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: authState.token,
        tokenType: authState.tokenType || 'Bearer',
        username: authState.username,
        firstLoginAt: authState.firstLoginAt
      })
    )
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function initAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    const p = JSON.parse(raw)
    if (p && p.token) {
      authState.token = p.token
      authState.tokenType = p.tokenType || 'Bearer'
      authState.username = p.username || '用户'
      authState.firstLoginAt = p.firstLoginAt || null
    }
  } catch (_) {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function isLoggedIn() {
  return !!authState.token
}

/** 全局复用，避免每处 useAuth() 新建 computed 导致部分 watch / 界面不同步 */
export const authIsLoggedIn = computed(() => !!authState.token)
export const authUsername = computed(() => authState.username)

/**
 * @returns {{ isLoggedIn: import('vue').ComputedRef<boolean>, username: import('vue').ComputedRef<string|null>, login: Function, logout: Function }}
 */
export function useAuth() {
  return {
    isLoggedIn: authIsLoggedIn,
    username: authUsername,
    login,
    logout
  }
}

export async function login(username, password, captchaId, captchaCode) {
  const u = String(username || '').trim()
  const p = String(password || '')
  const cid = String(captchaId || '').trim()
  const ccode = String(captchaCode || '').trim()
  if (!u || !p) {
    return { ok: false, message: '请输入用户名和密码' }
  }
  if (!cid || !ccode) {
    return { ok: false, message: '请输入验证码' }
  }
  try {
    const res = await portalLogin(u, p, cid, ccode)
    authState.token = res.token || ''
    authState.tokenType = res.tokenType || 'Bearer'
    authState.username = (res.user && res.user.username) || u
    authState.firstLoginAt = (res.user && res.user.firstLoginAt) || null
    persist()
    try {
      const profile = await getPortalProfile()
      authState.username = profile.username || authState.username
      authState.firstLoginAt = profile.firstLoginAt || authState.firstLoginAt
      persist()
    } catch (_) {
      // 忽略拉取用户信息失败，保持登录状态
    }
    return { ok: true, profileCompletionRequired: !!res.profileCompletionRequired }
  } catch (error) {
    return { ok: false, message: error && error.message ? error.message : '登录失败' }
  }
}

export function logout() {
  authState.token = null
  authState.username = null
  authState.firstLoginAt = null
  persist()
}
