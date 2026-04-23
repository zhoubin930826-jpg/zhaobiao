<template>
  <article class="card">
    <div class="meta">
      <span class="tag">{{ item.category }}</span>
      <span class="region">{{ item.region }}</span>
      <span :class="['status', item.status === '进行中' ? 'open' : 'closed']">{{ item.status }}</span>
    </div>
    <h2 class="title">
      <router-link :to="detailRoute">{{ item.title }}</router-link>
    </h2>
    <p v-if="item.summary && String(item.summary).trim()" class="summary">{{ item.summary }}</p>
    <dl class="facts">
      <div>
        <dt>采购单位</dt>
        <dd>{{ item.purchaser }}</dd>
      </div>
      <div>
        <dt>预算</dt>
        <dd>{{ item.budget }}</dd>
      </div>
      <div>
        <dt>发布</dt>
        <dd>{{ formatDate(item.publishAt) }}</dd>
      </div>
      <div>
        <dt>截止</dt>
        <dd>{{ formatDate(item.deadline) }}</dd>
      </div>
    </dl>
    <router-link class="more" :to="detailRoute">{{ detailLinkText }}</router-link>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate } from '@/utils/format'
import { useAuth } from '@/auth'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const { isLoggedIn } = useAuth()

const detailRoute = computed(() => {
  if (isLoggedIn.value) {
    return { name: 'detail', params: { id: props.item.id } }
  }
    return { path: '/', query: { redirect: `/detail/${props.item.id}` } }
})

const detailLinkText = computed(() => (isLoggedIn.value ? '查看详情' : '登录后查看详情'))
</script>

<style scoped>
.card {
  position: relative;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  border-radius: 14px;
  padding: 1.4rem 1.55rem 1.45rem;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.04), 0 10px 28px -8px rgba(26, 95, 180, 0.07);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-left: 3px solid #e2e8f0;
  transition: box-shadow 0.22s ease, border-color 0.22s ease, border-left-color 0.22s ease,
    transform 0.22s ease;
}

.card:hover {
  border-color: #c5d4eb;
  border-left-color: var(--color-primary);
  box-shadow: 0 8px 28px rgba(26, 95, 180, 0.1);
  transform: translateY(-2px);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 0.75rem;
  align-items: center;
  margin-bottom: 0.65rem;
}

.tag {
  font-size: 0.75rem;
  padding: 0.22rem 0.58rem;
  border-radius: 6px;
  background: linear-gradient(180deg, #eff6ff 0%, #e8f0fc 100%);
  color: var(--color-primary);
  font-weight: 600;
  border: 1px solid rgba(26, 95, 180, 0.12);
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

.title {
  margin: 0 0 0.55rem;
  font-size: 1.08rem;
  line-height: 1.48;
  font-weight: 600;
}

.title a {
  color: var(--text);
  text-decoration: none;
}

.title a:hover {
  color: var(--color-primary);
}

.summary {
  margin: 0 0 1rem;
  font-size: 0.9rem;
  color: var(--text-muted);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.facts {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.65rem 1rem;
  margin: 0 0 1rem;
  padding: 0.75rem 0.85rem;
  font-size: 0.85rem;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1px solid #eef2f7;
}

.facts dt {
  margin: 0;
  color: var(--text-muted);
  font-weight: normal;
  font-size: 0.78rem;
}

.facts dd {
  margin: 0.15rem 0 0;
  font-weight: 500;
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.88rem;
  font-weight: 600;
  text-decoration: none;
}

.more:hover {
  text-decoration: none;
}

.more::after {
  content: '→';
  font-size: 0.85em;
  opacity: 0.65;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.more:hover::after {
  transform: translateX(3px);
  opacity: 1;
}
</style>
