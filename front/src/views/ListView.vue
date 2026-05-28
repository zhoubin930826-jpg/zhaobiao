<template>
  <div class="home-portal">
    <div class="main-layout">
      <div class="content-left">
        <!-- 资讯看板 -->
        <section class="portal-section news-section">
          <div class="section-header">
            <h3>最新资讯</h3>
            <router-link :to="{ name: 'newsList' }" class="more">更多 >></router-link>
          </div>
          <div class="news-panel">
            <router-link
              v-if="activeNews"
              :to="{ name: 'newsDetail', params: { id: activeNews.id } }"
              class="news-visual"
            >
              <img :src="activeNewsCover" alt="资讯配图" class="news-image" />
              <div class="news-visual-mask">
                <span class="news-visual-tag">{{ activeNews.category }}</span>
                <p class="news-visual-title">{{ activeNews.title }}</p>
                <p class="news-visual-summary">{{ activeNews.summary }}</p>
              </div>
            </router-link>
            <div v-else class="news-visual news-visual--static">
              <img :src="activeNewsCover" alt="资讯配图" class="news-image" />
            </div>

            <div class="news-list-panel">
              <ul class="news-list">
                <li
                  v-for="item in newsList"
                  :key="item.id"
                  :class="{ active: item.id === activeNewsId }"
                >
                  <router-link
                    :to="{ name: 'newsDetail', params: { id: item.id } }"
                    class="news-item-link"
                    @mouseenter="activeNewsId = item.id"
                  >
                    <span class="news-type">{{ item.category }}</span>
                    <span class="news-title" :title="item.title">{{ item.title }}</span>
                    <span class="news-date">{{ formatDate(item.publishAt, 'MM-DD') }}</span>
                  </router-link>
                </li>
              </ul>
            </div>
          </div>
        </section>

        <!-- 采购项目动态 -->
        <section class="portal-section">
          <div class="section-header">
            <h3>项目动态</h3>
            <router-link :to="moreLink" class="more">更多 >></router-link>
          </div>
          <ul class="text-list">
            <li v-for="item in previewList" :key="item.id">
              <router-link :to="{ name: 'detail', params: { id: item.id } }" class="item-link">
                <span class="dot"></span>
                <span v-if="item.category" class="project-type">{{ item.category }}</span>
                <span class="title" :title="item.title">{{ item.title }}</span>
              </router-link>
              <span class="date">{{ formatDate(item.publishAt, 'MM-DD') }}</span>
            </li>
            <li v-if="!previewList.length" class="empty-tip">暂无相关数据</li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { listLatestPortalNews, listLatestPortalTenders, listPortalTenders } from '@/api/portal'
import { authState } from '@/auth'
import { formatDate } from '@/utils/format'
import newsBanner from '@/assets/banner-greatwall.png'

const NEWS_PREVIEW_LIMIT = 4

const newsList = ref([])
const activeNewsId = ref(null)

const PREVIEW_LIMIT = 6
const MORE_CATEGORY = '全部'

const list = ref([])

const displayList = computed(() =>
  [...list.value].sort((a, b) => new Date(b.publishAt) - new Date(a.publishAt))
)

const previewList = computed(() => displayList.value.slice(0, PREVIEW_LIMIT))

const moreLink = computed(() => ({
  name: 'categoryTenders',
  query: { category: MORE_CATEGORY }
}))

const activeNews = computed(() =>
  newsList.value.find(item => item.id === activeNewsId.value) || newsList.value[0] || null
)

const activeNewsCover = computed(() => {
  const news = activeNews.value
  if (news && news.coverUrl) return news.coverUrl
  return newsBanner
})

watch(newsList, items => {
  if (!items.length) {
    activeNewsId.value = null
    return
  }
  if (!items.some(item => item.id === activeNewsId.value)) {
    activeNewsId.value = items[0].id
  }
})

