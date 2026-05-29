<template>
  <div class="home-portal">
    <!-- 顶部横幅 -->
    <div class="portal-banner" v-if="!isLoggedIn">
      <div class="banner-content">
        <h2>欢迎来到军科集团入库管理平台</h2>
        <p>集中发布各类招标公告，支持按类型、地区筛选，为供应商与社会公众提供透明、高效的信息服务。</p>
        <div class="banner-actions">
          <router-link :to="{ name: 'login', query: { redirect: '/list' } }" class="btn-primary">
            立即登录
          </router-link>
          <router-link to="/register" class="btn-outline">
            免费注册
          </router-link>
        </div>
      </div>
    </div>

    <div class="main-layout">
      <!-- 左侧主内容区 -->
      <div class="content-left">
        
        <!-- 重点推荐 (卡片网格布局) -->
        <section class="portal-section featured-section">
          <div class="section-header">
            <h3>重点推荐</h3>
            <router-link to="/list" class="more">更多 >></router-link>
          </div>
          <div class="featured-grid">
            <router-link 
              v-for="item in featuredTenders" 
              :key="item.id" 
              :to="{ name: 'detail', params: { id: item.id } }" 
              class="featured-card"
            >
              <div class="f-top">
                <span class="f-cat" v-if="item.category">{{ item.category }}</span>
                <span class="f-region" v-if="item.region"><i class="icon-pin">📍</i>{{ item.region }}</span>
              </div>
              <h4 class="f-title" :title="item.title">{{ item.title }}</h4>
              <div class="f-bot">
                <span class="f-budget">{{ formatBudget(item.budget) }}</span>
                <span class="f-date">{{ formatDate(item.publishAt) }}</span>
              </div>
            </router-link>
            <div v-if="!featuredTenders.length" class="empty-tip">暂无重点推荐数据</div>
          </div>
        </section>

        <!-- 动态分类展示 (两列布局) -->
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

        <!-- 中标/结果公示 -->
        <section class="portal-section">
          <div class="section-header">
            <h3>结果公示</h3>
            <router-link to="/list" class="more">更多 >></router-link>
          </div>
          <ul class="text-list">
            <li v-for="item in closedTenders" :key="item.id">
              <router-link :to="{ name: 'detail', params: { id: item.id } }" class="item-link">
                <span class="dot"></span>
                <span class="category" v-if="item.category">[{{ item.category }}]</span>
                <span class="title" :title="item.title">{{ item.title }}</span>
              </router-link>
              <span class="date">{{ formatDate(item.publishAt) }}</span>
            </li>
            <li v-if="!closedTenders.length" class="empty-tip">暂无结果公示数据</li>
          </ul>
        </section>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar-right">
        
        <!-- 用户状态卡片 -->
        <div class="side-box user-box">
          <div v-if="isLoggedIn" class="logged-in">
            <div class="avatar">
              <span class="avatar-text">{{ username ? username.charAt(0).toUpperCase() : 'U' }}</span>
            </div>
            <div class="u-info">
              <div class="u-greeting">欢迎回来</div>
              <div class="u-name">{{ username || '会员用户' }}</div>
            </div>
            <router-link to="/setting" class="u-btn primary">进入会员中心</router-link>
          </div>
          <div v-else class="logged-out">
            <div class="avatar">
              <span class="avatar-text">客</span>
            </div>
            <div class="u-info">
              <div class="u-greeting">欢迎访问</div>
              <div class="u-name">您尚未登录</div>
            </div>
            <div class="u-actions">
              <router-link to="/login" class="u-btn primary">登录</router-link>
              <router-link to="/register" class="u-btn outline">注册</router-link>
            </div>
          </div>
        </div>

        <!-- 平台数据 -->
        <div class="side-box stats-box">
          <div class="side-header">
            <h3>平台数据</h3>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">{{ isLoggedIn ? '公告总数' : '最新公告' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-value open">{{ stats.open }}</div>
              <div class="stat-label">进行中</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ categorySections.length || 3 }}</div>
              <div class="stat-label">业务类型</div>
            </div>
          </div>
        </div>

        <!-- 专属服务 -->
        <div class="side-box">
          <div class="side-header">
            <h3>专属服务</h3>
          </div>
          <div class="service-grid">
            <div class="service-item">
              <i class="icon">📝</i>
              <span>信息发布</span>
            </div>
            <div class="service-item">
              <i class="icon">✍️</i>
              <span>在线报名</span>
            </div>
            <div class="service-item">
              <i class="icon">🔍</i>
              <span>进度查询</span>
            </div>
            <div class="service-item">
              <i class="icon">🏆</i>
              <span>中标公示</span>
            </div>
          </div>
        </div>

        <!-- 政策法规 (模拟静态数据) -->
        <div class="side-box">
          <div class="side-header">
            <h3>政策法规</h3>
          </div>
          <ul class="text-list mini">
            <li>
              <a href="#" @click.prevent class="item-link"><span class="dot"></span><span class="title">中华人民共和国招标投标法</span></a>
            </li>
            <li>
              <a href="#" @click.prevent class="item-link"><span class="dot"></span><span class="title">中华人民共和国政府采购法</span></a>
            </li>
            <li>
              <a href="#" @click.prevent class="item-link"><span class="dot"></span><span class="title">招标投标法实施条例</span></a>
            </li>
            <li>
              <a href="#" @click.prevent class="item-link"><span class="dot"></span><span class="title">电子招标投标办法</span></a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { listLatestPortalTenders, listPortalTenders } from '@/api/portal'
import { useAuth } from '@/auth'
import { formatDate, formatBudget } from '@/utils/format'

