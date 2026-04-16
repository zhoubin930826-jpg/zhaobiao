import { reactive, computed } from 'vue'
import { getPortalProfile, portalLogin } from '@/api/portal'

const STORAGE_KEY = 'zb_portal_auth'

export const authState = reactive({
  token: null,
  tokenType: 'Bearer',
  username: null
})

function persist() {
  if (authState.token) {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: authState.token,
        tokenType: authState.tokenType || 'Bearer',
        username: authState.username
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
    }
  } catch (_) {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function isLoggedIn() {
  return !!authState.token
}

/**
 * @returns {{ isLoggedIn: import('vue').ComputedRef<boolean>, username: import('vue').ComputedRef<string|null>, login: Function, logout: Function }}
 */
export function useAuth() {
  return {
    isLoggedIn: computed(() => !!authState.token),
    username: computed(() => authState.username),
    login,
    logout
  }
}

export async function login(username, password) {
  const u = String(username || '').trim()
  const p = String(password || '')
  if (!u || !p) {
    return { ok: false, message: '请输入用户名和密码' }
  }
  try {
    const res = await portalLogin(u, p)
    authState.token = res.token || ''
    authState.tokenType = res.tokenType || 'Bearer'
    authState.username = (res.user && res.user.username) || u
    persist()
    try {
      const profile = await getPortalProfile()
      authState.username = profile.username || authState.username
      persist()
    } catch (_) {
      // 忽略拉取用户信息失败，保持登录状态
    }
    return { ok: true }
  } catch (error) {
    return { ok: false, message: error && error.message ? error.message : '登录失败' }
  }
}

export function logout() {
  authState.token = null
  authState.username = null
  persist()
}
