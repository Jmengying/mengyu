<template>
  <div>
    <!-- Banner -->
    <section class="page-banner">
      <div class="banner-bg" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="banner-content">
        <h1 class="banner-title">
          <i class="fa fa-archive spinner"></i>
          <span class="text-gradient">📦 资源分享</span>
        </h1>
        <p class="banner-desc">发现和分享优质的 ACG 资源</p>
      </div>
      <div class="banner-skew">
        <svg viewBox="0 0 1200 60" preserveAspectRatio="none">
          <path d="M0,0 L600,60 L1200,0 L1200,60 L0,60 Z" fill="#f5f5f5"></path>
        </svg>
      </div>
    </section>

    <div class="content-wrap">
      <div class="section-header">
        <h2 class="section-title"><i class="fa fa-download"></i> 资源列表</h2>
        <span class="section-count" v-if="!loading">共 {{ resources.length }} 个资源</span>
      </div>

      <div v-if="loading" style="text-align: center; padding: 80px 0;">
        <div class="loading-anime">
          <div class="loading-cat"><div class="eyes"></div><div class="mouth"></div></div>
        </div>
        <p style="margin-top: 16px; color: #D87CFF; font-size: 13px;">正在拼命加载中...</p>
      </div>

      <div v-else-if="resources.length === 0" class="card empty-state">
        <i class="fa fa-inbox" style="font-size: 48px;"></i>
        <p style="margin-top: 12px;">还没有资源分享哦~</p>
        <router-link to="/articles/create" class="banner-btn banner-btn-primary" style="display: inline-block; margin-top: 16px;" v-if="isLoggedIn">
          分享资源
        </router-link>
      </div>

      <div v-else class="articles-layout">
        <div class="page-sidebar" v-if="totalPages > 1">
          <div class="page-sidebar-inner">
            <span class="page-arrow" :class="{ off: currentPage <= 1 }" @click="goPage(currentPage - 1)">◀</span>
            <span v-for="p in pageNumbers" :key="p" class="page-num"
              :class="{ on: p === currentPage, dot: p === '...' }"
              @click="p !== '...' && goPage(p)">{{ p === '...' ? '…' : p }}</span>
            <span class="page-arrow" :class="{ off: currentPage >= totalPages }" @click="goPage(currentPage + 1)">▶</span>
          </div>
        </div>
        <div class="articles-main">
          <div class="article-grid-wrap">
            <Transition :name="pageDir" mode="out-in">
              <div class="article-grid" :key="currentPage">
                <article v-for="article in displayResources" :key="article.id" class="post-card card" @click="$router.push(`/articles/${article.id}`)">
              <div class="card-img">
                <div class="img-wrap" :style="!article.coverImage ? 'background: #1a1a3e;' : ''">
                  <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
                  <img v-else :src="defaultCovers[article.id % defaultCovers.length]" :alt="article.title" />
                </div>
              </div>
              <div class="card-body">
                <div class="card-category">
                  <span class="category-tag"><i class="fa fa-user-circle-o"></i> {{ article.authorName || '匿名' }}</span>
                  <span class="category-tag" style="margin-left: 6px; background: rgba(216, 124, 255, 0.1); color: #D87CFF;"><i class="fa fa-archive"></i> 资源</span>
                </div>
                <h3 class="card-title">
                  <i class="fa fa-thumb-tack" style="color: #ff5c72; margin-right: 4px;" v-if="article.isPinned"></i>
                  {{ article.title }}
                </h3>
                <p class="card-summary">{{ article.summary || '暂无简介' }}</p>
                <div class="card-meta">
                  <span><i class="fa fa-eye"></i> {{ article.views || 0 }}</span>
                  <span class="dot"></span>
                  <span><i class="fa fa-comment-o"></i> {{ article.commentCount || 0 }}</span>
                </div>
              </div>
            </article>
          </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArticleList } from '../api/article'

const resources = ref([])
const loading = ref(true)
const isLoggedIn = ref(!!localStorage.getItem('token'))

