<template>
  <div class="home-portal category-page">
    <div class="toolbar">
      <router-link :to="{ name: 'list' }" class="back-link">← 返回列表</router-link>
      <h1 v-if="categoryTitle" class="page-title">{{ categoryTitle }}</h1>
    </div>

    <section v-if="!authIsLoggedIn" class="portal-section login-hint">
      <p>查看该分类下的全部公告需登录会员账号。</p>
      <router-link :to="{ name: 'login', query: { redirect: redirectPath } }" class="login-btn">去登录</router-link>
    </section>

    <template v-else>
      <section class="portal-section">
        <div class="section-header">
          <h3>公告列表</h3>
          <span v-if="!loading" class="meta">共 {{ total }} 条</span>
        </div>
        <div v-if="loading" class="state">加载中…</div>
        <div v-else-if="errorMsg" class="state error">{{ errorMsg }}</div>
        <ul v-else class="text-list">
          <li v-for="item in list" :key="item.id">
            <router-link :to="{ name: 'detail', params: { id: item.id } }" class="item-link">
              <span class="dot"></span>
              <span class="title" :title="item.title">{{ item.title }}</span>
            </router-link>
            <span class="date">{{ formatDate(item.publishAt) }}</span>
          </li>
          <li v-if="!list.length" class="empty-tip">暂无数据</li>
        </ul>
      </section>

      <div  class="pager">
        <button type="button" class="pager-btn" :disabled="pageNum <= 1" @click="goPage(pageNum - 1)">上一页</button>
        <span class="pager-info">第 {{ pageNum }} / {{ totalPages }} 页</span>
        <button type="button" class="pager-btn" :disabled="pageNum >= totalPages" @click="goPage(pageNum + 1)">下一页</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPortalTenders } from '@/api/portal'
import { authState, authIsLoggedIn } from '@/auth'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const PAGE_SIZE = 15

const categoryTitle = computed(() => {
  const c = route.query.category
  return typeof c === 'string' && c.trim() ? c.trim() : ''
})

const pageNum = computed(() => {
  const p = Number(route.query.page)
  if (!Number.isFinite(p) || p < 1) return 1
  return Math.floor(p)
})

const redirectPath = computed(() => route.fullPath)

const list = ref([])
const total = ref(0)
const totalPages = ref(0)
const loading = ref(false)
const errorMsg = ref('')

async function load() {
  errorMsg.value = ''
  if (!categoryTitle.value) {
    router.replace({ name: 'list' })
    return
  }
  document.title = `${categoryTitle.value} · 分类公告 · 招投标信息公示`
  if (!authState.token) {
    list.value = []
    total.value = 0
    totalPages.value = 0
    return
  }
  loading.value = true
  try {
    const res = await listPortalTenders({
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
      businessTypeName: categoryTitle.value
    })
    list.value = Array.isArray(res.list) ? res.list : []
    total.value = Number(res.total) || 0
    const tp = Number(res.totalPages)
    totalPages.value = Number.isFinite(tp) && tp > 0 ? tp : (total.value > 0 ? 1 : 0)
  } catch (e) {
    list.value = []
    total.value = 0
    totalPages.value = 0
    errorMsg.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goPage(n) {
  if (n < 1) return
  if (totalPages.value > 0 && n > totalPages.value) return
  router.push({
    name: 'categoryTenders',
    query: {
      category: categoryTitle.value,
      ...(n > 1 ? { page: String(n) } : {})
    }
  })
}

watch([categoryTitle, pageNum, () => !!authState.token], load, { immediate: true })
</script>

<style scoped>
.category-page.home-portal {
  padding: 1.5rem 0 3rem;
  max-width: 1200px;
  margin: 0 auto;
}

.toolbar {
  margin-bottom: 1.25rem;
}

.back-link {
  display: inline-block;
  font-size: 0.9rem;
  color: #64748b;
  text-decoration: none;
  margin-bottom: 0.5rem;
}

.back-link:hover {
  color: #1a5fb4;
}

.page-title {
  margin: 0.35rem 0 0;
  font-size: 1.35rem;
  font-weight: bold;
  color: #1e293b;
}

.login-hint {
  padding: 2rem 1.5rem;
  text-align: center;
}

.login-hint p {
  margin: 0 0 1rem;
  color: #64748b;
}

.login-btn {
  display: inline-block;
  padding: 0.5rem 1.5rem;
  background: #1a5fb4;
  color: #fff !important;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 600;
}

.login-btn:hover {
  background: #0d3d7a;
}

.portal-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.section-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #1a5fb4;
  font-weight: bold;
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

.text-list {
  list-style: none;
  margin: 0;
  padding: 0.5rem 1.25rem;
}

.text-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px dashed #e2e8f0;
}

.text-list li:last-child {
  border-bottom: none;
}

.item-link {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  text-decoration: none;
  color: #334155;
  margin-right: 1rem;
}

.item-link:hover .title {
  color: #1a5fb4;
  text-decoration: underline;
}

.dot {
  display: inline-block;
  width: 4px;
  height: 4px;
  background: #cbd5e1;
  border-radius: 50%;
  margin-right: 0.6rem;
  flex-shrink: 0;
}

.item-link:hover .dot {
  background: #1a5fb4;
}

.title {
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.date {
  font-size: 0.85rem;
  color: #94a3b8;
  flex-shrink: 0;
  font-family: monospace;
}

.empty-tip {
  color: #94a3b8;
  font-size: 0.9rem;
  justify-content: center !important;
  padding: 2rem 0 !important;
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
  border-color: #1a5fb4;
  color: #1a5fb4;
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
