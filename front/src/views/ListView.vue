<template>
  <div class="list-page">
    <header class="page-title">
      <h1>招标公告</h1>
      <p>
        按类型、地区与关键词筛选（演示数据）。
        <template v-if="!isLoggedIn">未登录仅展示前 {{ GUEST_LIST_LIMIT }} 条，详情需登录查看。</template>
      </p>
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

    <p v-if="!filtered.length" class="empty">暂无符合条件的公告。</p>
    <div v-else class="grid">
      <TenderCard v-for="item in visibleList" :key="item.id" :item="item" />
    </div>

    <div v-if="hiddenCount > 0" class="unlock card-surface">
      <p>
        还有 <strong>{{ hiddenCount }}</strong> 条符合条件的公告未展示。
        <router-link :to="{ name: 'login', query: { redirect: route.fullPath } }">登录</router-link>
        后可查看完整列表并阅读公告详情。
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TenderCard from '@/components/TenderCard.vue'
import { filterTenders, tenderCategories, tenderRegions } from '@/data/tenders'
import { GUEST_LIST_LIMIT, useAuth } from '@/auth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn } = useAuth()

const category = ref(route.query.category || '')
const region = ref(route.query.region || '')
const keyword = ref(route.query.q || '')

function syncQuery() {
  const q = {}
  if (category.value) q.category = category.value
  if (region.value) q.region = region.value
  if (keyword.value.trim()) q.q = keyword.value.trim()
  router.replace({ query: q })
}

watch([category, region, keyword], () => {
  syncQuery()
})

const filtered = computed(() =>
  filterTenders({
    category: category.value,
    region: region.value,
    keyword: keyword.value
  })
)

const visibleList = computed(() => {
  if (isLoggedIn.value) return filtered.value
  return filtered.value.slice(0, GUEST_LIST_LIMIT)
})

const hiddenCount = computed(() => {
  if (isLoggedIn.value) return 0
  return Math.max(0, filtered.value.length - GUEST_LIST_LIMIT)
})

function reset() {
  category.value = ''
  region.value = ''
  keyword.value = ''
}
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

.unlock {
  margin-top: 1.25rem;
  padding: 1rem 1.25rem;
  border-left: 4px solid var(--color-primary);
}

.unlock p {
  margin: 0;
  font-size: 0.92rem;
  color: var(--text-muted);
  line-height: 1.55;
}

.unlock strong {
  color: var(--text);
}

.unlock a {
  font-weight: 600;
}
</style>