const pageSize = 8
const currentPage = ref(1)
const pageDir = ref('page-next')

const displayResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return resources.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(resources.value.length / pageSize))

const pageNumbers = computed(() => {
  const tp = totalPages.value
  if (tp <= 7) return Array.from({ length: tp }, (_, i) => i + 1)
  const cp = currentPage.value
  if (cp <= 3) return [1, 2, 3, 4, '...', tp]
  if (cp >= tp - 2) return [1, '...', tp - 3, tp - 2, tp - 1, tp]
  return [1, '...', cp - 1, cp, cp + 1, '...', tp]
})

const goPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  pageDir.value = p > currentPage.value ? 'page-next' : 'page-prev'
  currentPage.value = p
  window.scrollTo({ top: 200, behavior: 'smooth' })
}

const defaultCovers = [
  'https://moewalls.com/wp-content/uploads/2025/12/chisa-in-the-rain-wuthering-waves-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2026/01/anime-girl-tank-flower-field-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2025/12/silhouetted-anime-girl-under-starry-night-sky-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2026/02/reading-clouds-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2026/03/horizon-sky-wanderer-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2025/12/naruto-cloudy-field-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2026/03/my-neighbor-totoro-miminzuku-thumb-364x205.jpg',
  'https://moewalls.com/wp-content/uploads/2026/03/neon-ruins-thumb-364x205.jpg'
]

const banners = [
  'linear-gradient(135deg, #2d1b4e 0%, #1a1a3e 30%, #4a2d5c 70%, #2d1b4e 100%)',
  'linear-gradient(135deg, #1e3a5f 0%, #2d1b4e 40%, #4a2d5c 70%, #1a1a3e 100%)'
]

const bannerImage = ref(banners[Math.floor(Math.random() * banners.length)])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(async () => {
  try {
    const res = await getArticleList('resource')
    if (res.code === 200) {
      resources.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load resources:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-banner {
  position: relative;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  width: 100%;
}

.banner-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center 30%;
  background-color: #1a1a3e;
  animation: bannerFadeIn 1.2s ease;
}

.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(30, 30, 50, 0.6), rgba(60, 40, 60, 0.4));
}

@keyframes bannerFadeIn {
  from { opacity: 0; transform: scale(1.05); }
  to { opacity: 1; transform: scale(1); }
}

.banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 20px;
}

.banner-title {
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #fff;
}

