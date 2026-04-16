<template>
  <div class="detail-page">
    <p v-if="loading" class="not-found">正在加载公告详情...</p>
    <p v-else-if="error" class="not-found">{{ error }}，<router-link to="/list">返回列表</router-link></p>
    <p v-else-if="!item" class="not-found">未找到该公告，<router-link to="/list">返回列表</router-link></p>

    <template v-else>
      <nav class="crumb">
        <router-link to="/home">首页</router-link>
        <span aria-hidden="true">/</span>
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
          <h2>项目简介</h2>
          <p>{{ item.summary }}</p>
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
  padding-top: 1.25rem;
}

.not-found {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--text-muted);
}

.crumb {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.crumb a {
  color: var(--color-primary);
}

.crumb .current {
  color: var(--text);
}

.card-surface {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.75rem 1.5rem 2rem;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 0.75rem;
  margin-bottom: 1rem;
}

.tag {
  font-size: 0.75rem;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  background: #e8f0fc;
  color: var(--color-primary);
  font-weight: 500;
}

.region {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.status {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  font-weight: 500;
}

.status.open {
  background: #ecfdf3;
  color: #047857;
}

.status.closed {
  background: #f3f4f6;
  color: #6b7280;
}

h1 {
  margin: 0 0 1.25rem;
  font-size: clamp(1.15rem, 2.5vw, 1.45rem);
  line-height: 1.4;
}

.facts {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1rem;
  margin: 0 0 1.5rem;
  padding: 1rem 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  font-size: 0.9rem;
}

.facts dt {
  margin: 0;
  color: var(--text-muted);
  font-weight: normal;
  font-size: 0.8rem;
}

.facts dd {
  margin: 0.2rem 0 0;
  font-weight: 500;
}

.block {
  margin-bottom: 1.5rem;
}

.block h2 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
  color: var(--color-primary-dark);
}

.block p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.65;
  font-size: 0.95rem;
}

.body {
  white-space: pre-wrap;
  line-height: 1.7;
  font-size: 0.95rem;
  color: var(--text);
}

.download-desc {
  margin: 0 0 0.75rem;
  font-size: 0.88rem;
  color: var(--text-muted);
}

.btn-download {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.55rem 1.15rem;
  background: var(--color-primary);
  color: #fff !important;
  text-decoration: none !important;
  border-radius: 8px;
  font-size: 0.92rem;
  font-weight: 500;
  border: none;
  box-shadow: 0 2px 8px rgba(26, 95, 180, 0.25);
}

.download-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
}

.btn-download:hover {
  background: var(--color-primary-dark);
}

.back {
  display: inline-block;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
}
</style>
