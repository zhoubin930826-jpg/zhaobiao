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
    <p class="summary">{{ item.summary }}</p>
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
  return { name: 'login', query: { redirect: `/detail/${props.item.id}` } }
})

const detailLinkText = computed(() => (isLoggedIn.value ? '查看详情' : '登录后查看详情'))
</script>

<style scoped>
.card {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 1.35rem 1.5rem;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  transition: box-shadow 0.2s, border-color 0.2s;
}

.card:hover {
  border-color: #c5d4eb;
  box-shadow: 0 8px 32px rgba(26, 95, 180, 0.1);
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

.title {
  margin: 0 0 0.5rem;
  font-size: 1.1rem;
  line-height: 1.45;
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
  font-size: 0.85rem;
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
  display: inline-block;
  font-size: 0.88rem;
  font-weight: 500;
}
</style>