.banner-title .text-gradient {
  background: linear-gradient(135deg, #fff, #ffcad4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.banner-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-bottom: 24px;
}

.banner-skew {
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 60px;
  z-index: 2;
}

.banner-skew svg {
  width: 100%;
  height: 100%;
}

.content-wrap {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  position: relative;
  z-index: 3;
  margin-top: -30px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 0 4px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.section-title i {
  color: #D87CFF;
  margin-right: 8px;
}

.section-count {
  font-size: 13px;
  color: #aaa;
}

.articles-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.page-sidebar {
  flex-shrink: 0;
  padding-top: 8px;
}

.page-sidebar-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  padding: 12px 8px;
  min-width: 40px;
}

.page-arrow {
  font-size: 12px; cursor: pointer;
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; color: #D87CFF;
  transition: all 0.2s; user-select: none;
}
.page-arrow:hover { background: rgba(216, 124, 255, 0.15); }
.page-arrow.off { opacity: 0.25; cursor: not-allowed; }

.page-num {
  width: 30px; height: 30px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; cursor: pointer;
  color: #888; transition: all 0.2s; user-select: none;
}
.page-num:hover { background: rgba(255, 92, 114, 0.1); color: #ff5c72; }
.page-num.on { background: #ff5c72; color: #fff; }
.page-num.dot { cursor: default; color: #ccc; font-size: 12px; }

.articles-main {
  flex: 1;
  min-width: 0;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  padding-bottom: 40px;
}

.article-grid-wrap {
  position: relative;
  overflow: hidden;
}

.page-prev-enter-active,
.page-prev-leave-active,
.page-next-enter-active,
.page-next-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-next-leave-to { opacity: 0; transform: translateX(-40px) scale(0.97); }
.page-next-enter-from { opacity: 0; transform: translateX(40px) scale(0.97); }
.page-prev-leave-to { opacity: 0; transform: translateX(40px) scale(0.97); }
.page-prev-enter-from { opacity: 0; transform: translateX(-40px) scale(0.97); }

.post-card {
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  border-radius: 12px;
  height: 320px;
}

.post-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-img {
  width: 100%;
  flex-shrink: 0;
  overflow: hidden;
  height: 180px;
}

.img-wrap {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #2d1b4e, #4a2d5c);
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.post-card:hover .img-wrap img {
  transform: scale(1.05);
}

.card-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.card-category {
  margin-bottom: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  line-height: 1.4;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
  margin-bottom: 0;
}

.card-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #aaa;
  flex-shrink: 0;
  margin-top: 10px;
}

.post-category {
  margin-bottom: 8px;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.4;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary {
  font-size: 14px;
  color: #888;
  margin-bottom: 16px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.post-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #aaa;
}

.post-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  display: inline-block;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #ccc;
  margin: 0 8px;
}

/* Loading animation */
.loading-anime {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
}

.loading-cat {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ff5c72, #D87CFF);
  border-radius: 50%;
  position: relative;
  animation: catBounce 1s ease-in-out infinite;
  box-shadow: 0 4px 20px rgba(216, 124, 255, 0.25);
}

.loading-cat::before,
.loading-cat::after {
  content: '';
  position: absolute;
  width: 14px;
  height: 14px;
  background: inherit;
  border-radius: 50% 50% 0 50%;
  top: -5px;
}
.loading-cat::before { left: 7px; transform: rotate(-30deg); }
.loading-cat::after { right: 7px; transform: rotate(30deg); }

.loading-cat .eyes::before,
.loading-cat .eyes::after {
  content: '';
  position: absolute;
  width: 5px;
  height: 5px;
  background: #fff;
  border-radius: 50%;
  top: 22px;
  animation: catBlink 2.5s ease-in-out infinite;
}
.loading-cat .eyes::before { left: 15px; }
.loading-cat .eyes::after { right: 15px; }

.loading-cat .mouth {
  position: absolute;
  width: 7px;
  height: 3px;
  border-bottom: 2px solid rgba(255,255,255,0.5);
  border-radius: 0 0 50% 50%;
  top: 32px;
  left: 50%;
  transform: translateX(-50%);
}

@keyframes catBounce {
  0%, 100% { transform: translateY(0) scale(1); }
  40% { transform: translateY(-18px) scale(1.05); }
  50% { transform: translateY(-20px); }
  60% { transform: translateY(-16px) scale(1.02); }
}

@keyframes catBlink {
  0%, 88%, 100% { transform: scaleY(1); }
  94% { transform: scaleY(0.1); }
}

@media (max-width: 1024px) {
  .article-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .page-banner { height: 260px; }
  .banner-title { font-size: 24px; }
  .banner-desc { font-size: 14px; }
  .article-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .post-card { height: 280px; }
  .card-img { height: 150px; }
  .card-title { font-size: 14px; }
  .card-summary { font-size: 12px; -webkit-line-clamp: 1; }
  .card-body { padding: 10px 12px 12px; }
  .content-wrap { padding: 0 12px; }

  .articles-layout { flex-direction: column; }
  .page-sidebar { width: 100%; padding-top: 0; }
  .page-sidebar-inner {
    flex-direction: row;
    justify-content: center;
    padding: 8px 12px;
    min-width: unset;
  }
  .page-arrow { width: 26px; height: 26px; font-size: 11px; }
  .page-num { width: 26px; height: 26px; font-size: 12px; }
}

@media (max-width: 480px) {
  .page-banner { height: 180px; }
  .banner-title { font-size: 20px; }
  .banner-desc { display: none; }
  .article-grid { grid-template-columns: 1fr; }
  .post-card { height: 260px; }
  .card-img { height: 130px; }
}
</style>
