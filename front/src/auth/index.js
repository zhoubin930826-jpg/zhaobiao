import { reactive, computed } from 'vue'

const STORAGE_KEY = 'zb_portal_auth'

/** 未登录时首页「最新」条数 */
export const GUEST_HOME_LATEST = 1
/** 未登录时列表最多展示条数 */
export const GUEST_LIST_LIMIT = 2

/** 演示账号（正式环境请改为对接后端登录接口） */
const DEMO_USER = 'demo'
const DEMO_PASS = 'demo123'

export const authState = reactive({
  token: null,
  username: null
})

function persist() {
  if (authState.token) {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ token: authState.token, username: authState.username })
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

export function login(username, password) {
  const u = String(username || '').trim()
  const p = String(password || '')
  if (u === DEMO_USER && p === DEMO_PASS) {
    authState.token = 'demo-token'
    authState.username = u
    persist()
    return { ok: true }
  }
  return { ok: false, message: '账号或密码错误（演示账号：demo / demo123）' }
}

export function logout() {
  authState.token = null
  authState.username = null
  persist()
}
