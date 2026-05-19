const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
const AUTH_STORAGE_KEY = 'zb_portal_auth'

function readToken() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) return ''
    const parsed = JSON.parse(raw)
    return parsed && parsed.token ? parsed.token : ''
  } catch (_) {
    return ''
  }
}

async function request(path, options = {}) {
  const { method = 'GET', params, data, withAuth = true } = options
  const url = new URL(`${API_BASE_URL}${path}`, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        url.searchParams.set(key, String(value))
      }
    })
  }

  const headers = {}
  if (withAuth) {
    const token = readToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }
  if (data !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(url.toString(), {
    method,
    headers,
    body: data !== undefined ? JSON.stringify(data) : undefined
  })

  const payload = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(payload.message || `请求失败(${response.status})`)
    error.code = payload.code || response.status
    error.status = response.status
    throw error
  }
  if (payload && (payload.code === 0 || payload.code === 200 || payload.code === undefined)) {
    return payload.data !== undefined ? payload.data : payload
  }
  const error = new Error(payload.message || '请求失败')
  error.code = payload.code
  throw error
}

function mapStatus(status) {
  return status === 'PUBLISHED' ? '进行中' : '已截止'
}

export function mapTenderListItem(item) {
  return {
    id: String(item.id),
    title: item.title || '',
    category: item.businessType && item.businessType.name ? item.businessType.name : '未分类',
    region: item.region || '-',
    purchaser: item.tenderUnit || '-',
    budget: item.budget || '-',
    publishAt: item.publishAt || '',
    deadline: item.deadline || '',
    status: mapStatus(item.status),
    summary: item.summary || '',
    projectCode: item.projectCode || ''
  }
}

export function buildPortalCaptchaUrl(scene, captchaId) {
  const url = new URL(`${API_BASE_URL}/portal/auth/captcha`, window.location.origin)
  url.searchParams.set('scene', scene)
  url.searchParams.set('captchaId', captchaId)
  url.searchParams.set('_t', String(Date.now()))
  return url.toString()
}

/**
 * @param {FormData} formData — 字段名与后端 MemberRegisterRequest 一致（含 businessLicenseFile、threeYearPerformanceFile）
 */
export async function portalRegister(formData) {
  const response = await fetch(new URL(`${API_BASE_URL}/portal/auth/register`, window.location.origin).toString(), {
    method: 'POST',
    body: formData
  })
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(payload.message || `注册失败(${response.status})`)
  }
  if (payload && (payload.code === 0 || payload.code === 200)) {
    return {
      data: payload.data !== undefined ? payload.data : payload,
      message: payload.message || '注册成功，请等待管理员启用账号'
    }
  }
  throw new Error((payload && payload.message) || '注册失败')
}

export async function portalLogin(username, password, captchaId, captchaCode) {
  return request('/portal/auth/login', {
    method: 'POST',
    withAuth: false,
    data: { username, password, captchaId, captchaCode }
  })
}

export async function getPortalProfile() {
  return request('/portal/auth/me')
}

export async function updatePortalProfile(data) {
  return request('/portal/auth/profile', {
    method: 'PUT',
    data
  })
}

export async function uploadPortalFiles(files) {
  const list = Array.isArray(files) ? files : []
  if (!list.length) return []
  const formData = new FormData()
  list.forEach(file => formData.append('files', file))

  const headers = {}
  const token = readToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(new URL(`${API_BASE_URL}/portal/auth/profile/files`, window.location.origin).toString(), {
    method: 'POST',
    headers,
    body: formData
  })
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(payload.message || `上传失败(${response.status})`)
  }
  if (payload && (payload.code === 0 || payload.code === 200 || payload.code === undefined)) {
    return Array.isArray(payload.data) ? payload.data : []
  }
  throw new Error(payload.message || '上传失败')
}

export async function listPortalTenders(params) {
  const page = await request('/portal/tenders', { params })
  const list = Array.isArray(page.list) ? page.list.map(mapTenderListItem) : []
  return { ...page, list }
}

export async function listLatestPortalTenders() {
  const list = await request('/portal/tenders/latest', { withAuth: false })
  const items = Array.isArray(list) ? list.map(mapTenderListItem) : []
  return { list: items, total: items.length }
}

export async function getPortalTenderDetail(tenderId) {
  const data = await request(`/portal/tenders/${tenderId}`)
  return {
    id: String(data.id),
    title: data.title || '',
    category: data.businessType && data.businessType.name ? data.businessType.name : '未分类',
    region: data.region || '-',
    purchaser: data.tenderUnit || '-',
    budget: data.budget || '-',
    publishAt: data.publishAt || '',
    deadline: data.deadline || '',
    signupDeadline: data.signupDeadline || '',
    status: mapStatus(data.status),
    summary: data.summary || '',
    content: data.content || '',
    contactPerson: data.contactPerson || '-',
    contactPhone: data.contactPhone || '-',
    projectCode: data.projectCode || '-',
    canDownload: !!data.canDownload,
    attachments: Array.isArray(data.attachments) ? data.attachments : []
  }
}

export function buildAttachmentDownloadUrl(tenderId, attachmentId) {
  return `${API_BASE_URL}/portal/tenders/${tenderId}/attachments/${attachmentId}/download`
}

/** 与后端 FileThumbnailUrlBuilder 一致：公开缩略图，可用于资料文件预览（无需会员下载接口） */
export function buildPortalFileThumbnailUrl(fileId) {
  if (fileId === undefined || fileId === null) return ''
  return `${API_BASE_URL}/files/${fileId}/thumbnail`
}

function extractFilename(contentDisposition) {
  if (!contentDisposition) return ''
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match && utf8Match[1]) {
    return decodeURIComponent(utf8Match[1])
  }
  const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
  return plainMatch && plainMatch[1] ? plainMatch[1] : ''
}

export async function downloadPortalAttachment(tenderId, attachmentId, fallbackFilename = '') {
  const url = new URL(buildAttachmentDownloadUrl(tenderId, attachmentId), window.location.origin)
  const headers = {}
  const token = readToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(url.toString(), { method: 'GET', headers })
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}))
    const message =
      payload.message ||
      (response.status === 401
        ? '请登录后下载附件'
        : response.status === 403
          ? '当前账号暂无附件下载权限'
          : `下载失败(${response.status})`)
    const error = new Error(message)
    error.code = payload.code || response.status
    error.status = response.status
    throw error
  }

  const blob = await response.blob()
  const filename =
    extractFilename(response.headers.get('content-disposition')) ||
    fallbackFilename ||
    `attachment-${attachmentId}`
  const objectUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(objectUrl)
}
