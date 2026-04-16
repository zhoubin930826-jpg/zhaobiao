<template>
  <div class="home">
    <section class="hero">
      <h1>公开、公平、公正的招投标信息</h1>
      <p>集中发布招标公告，支持按类型、地区筛选，便于供应商与社会公众查阅。</p>
      <div class="hero-actions">
        <router-link to="/list" class="cta">浏览公告</router-link>
        <router-link v-if="!isLoggedIn" :to="{ name: 'login', query: { redirect: '/list' } }" class="cta ghost">
          登录解锁全部
        </router-link>
      </div>
      <p v-if="!isLoggedIn" class="hero-note">请先登录后浏览招标列表与公告详情。</p>
    </section>

    <section class="section">
      <div class="section-head">
        <h2>最新公告</h2>
        <router-link to="/list">更多</router-link>
      </div>
      <div class="grid">
        <TenderCard v-for="item in latest" :key="item.id" :item="item" />
      </div>
    </section>

    <section class="highlights">
      <div class="box">
        <strong>{{ isLoggedIn ? stats.total : '—' }}</strong>
        <span>{{ isLoggedIn ? '条公告' : '条公告总数（登录后可见）' }}</span>
      </div>
      <div class="box">
        <strong>{{ isLoggedIn ? stats.open : '—' }}</strong>
        <span>{{ isLoggedIn ? '进行中' : '进行中（登录后可见）' }}</span>
      </div>
      <div class="box">
        <strong>3</strong>
        <span>业务类型</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import TenderCard from '@/components/TenderCard.vue'
import { listPortalTenders } from '@/api/portal'
import { useAuth } from '@/auth'

const { isLoggedIn } = useAuth()
const list = ref([])

const latest = computed(() => {
  const sorted = [...list.value].sort((a, b) => new Date(b.publishAt) - new Date(a.publishAt))
  return sorted.slice(0, 3)
})

const stats = computed(() => ({
  total: list.value.length,
  open: list.value.filter((t) => t.status === '进行中').length
}))

onMounted(async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await listPortalTenders({ pageNum: 1, pageSize: 100 })
    list.value = Array.isArray(res.list) ? res.list : []
  } catch (_) {
    list.value = []
  }
})
</script>

<style scoped>
.home {
  padding-top: 1.5rem;
}

.hero {
  background: linear-gradient(145deg, #fff 0%, #eef4fc 100%);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 2.25rem 1.75rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow);
}

.hero h1 {
  margin: 0 0 0.75rem;
  font-size: clamp(1.35rem, 3vw, 1.75rem);
  color: var(--color-primary-dark);
  line-height: 1.3;
}

.hero p {
  margin: 0 0 1.25rem;
  color: var(--text-muted);
  max-width: 36rem;
  font-size: 0.95rem;
}

.cta {
  display: inline-block;
  padding: 0.55rem 1.25rem;
  background: var(--color-primary);
  color: #fff !important;
  border-radius: 8px;
  font-weight: 500;
  text-decoration: none !important;
}

.cta:hover {
  background: var(--color-primary-dark);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  align-items: center;
}

.cta.ghost {
  background: transparent;
  color: var(--color-primary) !important;
  border: 2px solid var(--color-primary);
}

.cta.ghost:hover {
  background: rgba(26, 95, 180, 0.08);
}

.hero-note {
  margin: 1rem 0 0;
  font-size: 0.82rem;
  color: var(--text-muted);
  max-width: 40rem;
}

.section {
  margin-bottom: 2rem;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.section-head h2 {
  margin: 0;
  font-size: 1.15rem;
}

.grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.highlights {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

@media (max-width: 640px) {
  .highlights {
    grid-template-columns: 1fr;
  }
}

.box {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1.25rem;
  text-align: center;
  box-shadow: var(--shadow);
}

.box strong {
  display: block;
  font-size: 1.5rem;
  color: var(--color-primary);
  margin-bottom: 0.25rem;
}

.box span {
  font-size: 0.85rem;
  color: var(--text-muted);
}
</style>