const { isLoggedIn, username } = useAuth()
const list = ref([])

// 重点推荐（取前4条）
const featuredTenders = computed(() => {
  const sorted = [...list.value].sort((a, b) => new Date(b.publishAt) - new Date(a.publishAt))
  return sorted.slice(0, 4)
})

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

// 中标/结果公示（取状态不是“进行中”的）
const closedTenders = computed(() => {
  const closed = list.value.filter(t => t.status !== '进行中')
  if (closed.length > 0) {
    return closed.sort((a, b) => new Date(b.publishAt) - new Date(a.publishAt)).slice(0, 8)
  }
  // 如果没有已结束的，就取列表后半部分模拟展示
  const sorted = [...list.value].sort((a, b) => new Date(a.publishAt) - new Date(b.publishAt))
  return sorted.slice(0, 8)
})

const stats = computed(() => ({
  total: list.value.length,
  open: list.value.filter((t) => t.status === '进行中').length
}))

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

/* 顶部横幅 */
.portal-banner {
  background: #1a5fb4;
  border-radius: 4px;
  padding: 2rem 2.5rem;
  margin-bottom: 1.5rem;
  color: #fff;
}

.banner-content h2 {
  margin: 0 0 1rem;
  font-size: 1.6rem;
  font-weight: bold;
  letter-spacing: 1px;
}

.banner-content p {
  margin: 0 0 1.8rem;
  font-size: 1rem;
  opacity: 0.9;
  max-width: 600px;
  line-height: 1.6;
}

.banner-actions {
  display: flex;
  gap: 1rem;
}

.btn-primary {
  background: #fff;
  color: #1a5fb4 !important;
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  font-weight: bold;
  text-decoration: none !important;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #f0f6ff;
}

.btn-outline {
  background: transparent;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  text-decoration: none !important;
  transition: all 0.2s;
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #fff;
}

/* 主体布局 */
.main-layout {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.content-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.sidebar-right {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 栏目区块 */
.portal-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.25rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.section-header h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #1a5fb4;
  font-weight: bold;
  position: relative;
  padding-left: 0.8rem;
}

.section-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
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

/* 重点推荐网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  padding: 1rem;
}

.featured-card {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  border: 1px dashed #e2e8f0;
  border-radius: 4px;
  background: #fff;
  text-decoration: none;
  transition: all 0.2s ease;
}

.featured-card:hover {
  border-color: #1a5fb4;
  background: #f8fafc;
}

.f-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.f-cat {
  font-size: 0.7rem;
  color: #1a5fb4;
  background: #f0f6ff;
  padding: 0.15rem 0.45rem;
  border-radius: 4px;
  font-weight: 600;
}

.f-region {
  font-size: 0.75rem;
  color: #64748b;
}

.f-title {
  margin: 0 0 0.75rem;
  font-size: 0.95rem;
  color: #1e293b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-card:hover .f-title {
  color: #1a5fb4;
}

.f-bot {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}

.f-budget {
  color: #1a5fb4;
  font-weight: bold;
}

.f-date {
  color: #94a3b8;
}

/* 动态分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

/* 文本列表 */
.text-list {
  list-style: none;
  margin: 0;
  padding: 0.5rem 1.25rem;
}

.text-list.mini {
  padding: 0.25rem 1rem;
}

.text-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.65rem 0;
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

.category {
  color: #1a5fb4;
  margin-right: 0.5rem;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.title {
  font-size: 0.9rem;
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

/* 侧边栏区块 */
.side-box {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
}

.side-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.side-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #334155;
  font-weight: bold;
}

/* 用户卡片 */
.user-box {
  padding: 1.25rem;
}

.logged-in, .logged-out {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar {
  width: 56px;
  height: 56px;
  background: #f1f5f9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.75rem;
}

.avatar-text {
  color: #64748b;
  font-size: 1.5rem;
  font-weight: bold;
}

.u-greeting {
  font-size: 0.85rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.u-name {
  font-size: 1rem;
  font-weight: bold;
  color: #1e293b;
  margin-bottom: 1rem;
}

.u-actions {
  display: flex;
  gap: 0.75rem;
  width: 100%;
}

.u-btn {
  flex: 1;
  padding: 0.5rem 0;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  text-align: center;
  transition: all 0.2s;
}

.u-btn.primary {
  background: #1a5fb4;
  color: #fff;
  border: 1px solid #1a5fb4;
}

.u-btn.primary:hover {
  background: #0d3d7a;
}

.u-btn.outline {
  background: #fff;
  color: #1a5fb4;
  border: 1px solid #1a5fb4;
}

.u-btn.outline:hover {
  background: #f0f6ff;
}

/* 数据统计 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 1.25rem 1rem;
  gap: 1rem;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: bold;
  color: #1a5fb4;
  margin-bottom: 0.25rem;
}

.stat-value.open {
  color: #1a5fb4;
}

.stat-label {
  font-size: 0.8rem;
  color: #64748b;
}

/* 专属服务 */
.service-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: #e2e8f0;
}

.service-item {
  background: #fff;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: background 0.2s;
  color: #1a5fb4;
}

.service-item:hover {
  background: #f8fafc;
}

.service-item .icon {
  font-size: 1.5rem;
  font-style: normal;
  margin-bottom: 0.25rem;
}

.service-item span {
  font-size: 0.9rem;
  color: #475569;
  font-weight: 500;
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
  }
  .sidebar-right {
    width: 100%;
  }
  .category-grid {
    grid-template-columns: 1fr;
  }
  .featured-grid {
    grid-template-columns: 1fr;
  }
}
</style>

