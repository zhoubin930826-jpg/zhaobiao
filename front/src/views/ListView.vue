<template>
  <div class="home-portal">
    <div class="main-layout">
      <div class="content-left">

        <!-- 动态分类展示（单列） -->
        <div class="category-grid">
          <section class="portal-section" v-for="sec in categorySections" :key="sec.title">
            <div class="section-header">
              <h3>{{ sec.title }}</h3>
              <router-link :to="{ name: 'categoryTenders', query: { category: sec.title } }" class="more">更多 >></router-link>
            </div>
            <ul class="text-list">
              <li v-for="item in sec.items" :key="item.id">
                <router-link :to="{ name: 'detail', params: { id: item.id } }" class="item-link">
                  <span class="dot"></span>
                  <span class="title" :title="item.title">{{ item.title }}</span>
                </router-link>
                <span class="date">{{ formatDate(item.publishAt, 'MM-DD') }}</span>
              </li>
              <li v-if="!sec.items.length" class="empty-tip">暂无相关数据</li>
            </ul>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { listLatestPortalTenders, listPortalTenders } from '@/api/portal'
import { useAuth } from '@/auth'
import { formatDate } from '@/utils/format'

const { isLoggedIn } = useAuth()
const list = ref([])

// 动态分类展示
const categorySections = computed(() => {
  const cats = [...new Set(list.value.map(t => t.category).filter(Boolean))]
  
  // 如果没有数据，提供默认分类占位
  if (cats.length === 0) {
    return [
      { title: '工程', items: [] },
      { title: '货物', items: [] },
      { title: '服务', items: [] }
    ]
  }

  // 按照分类分组，每个分类取前 6 条
  return cats.map(c => ({
    title: c,
    items: list.value.filter(t => t.category === c).sort((a, b) => new Date(b.publishAt) - new Date(a.publishAt)).slice(0, 6)
  }))
})

async function loadList() {
  try {
    if (isLoggedIn.value) {
      const res = await listPortalTenders({ pageNum: 1, pageSize: 100 })
      list.value = Array.isArray(res.list) ? res.list : []
    } else {
      const res = await listLatestPortalTenders()
      list.value = Array.isArray(res.list) ? res.list : []
    }
  } catch (_) {
    list.value = []
  }
}

onMounted(loadList)
watch(isLoggedIn, loadList)
</script>

<style scoped>
.home-portal {
  padding: 1.5rem 0 3rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* 主体布局 */
.main-layout {
  display: block;
}

.content-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 栏目区块 */
.portal-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
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
  font-size: 1.15rem;
  color: #1a5fb4;
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
  height: 18px;
  background: #1a5fb4;
  border-radius: 2px;
}

.more {
  font-size: 0.85rem;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}

.more:hover {
  color: #1a5fb4;
}

/* 动态分类：单列，每类一行 */
.category-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

/* 文本列表 */
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
  transition: background 0.2s;
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
</style>

