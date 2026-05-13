<template>
  <div class="setting-page">

    <section class="card-surface form-wrap" aria-label="账户信息设置">
      <p v-if="loading" class="state state-loading">正在加载账户信息...</p>
      <p v-else-if="error" class="state state-error">{{ error }}</p>
      <form v-else class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field-label">用户名称（只读）</span>
          <input :value="displayName" type="text" readonly />
        </label>

        <div class="grid">
          <label class="field">
            <span class="field-label">真实姓名</span>
            <input v-model.trim="form.realName" type="text" placeholder="请输入真实姓名" />
          </label>
          <label class="field">
            <span class="field-label">联系方式</span>
            <input v-model.trim="form.phone" type="text" placeholder="请输入联系方式" />
          </label>
          <label class="field">
            <span class="field-label">邮箱</span>
            <input v-model.trim="form.email" type="email" placeholder="请输入邮箱" />
          </label>
          <label class="field">
            <span class="field-label">联系人</span>
            <input v-model.trim="form.contactPerson" type="text" placeholder="请输入联系人" />
          </label>
          <label class="field">
            <span class="field-label">公司名称</span>
            <input v-model.trim="form.companyName" type="text" placeholder="请输入公司名称" />
          </label>
          <label class="field">
            <span class="field-label">统一社会信用代码</span>
            <input v-model.trim="form.unifiedSocialCreditCode" type="text" placeholder="请输入统一社会信用代码" />
          </label>
        </div>
        <div class="upload-grid">
          <div class="upload-block">
            <div class="upload-head">
              <span class="field-label">营业执照</span>
              <input type="file" class="file-input" @change="onBusinessLicenseSelect" :disabled="uploading" />
            </div>
            <div v-if="businessLicense" class="attachment-item">
              <div class="attachment-meta">
                <span class="file-name">{{ businessLicense.fileName || `文件#${businessLicense.fileId}` }}</span>
                <small v-if="businessLicense.fileSize" class="file-size">{{ formatSize(businessLicense.fileSize) }}</small>
              </div>
              <div class="attachment-actions">
                <button type="button" class="link-btn" @click="viewBusinessLicense">查看</button>
                <button type="button" class="link-btn" @click="removeBusinessLicense">移除</button>
              </div>
            </div>
            <p v-else class="upload-empty">未上传营业执照</p>
          </div>
          <div class="upload-block">
            <div class="upload-head">
              <span class="field-label">近三年业绩证明</span>
              <input type="file" class="file-input" @change="onPerformanceSelect" :disabled="uploading" />
            </div>
            <div v-if="performanceFile" class="attachment-item">
              <div class="attachment-meta">
                <span class="file-name">{{ performanceFile.fileName || `文件#${performanceFile.fileId}` }}</span>
                <small v-if="performanceFile.fileSize" class="file-size">{{ formatSize(performanceFile.fileSize) }}</small>
              </div>
              <div class="attachment-actions">
                <button type="button" class="link-btn" @click="viewPerformanceFile">查看</button>
                <button type="button" class="link-btn" @click="removePerformanceFile">移除</button>
              </div>
            </div>
            <p v-else class="upload-empty">未上传近三年业绩证明</p>
          </div>
        </div>
        <p v-if="uploadError" class="tips tips-error">{{ uploadError }}</p>

        <div class="readonly-grid">
          <div class="readonly-item">
            <span class="field-label">到期时间</span>
            <strong>{{ profileState.expiresAt || '-' }}</strong>
          </div>
          <div class="readonly-item">
            <span class="field-label">业务类型</span>
            <strong>{{ profileState.businessTypesText }}</strong>
          </div>
          <div class="readonly-item">
            <span class="field-label">首次登录时间</span>
            <strong>{{ profileState.firstLoginAt || '-' }}</strong>
          </div>
        </div>

        <p v-if="submitError" class="tips tips-error">{{ submitError }}</p>
        <p v-if="success" class="tips tips-success">账户信息已更新。</p>

        <div class="actions">
          <button type="submit" class="submit" :disabled="submitting">{{ submitting ? '保存中...' : '保存设置' }}</button>
        </div>
      </form>
    </section>

    <div v-if="viewer.open" class="preview-mask" @click.self="closeViewer">
      <div class="preview-dialog">
        <header class="preview-header">
          <span class="preview-title">{{ viewer.title }}</span>
          <button type="button" class="preview-close" @click="closeViewer">关闭</button>
        </header>
        <div class="preview-body">
          <img v-if="viewer.type.startsWith('image/')" :src="viewer.src" alt="文件预览" />
          <iframe v-else :src="viewer.src" frameborder="0"></iframe>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { fetchPortalProfileFile, getPortalProfile, updatePortalProfile, uploadPortalFiles } from '@/api/portal'
