<template>
  <div class="list-page">
    <header class="page-title">
      <h1>招标公告</h1>
      <p>按类型、地区与关键词筛选，数据来自门户招标接口。</p>
    </header>

    <div class="filters card-surface">
      <label class="field">
        <span>类型</span>
        <select v-model="category">
          <option v-for="c in tenderCategories" :key="c.value || 'all'" :value="c.value">
            {{ c.label }}
          </option>
        </select>
      </label>
      <label class="field">
        <span>地区</span>
        <select v-model="region">
          <option v-for="r in tenderRegions" :key="r.value || 'all'" :value="r.value">
            {{ r.label }}
          </option>
        </select>
      </label>
      <label class="field grow">
        <span>关键词</span>
        <input v-model="keyword" type="search" placeholder="标题或采购单位" />
      </label>
      <button type="button" class="reset" @click="reset">重置</button>
    </div>

    <p v-if="loading" class="empty">正在加载公告...</p>
    <p v-else-if="error" class="empty">{{ error }}</p>
    <p v-else-if="!filtered.length" class="empty">暂无符合条件的公告。</p>
    <div v-else class="grid">
      <TenderCard v-for="item in filtered" :key="item.id" :item="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TenderCard from '@/components/TenderCard.vue'
import { listPortalTenders } from '@/api/portal'
import { useAuth } from '@/auth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn } = useAuth()
const loading = ref(false)
const error = ref('')
const tenders = ref([])
const tenderCategories = ref([{ value: '', label: '全部类型' }])
const tenderRegions = ref([{ value: '', label: '全部地区' }])

const category = ref(route.query.category || '')
const region = ref(route.query.region || '')
const keyword = ref(route.query.q || '')

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const res = await listPortalTenders({
      pageNum: 1,
      pageSize: 100,
      keyword: keyword.value || undefined,
      region: region.value || undefined
    })
    tenders.value = Array.isArray(res.list) ? res.list : []
    const categorySet = new Set()
    const regionSet = new Set()
    tenders.value.forEach(item => {
      if (item.category) categorySet.add(item.category)
      if (item.region && item.region !== '-') regionSet.add(item.region)
    })
    tenderCategories.value = [
      { value: '', label: '全部类型' },
      ...Array.from(categorySet).map(name => ({ value: name, label: name }))
    ]
    tenderRegions.value = [
      { value: '', label: '全部地区' },
      ...Array.from(regionSet).map(name => ({ value: name, label: name }))
    ]
  } catch (e) {
    error.value = (e && e.message) || '加载公告失败'
    tenders.value = []
  } finally {
    loading.value = false
  }
}

function syncQuery() {
  const q = {}
  if (category.value) q.category = category.value
  if (region.value) q.region = region.value
  if (keyword.value.trim()) q.q = keyword.value.trim()
  router.replace({ query: q })
}

watch([category, region, keyword], () => {
  syncQuery()
  loadList()
})

const filtered = computed(() =>
  tenders.value.filter(item => !category.value || item.category === category.value)
)

function reset() {
  category.value = ''
  region.value = ''
  keyword.value = ''
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadList()
  }
})
</script>

<style scoped>
.list-page {
  padding-top: 1.25rem;
}

.page-title h1 {
  margin: 0 0 0.35rem;
  font-size: 1.35rem;
}

.page-title p {
  margin: 0 0 1.25rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.card-surface {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  padding: 1rem 1.25rem;
  margin-bottom: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.field.grow {
  flex: 1;
  min-width: 180px;
}

.field select,
.field input {
  padding: 0.45rem 0.6rem;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 140px;
}

.field.grow input {
  min-width: 100%;
}

.reset {
  padding: 0.45rem 1rem;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  font-size: 0.9rem;
}

.reset:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty {
  text-align: center;
  padding: 2.5rem 1rem;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px dashed var(--border);
}

</style>