async function loadNews() {
  try {
    const res = await listLatestPortalNews(NEWS_PREVIEW_LIMIT)
    newsList.value = Array.isArray(res.list) ? res.list : []
    if (newsList.value.length && !newsList.value.some(item => item.id === activeNewsId.value)) {
      activeNewsId.value = newsList.value[0].id
    }
  } catch (_) {
    newsList.value = []
    activeNewsId.value = null
  }
}

async function loadList() {
  try {
    if (authState.token) {
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

onMounted(() => {
  loadNews()
  loadList()
})
watch(() => authState.token, loadList)
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

.main-layout {
  display: block;
}

.content-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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
  font-size: 1.15rem;
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
  height: 18px;
  background: var(--section-color);
  border-radius: 2px;
}

.more {
  font-size: 0.85rem;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}

.more:hover {
  color: var(--section-color);
}

/* 资讯栏（紧凑布局） */
.news-section .section-header {
  padding: 0.65rem 1rem;
}

.news-section .section-header h3 {
  font-size: 1rem;
}

.news-section .section-header h3::before {
  height: 14px;
}

.news-panel {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem 0.9rem;
}

.news-visual {
  flex: 0 0 30%;
  max-width: 30%;
  position: relative;
  display: block;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  min-height: 140px;
  max-height: 160px;
  background: #e2e8f0;
  text-decoration: none;
}

.news-visual--static {
  flex: 0 0 30%;
  max-width: 30%;
  min-height: 140px;
  max-height: 160px;
}

.news-image {
  width: 100%;
  height: 100%;
  min-height: 140px;
  max-height: 160px;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.news-visual-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 0.55rem 0.65rem 0.5rem;
  background: linear-gradient(180deg, transparent 0%, rgba(15, 23, 42, 0.82) 100%);
  color: #fff;
}

.news-visual-tag {
  display: inline-block;
  margin-bottom: 0.2rem;
  padding: 0.05rem 0.35rem;
  font-size: 0.65rem;
  background: rgba(26, 95, 180, 0.9);
  border-radius: 3px;
}

.news-visual-title {
  margin: 0 0 0.2rem;
  font-size: 0.8rem;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-visual-summary {
  margin: 0;
  font-size: 0.72rem;
  line-height: 1.4;
  opacity: 0.92;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-list-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.news-list {
  list-style: none;
  margin: 0;
  padding: 0.2rem 0;
  flex: 1;
  overflow: auto;
}

.news-list li {
  border-bottom: 1px dashed #e2e8f0;
}

.news-list li:last-child {
  border-bottom: none;
}

.news-list li.active {
  background: var(--section-color-light);
}

.news-item-link {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  width: 100%;
  padding: 0.45rem 0.75rem;
  text-decoration: none;
  color: #334155;
  min-width: 0;
}

.news-item-link:hover .news-title,
.news-list li.active .news-title {
  color: var(--section-color);
}

.news-type {
  flex-shrink: 0;
  font-size: 0.72rem;
  color: var(--section-color);
  background: var(--section-color-light);
  border: 1px solid var(--section-color-border);
  border-radius: 4px;
  padding: 0.05rem 0.35rem;
}

.news-title {
  flex: 1;
  min-width: 0;
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.news-date {
  flex-shrink: 0;
  font-size: 0.75rem;
  color: #94a3b8;
  font-family: monospace;
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
  color: var(--section-color);
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
  background: var(--section-color);
}

.project-type {
  display: inline-block;
  margin-right: 0.5rem;
  padding: 0.1rem 0.45rem;
  font-size: 0.75rem;
  line-height: 1.4;
  color: var(--section-color);
  background: var(--section-color-light);
  border: 1px solid var(--section-color-border);
  border-radius: 4px;
  flex-shrink: 0;
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

@media (max-width: 768px) {
  .news-panel {
    flex-direction: column;
  }

  .news-visual,
  .news-visual--static {
    flex: none;
    max-width: 100%;
    width: 100%;
    min-height: 120px;
    max-height: 140px;
  }

  .news-image {
    min-height: 120px;
    max-height: 140px;
  }
}
</style>