import { authState } from '@/auth'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const error = ref('')
const submitError = ref('')
const uploadError = ref('')
const success = ref(false)
const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  companyName: '',
  contactPerson: '',
  unifiedSocialCreditCode: ''
})
const businessLicense = ref(null)
const performanceFile = ref(null)
const profileState = reactive({
  expiresAt: '',
  businessTypesText: '-',
  firstLoginAt: ''
})
const viewer = reactive({
  open: false,
  src: '',
  title: '',
  type: ''
})
const displayName = computed(() => form.realName || form.username || '')
const formatSize = size => {
  if (!size || isNaN(size)) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
}

function fillForm(profile) {
  form.username = profile.username || ''
  form.realName = profile.realName || ''
  form.phone = profile.phone || ''
  form.email = profile.email || ''
  form.companyName = profile.companyName || ''
  form.contactPerson = profile.contactPerson || ''
  form.unifiedSocialCreditCode = profile.unifiedSocialCreditCode || ''
  businessLicense.value = profile.businessLicenseFileId
    ? {
        fileId: profile.businessLicenseFileId,
        fileName: profile.businessLicenseFileName || '营业执照',
        contentType: profile.businessLicenseContentType,
        fileSize: profile.businessLicenseFileSize
      }
    : null
  performanceFile.value = profile.threeYearPerformanceFileId
    ? {
        fileId: profile.threeYearPerformanceFileId,
        fileName: profile.threeYearPerformanceFileName || '近三年业绩证明',
        contentType: profile.threeYearPerformanceContentType,
        fileSize: profile.threeYearPerformanceFileSize
      }
    : null
  profileState.expiresAt = profile.expiresAt || ''
  profileState.businessTypesText = Array.isArray(profile.businessTypes) && profile.businessTypes.length
    ? profile.businessTypes.map(item => item.name).filter(Boolean).join('、')
    : '-'
  profileState.firstLoginAt = profile.firstLoginAt || ''
}

async function onBusinessLicenseSelect(event) {
  const files = Array.from((event && event.target && event.target.files) || [])
  if (!files.length) return
  uploadError.value = ''
  uploading.value = true
  try {
    const uploaded = await uploadPortalFiles([files[0]])
    if (uploaded.length && uploaded[0].fileId != null) {
      businessLicense.value = {
        fileId: uploaded[0].fileId,
        fileName: uploaded[0].fileName || files[0].name
      }
    }
  } catch (e) {
    uploadError.value = (e && e.message) || '营业执照上传失败'
  } finally {
    uploading.value = false
    if (event && event.target) event.target.value = ''
  }
}

function removeBusinessLicense() {
  businessLicense.value = null
}

function closeViewer() {
  if (viewer.src) {
    window.URL.revokeObjectURL(viewer.src)
  }
  viewer.open = false
  viewer.src = ''
  viewer.title = ''
  viewer.type = ''
}

async function previewProfileFile(file, fallbackTitle) {
  if (!file || !file.fileId) return
  try {
    const result = await fetchPortalProfileFile(file.fileId, file.fileName || fallbackTitle)
    const previewable =
      (result.contentType && result.contentType.startsWith('image/')) || result.contentType === 'application/pdf'

    if (!previewable) {
      const link = document.createElement('a')
      link.href = result.objectUrl
      link.download = result.filename || fallbackTitle
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(result.objectUrl)
      return
    }

    closeViewer()
    viewer.title = result.filename || fallbackTitle
    viewer.src = result.objectUrl
    viewer.type = result.contentType || ''
    viewer.open = true
  } catch (e) {
    window.alert((e && e.message) || '查看失败，请稍后重试')
  }
}

async function viewBusinessLicense() {
  await previewProfileFile(businessLicense.value, '营业执照')
}

async function onPerformanceSelect(event) {
  const files = Array.from((event && event.target && event.target.files) || [])
  if (!files.length) return
  uploadError.value = ''
  uploading.value = true
  try {
    const uploaded = await uploadPortalFiles([files[0]])
    if (uploaded.length && uploaded[0].fileId != null) {
      performanceFile.value = {
        fileId: uploaded[0].fileId,
        fileName: uploaded[0].fileName || files[0].name
      }
    }
  } catch (e) {
    uploadError.value = (e && e.message) || '文件上传失败'
  } finally {
    uploading.value = false
    if (event && event.target) event.target.value = ''
  }
}

