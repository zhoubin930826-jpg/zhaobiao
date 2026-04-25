<template>
  <div class="list-page">
    <header class="page-hero">
      <h1>招标公告</h1>
      <p class="page-desc">按类型、地区与关键词筛选，数据来自门户招标接口。</p>
    </header>

    <section class="filters card-surface" aria-label="筛选条件">
      <div class="filters-title">筛选条件</div>
      <div class="filters-row">
        <label class="field">
          <span class="field-label">类型</span>
          <select v-model="category">
            <option v-for="c in tenderCategories" :key="c.value || 'all'" :value="c.value">
              {{ c.label }}
            </option>
          </select>
        </label>
        <label class="field">
          <span class="field-label">地区</span>
          <select v-model="region">
            <option v-for="r in tenderRegions" :key="r.value || 'all'" :value="r.value">
              {{ r.label }}
            </option>
          </select>
        </label>
        <label class="field grow">
          <span class="field-label">关键词</span>
          <input v-model="keyword" type="search" placeholder="标题或采购单位" autocomplete="off" />
        </label>
        <button type="button" class="reset" @click="reset">重置筛选</button>
      </div>
    </section>

    <p v-if="loading" class="state state-loading">正在加载公告…</p>
    <p v-else-if="error" class="state state-error">{{ error }}</p>
    <p v-else-if="!filtered.length" class="state state-empty">暂无符合条件的公告，可尝试调整筛选或关键词。</p>
    <div v-else class="list-stack">
      <p class="result-tip">
        <span class="result-pill">为您找到 <strong>{{ total }}</strong> 条公告</span>
      </p>
      <div class="grid">
        <TenderCard v-for="item in filtered" :key="item.id" :item="item" />
      </div>

      <div class="pagination" aria-label="分页">
        <button type="button" class="page-btn" :disabled="pageNum <= 1" @click="goToPage(pageNum - 1)">
          上一页
        </button>

        <div class="page-numbers" role="group" aria-label="页码">
          <button
            v-for="p in visiblePages"
            :key="p"
            type="button"
            class="page-number"
            :class="{ active: p === pageNum }"
            @click="goToPage(p)"
          >
            {{ p }}
          </button>
        </div>

        <button type="button" class="page-btn" :disabled="pageNum >= totalPages" @click="goToPage(pageNum + 1)">
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TenderCard from '@/components/TenderCard.vue'
import { listPortalTenders } from '@/api/portal'
import { useAuth } from '@/auth'

/** 与后台 `frontend/src/pages/sys/tender/index.vue` 中 regionOptions 保持一致 */
const TENDER_REGION_OPTIONS = [
  '北京',
  '上海',
  '广东',
  '浙江',
  '江苏',
  '山东',
  '四川',
  '湖北',
  '福建',
  '湖南'
]

const tenderRegions = [
  { value: '', label: '全部地区' },
  ...TENDER_REGION_OPTIONS.map((name) => ({ value: name, label: name }))
]

const route = useRoute()
const router = useRouter()
const { isLoggedIn } = useAuth()
const loading = ref(false)
const error = ref('')
const tenders = ref([])
const tenderCategories = ref([{ value: '', label: '全部类型' }])

const category = ref(route.query.category || '')
const region = ref(route.query.region || '')
const keyword = ref(route.query.q || '')

const pageNum = ref(Number(route.query.pageNum) || 1)
const pageSize = ref(Number(route.query.pageSize) || 10)
const total = ref(0)
const totalPages = ref(0)

const visiblePages = computed(() => {
  if (totalPages.value <= 1) return [1]
  const pages = []
  const start = Math.max(1, pageNum.value - 2)
  const end = Math.min(totalPages.value, pageNum.value + 2)
  for (let p = start; p <= end; p++) pages.push(p)
  return pages
})

function buildQueryPatch() {
  const q = {}
  if (category.value) q.category = category.value
  if (region.value) q.region = region.value
  if (keyword.value.trim()) q.q = keyword.value.trim()
  if (pageNum.value && pageNum.value !== 1) q.pageNum = String(pageNum.value)
  if (pageSize.value && pageSize.value !== 10) q.pageSize = String(pageSize.value)
  return q
}

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const res = await listPortalTenders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      region: region.value || undefined
    })
    tenders.value = Array.isArray(res.list) ? res.list : []
    total.value = typeof res.total === 'number' ? res.total : 0
    totalPages.value = typeof res.totalPages === 'number' ? res.totalPages : 0
    const categorySet = new Set()
    tenders.value.forEach(item => {
      if (item.category) categorySet.add(item.category)
    })
    tenderCategories.value = [
      { value: '', label: '全部类型' },
      ...Array.from(categorySet).map(name => ({ value: name, label: name }))
    ]
  } catch (e) {
    error.value = (e && e.message) || '加载公告失败'
    tenders.value = []
  } finally {
    loading.value = false
  }
}

function syncQuery() {
  router.replace({ query: buildQueryPatch() })
}

function goToPage(n) {
  const next = Math.max(1, Math.min(totalPages.value || 1, n))
  if (next === pageNum.value) return
  pageNum.value = next
  syncQuery()
  loadList()
}

