<template>
  <div class="detail-page">
    <p v-if="loading" class="not-found">正在加载公告详情...</p>
    <p v-else-if="error" class="not-found">{{ error }}，<router-link to="/list">返回列表</router-link></p>
    <p v-else-if="!item" class="not-found">未找到该公告，<router-link to="/list">返回列表</router-link></p>

    <template v-else>
      <nav class="crumb">
        <router-link to="/list">招标公告</router-link>
        <span aria-hidden="true">/</span>
        <span class="current">详情</span>
      </nav>

      <article class="article card-surface">
        <div class="meta">
          <span class="tag">{{ item.category }}</span>
          <span class="region">{{ item.region }}</span>
          <span :class="['status', item.status === '进行中' ? 'open' : 'closed']">{{ item.status }}</span>
        </div>
        <h1>{{ item.title }}</h1>
        <dl class="facts">
          <div>
            <dt>采购单位</dt>
            <dd>{{ item.purchaser }}</dd>
          </div>
          <div>
            <dt>预算金额</dt>
            <dd>{{ item.budget }}</dd>
          </div>
          <div>
            <dt>发布时间</dt>
            <dd>{{ formatDate(item.publishAt) }}</dd>
          </div>
          <div>
            <dt>截止时间</dt>
            <dd>{{ formatDate(item.deadline) }}</dd>
          </div>
        </dl>
        <section v-if="item.attachments && item.attachments.length" class="block download-block">
          <h2>文件下载</h2>
          <p class="download-desc">招标文件及相关资料下载。</p>
          <div class="download-list">
            <button
              v-for="file in item.attachments"
              :key="file.attachmentId"
              class="btn-download"
              type="button"
              @click="handleDownload(file)"
            >
              ⬇ {{ file.fileName || `附件-${file.attachmentId}` }}
            </button>
          </div>
        </section>
        <section class="block">
          <h2>公告正文</h2>
          <div class="body">{{ item.content }}</div>
        </section>
        <router-link to="/list" class="back">← 返回列表</router-link>
      </article>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { downloadPortalAttachment, getPortalTenderDetail } from '@/api/portal'
import { formatDate } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const error = ref('')
const item = ref(null)

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    item.value = await getPortalTenderDetail(route.params.id)
  } catch (e) {
    error.value = (e && e.message) || '加载详情失败'
    item.value = null
  } finally {
    loading.value = false
  }
}

async function handleDownload(file) {
  if (!item.value || !file) return
  try {
    await downloadPortalAttachment(item.value.id, file.attachmentId, file.fileName || '')
  } catch (e) {
    window.alert((e && e.message) || '下载失败，请稍后重试')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  padding-top: 1.35rem;
  padding-bottom: 1rem;
}

.not-found {
  text-align: center;
  max-width: 26rem;
  margin: 2rem auto 0;
  padding: 2.5rem 1.35rem;
  color: var(--text-muted);
  font-size: 0.94rem;
  line-height: 1.6;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.05);
}

.not-found a {
  font-weight: 600;
}

.crumb {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-bottom: 1.1rem;
  padding: 0.45rem 1rem;
  font-size: 0.82rem;
  color: var(--text-muted);
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
}

.crumb a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
}

.crumb a:hover {
  text-decoration: underline;
}

.crumb .current {
  color: var(--text);
  font-weight: 600;
}

.article.card-surface {
  position: relative;
  padding: 2rem 1.65rem 2rem;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.04), 0 14px 36px -10px rgba(26, 95, 180, 0.1);
}

.article.card-surface::before {
  content: '';
  position: absolute;
  left: 1.25rem;
  right: 1.25rem;
  top: 0;
  height: 3px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, var(--color-primary-dark), var(--color-primary) 50%, #3b82f6);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 0.75rem;
  align-items: center;
  margin-bottom: 1rem;
}

.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
  background: linear-gradient(180deg, #eff6ff 0%, #e8f0fc 100%);
  color: var(--color-primary);
  font-weight: 600;
  border: 1px solid rgba(26, 95, 180, 0.12);
}

.region {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 500;
}

.status {
  font-size: 0.75rem;
  padding: 0.22rem 0.55rem;
  border-radius: 6px;
  font-weight: 600;
}

.status.open {
  background: #ecfdf3;
  color: #047857;
}

.status.closed {
  background: #f3f4f6;
  color: #6b7280;
}

.article h1 {
  margin: 0 0 1.35rem;
  font-size: clamp(1.15rem, 2.5vw, 1.48rem);
  line-height: 1.42;
  font-weight: 700;
  letter-spacing: -0.015em;
  color: var(--color-primary-dark);
}

.facts {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(158px, 1fr));
  gap: 0.65rem;
  margin: 0 0 1.5rem;
  padding: 0;
  border: none;
  font-size: 0.88rem;
}

.facts > div {
  padding: 0.8rem 0.95rem;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #eef2f7;
  border-radius: 10px;
}

.facts dt {
  margin: 0;
  color: var(--text-muted);
  font-weight: 500;
  font-size: 0.76rem;
}

.facts dd {
  margin: 0.28rem 0 0;
  font-weight: 600;
  color: var(--text);
  font-size: 0.92rem;
}

.block {
  margin-bottom: 1.5rem;
}

.block:last-of-type {
  margin-bottom: 0.5rem;
}

.block h2 {
  margin: 0 0 0.65rem;
  font-size: 1.02rem;
  font-weight: 700;
  color: var(--color-primary-dark);
  padding-left: 0.65rem;
  border-left: 3px solid var(--color-primary);
  line-height: 1.3;
}

.block p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.65;
  font-size: 0.94rem;
}

.body {
  white-space: pre-wrap;
  line-height: 1.75;
  font-size: 0.95rem;
  color: var(--text);
  padding: 1.1rem 1.2rem;
  background: #fafbfc;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.download-block {
  padding: 1.05rem 1.15rem 1.15rem;
  background: linear-gradient(135deg, #f0f6ff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  border-left: 3px solid var(--color-primary);
}

.download-block h2 {
  border-left: none;
  padding-left: 0;
  margin-bottom: 0.4rem;
}

.download-desc {
  margin: 0 0 0.85rem;
  font-size: 0.87rem;
  color: var(--text-muted);
  line-height: 1.55;
}

.btn-download {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.2rem;
  background: linear-gradient(180deg, #2563eb 0%, var(--color-primary) 45%, var(--color-primary-dark) 100%);
  color: #fff !important;
  text-decoration: none !important;
  border-radius: 9px;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(13, 61, 122, 0.25);
  transition: filter 0.15s ease, transform 0.12s ease;
}

.download-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
}

.btn-download:hover {
  filter: brightness(1.06);
}

.btn-download:active {
  transform: translateY(1px);
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  margin-top: 1.35rem;
  padding: 0.5rem 1.05rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--color-primary);
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  text-decoration: none !important;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.back:hover {
  border-color: var(--color-primary);
  background: #f8fafc;
}
</style>
