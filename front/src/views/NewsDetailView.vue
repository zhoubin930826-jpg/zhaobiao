<template>
  <div class="news-detail-page">
    <p v-if="!item" class="not-found">
      未找到该资讯，<router-link :to="{ name: 'list' }">返回首页</router-link>
    </p>

    <template v-else>
      <nav class="crumb">
        <router-link :to="{ name: 'list' }">首页</router-link>
        <span aria-hidden="true">/</span>
        <router-link :to="{ name: 'newsList' }">最新资讯</router-link>
        <span aria-hidden="true">/</span>
        <span class="current">资讯详情</span>
      </nav>

      <article class="article card-surface">
        <div class="article-body">
          <div class="meta">
            <span class="tag">{{ item.category }}</span>
            <span class="date">{{ item.publishDate }}</span>
          </div>
          <h1>{{ item.title }}</h1>
          <dl class="facts">
            <div>
              <dt>发布日期</dt>
              <dd>{{ item.publishDate }}</dd>
            </div>
            <div>
              <dt>信息来源</dt>
              <dd>{{ item.source }}</dd>
            </div>
          </dl>
          <p class="lead">{{ item.summary }}</p>
          <section class="block content-block">
            <h2>正文</h2>
            <div class="body">
              <figure class="body-figure">
                <img :src="coverImage" :alt="item.title" class="body-image" />
              </figure>
              <p v-for="(para, index) in item.content" :key="index">{{ para }}</p>
            </div>
          </section>
          <router-link :to="{ name: 'list' }" class="back">← 返回首页</router-link>
        </div>
      </article>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getNewsById } from '@/data/news'
import newsBanner from '@/assets/banner-greatwall.png'

const route = useRoute()

const item = computed(() => getNewsById(route.params.id))

const coverImage = computed(() => {
  const news = item.value
  if (!news) return newsBanner
  return news.coverImage || newsBanner
})
</script>

<style scoped>
.news-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
}

.not-found {
  text-align: center;
  color: #64748b;
  padding: 3rem 1rem;
}

.crumb {
  font-size: 0.9rem;
  color: #64748b;
  margin-bottom: 1rem;
}

.crumb a {
  color: #1a5fb4;
  text-decoration: none;
}

.crumb a:hover {
  text-decoration: underline;
}

.crumb .current {
  color: #334155;
}

.article.card-surface {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.article-body {
  padding: 1.75rem 2rem 2rem;
}

.content-block .body {
  padding: 1rem 1.25rem;
  background: #fafbfc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}

.body-figure {
  margin: 0 0 1.25rem;
  padding: 0;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #f1f5f9;
}

.body-image {
  display: block;
  width: 100%;
  max-height: 320px;
  object-fit: cover;
  vertical-align: middle;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 1rem;
}

.tag {
  display: inline-block;
  padding: 0.15rem 0.55rem;
  font-size: 0.8rem;
  color: #1a5fb4;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
}

.date {
  font-size: 0.85rem;
  color: #94a3b8;
}

.article h1 {
  margin: 0 0 1.25rem;
  font-size: 1.5rem;
  line-height: 1.4;
  color: #0f172a;
}

.facts {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem 1.5rem;
  margin: 0 0 1.25rem;
  padding: 1rem 1.25rem;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.facts div {
  min-width: 0;
}

.facts dt {
  font-size: 0.8rem;
  color: #64748b;
  margin-bottom: 0.2rem;
}

.facts dd {
  margin: 0;
  font-size: 0.95rem;
  color: #334155;
}

.lead {
  margin: 0 0 1.5rem;
  padding: 0.85rem 1rem;
  font-size: 0.95rem;
  line-height: 1.7;
  color: #475569;
  background: #f1f5f9;
  border-left: 4px solid #1a5fb4;
  border-radius: 0 4px 4px 0;
}

.block h2 {
  margin: 0 0 0.85rem;
  font-size: 1.05rem;
  color: #1a5fb4;
}

.content-block .body p {
  margin: 0 0 1rem;
  font-size: 0.95rem;
  line-height: 1.85;
  color: #334155;
  text-indent: 2em;
}

.content-block .body p:last-child {
  margin-bottom: 0;
}

.back {
  display: inline-block;
  margin-top: 1.5rem;
  color: #1a5fb4;
  text-decoration: none;
  font-size: 0.95rem;
}

.back:hover {
  text-decoration: underline;
}

@media (max-width: 640px) {
  .body-image {
    max-height: 200px;
  }

  .article-body {
    padding: 1.25rem 1rem 1.5rem;
  }

  .content-block .body {
    padding: 0.85rem 1rem;
  }
}
</style>