watch([category, region, keyword], ([newCategory, newRegion, newKeyword], [oldCategory, oldRegion, oldKeyword]) => {
  // 切类型：只更新 URL query（列表分页不受影响，由前端 filtered 过滤）
  // 切地区/关键词：需要重置到第一页并重新请求
  if (newRegion !== oldRegion || newKeyword !== oldKeyword) {
    pageNum.value = 1
    syncQuery()
    loadList()
    return
  }

  if (newCategory !== oldCategory) {
    syncQuery()
  }
})

const filtered = computed(() =>
  tenders.value.filter(item => !category.value || item.category === category.value)
)

function reset() {
  category.value = ''
  region.value = ''
  keyword.value = ''
  pageNum.value = 1
}

onMounted(() => {
  if (isLoggedIn.value) {
    syncQuery()
    loadList()
  }
})
</script>

<style scoped>
.list-page {
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
  opacity: 0.9;
}

.page-hero h1 {
  margin: 0 0 0.5rem;
  font-size: clamp(1.3rem, 3.2vw, 1.6rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-primary-dark);
  line-height: 1.25;
}

.page-desc {
  margin: 0;
  max-width: 40rem;
  color: var(--text-muted);
  font-size: 0.92rem;
  line-height: 1.55;
}

.card-surface {
  position: relative;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.04), 0 12px 32px -8px rgba(26, 95, 180, 0.08);
}

.card-surface::before {
  content: '';
  position: absolute;
  left: 1rem;
  right: 1rem;
  top: 0;
  height: 3px;
  border-radius: 0 0 6px 6px;
  background: linear-gradient(90deg, var(--color-primary-dark), var(--color-primary) 50%, #3b82f6);
  opacity: 0.85;
}

.filters {
  padding: 1.35rem 1.4rem 1.45rem;
  margin-bottom: 1.35rem;
  padding-top: 1.55rem;
  background: linear-gradient(165deg, #ffffff 0%, #f4f7fb 55%, #f8fafc 100%);
}

.filters-title {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--color-primary);
  margin-bottom: 0.95rem;
  padding-left: 0.65rem;
  border-left: 3px solid var(--color-primary);
  line-height: 1.3;
}

.filters-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 1.15rem;
  align-items: flex-end;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.78rem;
  font-weight: 600;
  color: #475569;
  letter-spacing: 0.02em;
}

.field.grow {
  flex: 1;
  min-width: 200px;
}

.field select,
.field input {
  min-height: 2.45rem;
  padding: 0.5rem 0.65rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  min-width: 148px;
  background: #f8fafc;
  color: var(--text);
  transition: border-color 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.field select:hover,
.field input:hover {
  border-color: #cbd5e1;
  background: #fff;
}

.field select:focus,
.field input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(26, 95, 180, 0.14);
}

.field.grow input {
  min-width: 100%;
}

.reset {
  min-height: 2.45rem;
  padding: 0 1.2rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff, #f1f5f9);
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--text-muted);
  transition: border-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
}

.reset:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.8) inset;
}

.reset:active {
  transform: translateY(1px);
}

.list-stack {
  animation: list-fade-in 0.35s ease;
}

@keyframes list-fade-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-tip {
  margin: 0 0 1rem;
}

.result-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.4rem 0.85rem;
  font-size: 0.82rem;
  color: #475569;
  font-weight: 500;
  background: linear-gradient(135deg, #f0f6ff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  border-left: 3px solid var(--color-primary);
}

.result-pill strong {
  color: var(--color-primary);
  font-weight: 700;
}

.grid {
  display: flex;
  flex-direction: column;
  gap: 1.05rem;
}

.state {
  text-align: center;
  margin: 0;
  padding: 2.85rem 1.5rem;
  border-radius: 14px;
  border: 1px solid var(--border);
  font-size: 0.94rem;
  line-height: 1.55;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

.state-loading {
  color: var(--text-muted);
  background: linear-gradient(180deg, #ffffff 0%, #f4f7fb 100%);
  animation: list-pulse 1.35s ease-in-out infinite;
}

@keyframes list-pulse {
  0%,
  100% {
    opacity: 0.72;
  }
  50% {
    opacity: 1;
  }
}

.state-error {
  color: #b91c1c;
  background: linear-gradient(180deg, #ffffff 0%, #fef2f2 100%);
  border-color: #fecaca;
}

.state-empty {
  color: var(--text-muted);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-style: dashed;
  border-color: #cbd5e1;
  max-width: 32rem;
  margin-left: auto;
  margin-right: auto;
}

@media (max-width: 640px) {
  .filters-row {
    flex-direction: column;
    align-items: stretch;
  }

  .field select,
  .field input,
  .reset {
    width: 100%;
    min-width: 0;
  }

  .reset {
    margin-top: 0.25rem;
  }
}

.pagination {
  margin-top: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-number {
  width: 2.2rem;
  height: 2.2rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text-muted);
  font-weight: 600;
  cursor: pointer;
}

.page-number.active {
  border-color: rgba(37, 99, 235, 0.65);
  background: rgba(37, 99, 235, 0.08);
  color: var(--color-primary-dark);
}

.page-btn {
  min-width: 5.2rem;
  padding: 0.55rem 0.9rem;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #ffffff 0%, #f1f5f9 100%);
  color: var(--text-muted);
  font-weight: 600;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
