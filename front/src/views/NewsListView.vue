<template>
  <div class="home-portal news-list-page">
    <div class="toolbar">
      <router-link :to="{ name: 'list' }" class="back-link">← 返回首页</router-link>
      <h1 class="page-title">最新资讯</h1>
    </div>

    <section class="portal-section">
      <div class="section-header">
        <h3>资讯列表</h3>
        <span v-if="!loading" class="meta">共 {{ total }} 条</span>
      </div>
      <div v-if="loading" class="state">加载中…</div>
      <div v-else-if="errorMsg" class="state error">{{ errorMsg }}</div>
      <ul v-else-if="newsList.length" class="news-full-list">
        <li v-for="item in newsList" :key="item.id" class="news-full-item">
          <router-link :to="{ name: 'newsDetail', params: { id: item.id } }" class="news-full-link">
            <div class="news-full-head">
              <span class="news-type">{{ item.category }}</span>
              <span class="news-date">{{ formatDate(item.publishAt) }}</span>
            </div>
            <h2 class="news-full-title">{{ item.title }}</h2>
            <p class="news-full-summary">{{ item.summary }}</p>
            <span class="read-more">查看详情 →</span>
          </router-link>
        </li>
      </ul>
      <p v-else class="empty-tip">暂无资讯</p>
    </section>

    <div v-if="totalPages > 1" class="pager">
      <button type="button" class="pager-btn" :disabled="pageNum <= 1" @click="goPage(pageNum - 1)">上一页</button>
      <span class="pager-info">第 {{ pageNum }} / {{ totalPages }} 页</span>
      <button type="button" class="pager-btn" :disabled="pageNum >= totalPages" @click="goPage(pageNum + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPortalNews } from '@/api/portal'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const PAGE_SIZE = 10

const newsList = ref([])
const total = ref(0)
const totalPages = ref(0)
const pageNum = ref(1)
const loading = ref(false)
const errorMsg = ref('')

function readPageFromRoute() {
  const p = Number(route.query.page)
  pageNum.value = !Number.isFinite(p) || p < 1 ? 1 : Math.floor(p)
}

async function load() {
  readPageFromRoute()
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await listPortalNews({ pageNum: pageNum.value, pageSize: PAGE_SIZE })
    newsList.value = Array.isArray(res.list) ? res.list : []
    total.value = Number(res.total) || 0
    const tp = Number(res.totalPages)
    totalPages.value = Number.isFinite(tp) && tp > 0 ? tp : (total.value > 0 ? 1 : 0)
  } catch (e) {
    newsList.value = []
    total.value = 0
    totalPages.value = 0
    errorMsg.value = (e && e.message) || '加载失败'
  } finally {
    loading.value = false
  }
}

function goPage(n) {
  if (n < 1) return
  if (totalPages.value > 0 && n > totalPages.value) return
  router.push({
    name: 'newsList',
    query: n > 1 ? { page: String(n) } : {}
  })
}

onMounted(load)
watch(() => route.query.page, load)
</script>

<style scoped>
.home-portal {
  --section-color: #1a5fb4;
  --section-color-light: #eff6ff;
  --section-color-border: #bfdbfe;
  --section-header-bg: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);

  padding: 1.5rem 0 3rem;
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  margin-bottom: 1.25rem;
}

.back-link {
  display: inline-block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #64748b;
  text-decoration: none;
}

.back-link:hover {
  color: var(--section-color);
}

.page-title {
  margin: 0;
  font-size: 1.35rem;
  color: #1e293b;
  font-weight: 600;
}

.portal-section {
  background: #fff;
  border: 1px solid #dce4ee;
  border-radius: 6px;
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--section-color-border);
  background: var(--section-header-bg);
}

.section-header h3 {
  margin: 0;
  font-size: 1.05rem;
  color: var(--section-color);
  font-weight: bold;
  position: relative;
  padding-left: 0.85rem;
}

.section-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: var(--section-color);
  border-radius: 2px;
}

.meta {
  font-size: 0.85rem;
  color: #64748b;
}

.state {
  padding: 2rem 1.25rem;
  text-align: center;
  color: #64748b;
}

.state.error {
  color: #b91c1c;
}

.news-full-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.news-full-item {
  border-bottom: 1px solid #e2e8f0;
}

.news-full-item:last-child {
  border-bottom: none;
}

.news-full-link {
  display: block;
  padding: 1.1rem 1.25rem;
  text-decoration: none;
  color: inherit;
  transition: background 0.2s;
}

.news-full-link:hover {
  background: var(--section-color-light);
}

.news-full-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.news-type {
  display: inline-block;
  padding: 0.1rem 0.45rem;
  font-size: 0.75rem;
  color: var(--section-color);
  background: var(--section-color-light);
  border: 1px solid var(--section-color-border);
  border-radius: 4px;
}

.news-date {
  font-size: 0.8rem;
  color: #94a3b8;
  font-family: monospace;
}

.news-full-title {
  margin: 0 0 0.45rem;
  font-size: 1rem;
  font-weight: 600;
  line-height: 1.45;
  color: #1e293b;
  transition: color 0.2s;
}

.news-full-link:hover .news-full-title {
  color: var(--section-color);
}

.news-full-summary {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
  line-height: 1.65;
  color: #64748b;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.read-more {
  font-size: 0.8rem;
  color: var(--section-color);
}

.empty-tip {
  margin: 0;
  padding: 2.5rem 1rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.9rem;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
  flex-wrap: wrap;
}

.pager-btn {
  padding: 0.45rem 1rem;
  font-size: 0.9rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
  color: #334155;
  cursor: pointer;
}

.pager-btn:hover:not(:disabled) {
  border-color: var(--section-color);
  color: var(--section-color);
}

.pager-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pager-info {
  font-size: 0.9rem;
  color: #64748b;
}
</style>