function removePerformanceFile() {
  performanceFile.value = null
}

async function viewPerformanceFile() {
  await previewProfileFile(performanceFile.value, '近三年业绩证明')
}

async function loadProfile() {
  loading.value = true
  error.value = ''
  try {
    const profile = await getPortalProfile()
    fillForm(profile || {})
  } catch (e) {
    error.value = (e && e.message) || '加载账户信息失败'
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  submitError.value = ''
  success.value = false
  submitting.value = true
  try {
    const profile = await updatePortalProfile({
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      companyName: form.companyName,
      contactPerson: form.contactPerson,
      unifiedSocialCreditCode: form.unifiedSocialCreditCode,
      businessLicenseFileId: businessLicense.value ? businessLicense.value.fileId : null,
      threeYearPerformanceFileId: performanceFile.value ? performanceFile.value.fileId : null
    })
    fillForm(profile || {})
    authState.username = form.realName || form.username || authState.username
    success.value = true
  } catch (e) {
    submitError.value = (e && e.message) || '更新失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(loadProfile)
onBeforeUnmount(closeViewer)
</script>

<style scoped>
.setting-page {
  padding-top: 1.35rem;
  padding-bottom: 1.25rem;
}

.page-hero {
  position: relative;
  margin-bottom: 1.5rem;
  padding: 0.35rem 0 1.4rem 1rem;
  border-bottom: 1px solid rgba(226, 232, 240, 0.95);
}

.page-hero::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.25rem;
  bottom: 0.85rem;
  width: 4px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
}

.page-hero h1 {
  margin: 0 0 0.5rem;
  font-size: clamp(1.3rem, 3.2vw, 1.6rem);
  font-weight: 700;
  color: var(--color-primary-dark);
}

.page-desc {
  margin: 0;
  max-width: 40rem;
  color: var(--text-muted);
  font-size: 0.92rem;
  line-height: 1.55;
}

.card-surface {
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.04), 0 12px 32px -8px rgba(26, 95, 180, 0.08);
}

.form-wrap {
  padding: 1.35rem 1.4rem 1.45rem;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.95rem 1rem;
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
}

.field input {
  min-height: 2.45rem;
  padding: 0.5rem 0.65rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  background: #f8fafc;
  color: var(--text);
}

.field textarea {
  padding: 0.6rem 0.65rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  background: #f8fafc;
  color: var(--text);
  resize: vertical;
}

.field input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(26, 95, 180, 0.14);
}

.field textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(26, 95, 180, 0.14);
}

.field input[readonly] {
  cursor: not-allowed;
  background: #f1f5f9;
}

.readonly-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.75rem;
}

.readonly-item {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 0.75rem 0.85rem;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.readonly-item strong {
  color: #1f2937;
  font-size: 0.9rem;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 0.95rem 1rem;
}

.upload-block {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.75rem 0.85rem;
  background: #f8fafc;
}

.upload-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.55rem;
}

.file-input {
  max-width: 210px;
  font-size: 0.8rem;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
  padding: 0.45rem 0.55rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 0.86rem;
}

.link-btn {
  border: none;
  background: transparent;
  color: #1d4ed8;
  cursor: pointer;
  font-size: 0.82rem;
}

.upload-empty {
  margin: 0;
  font-size: 0.84rem;
  color: #64748b;
}

.state {
  text-align: center;
  margin: 0;
  padding: 2rem 1.2rem;
  border-radius: 12px;
  border: 1px solid var(--border);
}

.state-loading {
  color: var(--text-muted);
}

.state-error {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fef2f2;
}

.tips {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.45;
}

.tips-error {
  color: #b91c1c;
}

.tips-success {
  color: #047857;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.submit {
  min-width: 120px;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(180deg, #2563eb 0%, var(--color-primary) 40%, var(--color-primary-dark) 100%);
  cursor: pointer;
}

.submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.preview-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1.2rem;
  z-index: 999;
}

.preview-dialog {
  width: min(900px, 95vw);
  height: min(80vh, 720px);
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.preview-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 0.95rem;
}

.preview-close {
  border: none;
  background: #1d4ed8;
  color: #fff;
  padding: 0.35rem 0.85rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.preview-body {
  flex: 1;
  background: #0f172a;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.5rem;
}

.preview-body img,
.preview-body iframe {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border: none;
  background: #0f172a;
}
</style>
