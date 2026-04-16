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
    throw new Error(payload.message || `请求失败(${response.status})`)
  }
  if (payload && (payload.code === 0 || payload.code === 200 || payload.code === undefined)) {
    return payload.data !== undefined ? payload.data : payload
  }
  throw new Error(payload.message || '请求失败')
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

export async function portalLogin(username, password) {
  return request('/portal/auth/login', {
    method: 'POST',
    withAuth: false,
    data: { username, password }
  })
}

export async function getPortalProfile() {
  return request('/portal/auth/me')
}

export async function listPortalTenders(params) {
  const page = await request('/portal/tenders', { params })
  const list = Array.isArray(page.list) ? page.list.map(mapTenderListItem) : []
  return { ...page, list }
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
    throw new Error(payload.message || `下载失败(${response.status})`)
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
