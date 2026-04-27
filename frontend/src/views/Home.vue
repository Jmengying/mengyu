<template>
  <div>
    <!-- Banner -->
    <section class="page-banner">
      <div class="banner-bg" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="banner-content">
        <h1 class="banner-title">
          <i class="fa fa-star spinner"></i>
          <span class="text-gradient">✨ 萌域 · 分享热爱</span>
        </h1>
        <p class="banner-desc">ACG 社区 · 创意分享 · 同好聚集地</p>
        <div class="banner-actions">
          <router-link to="/articles/create" class="banner-btn banner-btn-primary" v-if="isLoggedIn">
            <i class="fa fa-pencil"></i> 写文章
          </router-link>
          <router-link to="/register" class="banner-btn banner-btn-primary" v-else>
            <i class="fa fa-rocket"></i> 加入我们
          </router-link>
        </div>
      </div>
      <div class="banner-skew">
        <svg viewBox="0 0 1200 60" preserveAspectRatio="none">
          <path d="M0,0 L600,60 L1200,0 L1200,60 L0,60 Z" fill="#f5f5f5"></path>
        </svg>
      </div>
    </section>

    <!-- Content -->
    <div class="content-wrap">
      <div class="section-header">
        <h2 class="section-title"><i class="fa fa-file-text-o"></i> 最新文章</h2>
        <span class="section-count" v-if="!loading">共 {{ articles.length }} 篇</span>
      </div>

      <div class="articles-layout">
        <!-- Pagination sidebar -->
        <div class="page-sidebar" v-if="totalPages > 1">
          <div class="page-sidebar-inner">
            <span class="page-arrow" :class="{ off: currentPage <= 1 }" @click="goPage(currentPage - 1)">◀</span>
            <span
              v-for="p in pageNumbers"
              :key="p"
              class="page-num"
              :class="{ on: p === currentPage, dot: p === '...' }"
              @click="p !== '...' && goPage(p)"
            >{{ p === '...' ? '…' : p }}</span>
            <span class="page-arrow" :class="{ off: currentPage >= totalPages }" @click="goPage(currentPage + 1)">▶</span>
          </div>
        </div>

        <div class="articles-main">
          <!-- Category tabs -->
          <div class="category-tabs">
            <span v-for="cat in categories" :key="cat.key"
              class="cat-tab"
              :class="{ active: activeCategory === cat.key }"
              @click="switchCategory(cat.key)">
              {{ cat.label }}
            </span>
          </div>

          <!-- Loading -->
          <div v-if="loading" style="text-align: center; padding: 80px 0;">
            <div class="loading-anime">
              <div class="loading-cat"></div>
            </div>
            <p style="margin-top: 16px; color: #D87CFF; font-size: 13px;">正在拼命加载中...</p>
          </div>

          <!-- Empty -->
          <div v-else-if="articles.length === 0" class="card empty-state">
            <i class="fa fa-inbox" style="font-size: 48px;"></i>
            <p style="margin-top: 12px;">还没有文章哦~</p>
            <router-link to="/articles/create" class="banner-btn banner-btn-primary" style="display: inline-block; margin-top: 16px;" v-if="isLoggedIn">
              写第一篇
            </router-link>
          </div>

          <!-- Article grid -->
          <div v-else class="article-grid-wrap">
            <Transition :name="pageDir" mode="out-in">
              <div class="article-grid" :key="currentPage">
                <article v-for="article in displayArticles" :key="article.id" class="post-card card" @click="$router.push(`/articles/${article.id}`)">
              <div class="card-img">
                <div class="img-wrap" :style="!article.coverImage ? 'background: #1a1a3e;' : ''">
                  <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
                  <img v-else :src="defaultCovers[article.id % defaultCovers.length]" :alt="article.title" />
                </div>
              </div>
              <div class="card-body">
                <div class="card-category">
                  <span class="category-tag">
                    <i class="fa fa-user-circle-o"></i> {{ article.authorName || '匿名' }}
                  </span>
                </div>
                <h3 class="card-title">
                  <i class="fa fa-thumb-tack" style="color: #ff5c72; margin-right: 4px;" v-if="article.isPinned"></i>
                  {{ article.title }}
                </h3>
                <p class="card-summary">{{ article.summary || '暂无摘要' }}</p>
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

const articles = ref([])
const loading = ref(true)
const isLoggedIn = ref(!!localStorage.getItem('token'))
const activeCategory = ref('')
const categories = [
  { key: '', label: '✨ 全部' },
  { key: 'resource', label: '📦 资源分享' },
  { key: 'discussion', label: '💬 讨论' },
  { key: 'art', label: '🎨 绘画' },
  { key: 'notice', label: '📢 通知' },
  { key: 'general', label: '📝 随笔' }
]

const pageSize = 8
const currentPage = ref(1)
const pageDir = ref('page-next')

const displayArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return articles.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(articles.value.length / pageSize))

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
  window.scrollTo({ top: 300, behavior: 'smooth' })
}

const fallbackBanners = [
  'https://cdn.nekosia.cat/images/catgirl/66ae26be7886f165901e8a48.jpg',
  'https://cdn.nekosia.cat/images/catgirl/66a6d96a44cc2f8f27e40f95.png',
  'https://cdn.nekosia.cat/images/tail-with-ribbon/66ae6a276743d24b5b03128d.png',
  'https://cdn.nekosia.cat/images/maid-uniform/66aac1eef7e81c5eaa67e7bf.png',
  'https://cdn.nekosia.cat/images/catgirl/66aabd7ac47bb24acdcbc469.png',
  'https://cdn.nekosia.cat/images/catgirl/66a72aa2a81baa4c6ac15790.png',
  'https://cdn.nekosia.cat/images/catgirl/66aab727167bbbb3d47a29e9.png',
  'https://cdn.nekosia.cat/images/catgirl/66925c2b86a614306e9d8a1b.jpg'
]

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

const bannerImage = ref('')

const loadBanner = async () => {
  // Try Nekosia API first
  try {
    const res = await fetch('https://api.nekosia.cat/api/v1/images/catgirl', { signal: AbortSignal.timeout(5000) })
    const data = await res.json()
    if (data?.image?.compressed?.url) {
      bannerImage.value = data.image.compressed.url
      return
    }
  } catch (e) {
    // API fallback: use predefined URL
  }
  // Fallback: random from predefined list
  bannerImage.value = fallbackBanners[Math.floor(Math.random() * fallbackBanners.length)]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const loadArticles = async (category) => {
  loading.value = true
  try {
    const res = await getArticleList(category || undefined)
    if (res.code === 200) {
      articles.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load articles:', e)
  } finally {
    loading.value = false
  }
}

const switchCategory = (cat) => {
  activeCategory.value = cat
  currentPage.value = 1
  loadArticles(cat)
}

onMounted(() => {
  loadBanner()
  loadArticles()
})
</script>

<style scoped>
/* ====== Banner ====== */
.page-banner {
  position: relative;
  height: 500px;
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
  font-size: 40px;
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

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spinner {
  display: inline-block;
  animation: spin 4s linear infinite;
  color: #ff5c72;
  margin-right: 8px;
  font-size: 28px;
  vertical-align: middle;
}

.banner-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-bottom: 24px;
}

.banner-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.banner-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
  border-radius: 25px;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
}

.banner-btn-primary {
  background: #ff5c72;
  color: #fff;
}

.banner-btn-primary:hover {
  background: #ff7a8a;
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(255, 92, 114, 0.4);
  color: #fff;
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

/* ====== Content ====== */
.content-wrap {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  position: relative;
  z-index: 3;
  margin-top: -30px;
}

@media (min-width: 1600px) {
  .content-wrap {
    padding: 0 60px;
  }
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
  color: #ff5c72;
  margin-right: 8px;
}

.section-count {
  font-size: 13px;
  color: #aaa;
}

/* ====== Category Tabs ====== */
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.cat-tab {
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.5);
  color: #666;
  transition: all 0.3s;
  user-select: none;
}

.cat-tab:hover {
  background: rgba(255, 92, 114, 0.1);
  color: #ff5c72;
}

.cat-tab.active {
  background: #ff5c72;
  color: #fff;
}

/* ====== Articles Layout (with sidebar pagination) ====== */
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
  font-size: 12px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #D87CFF;
  transition: all 0.2s;
  user-select: none;
}

.page-arrow:hover { background: rgba(216, 124, 255, 0.15); }
.page-arrow.off { opacity: 0.25; cursor: not-allowed; }

.page-num {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: #888;
  transition: all 0.2s;
  user-select: none;
}

.page-num:hover { background: rgba(255, 92, 114, 0.1); color: #ff5c72; }
.page-num.on { background: #ff5c72; color: #fff; }
.page-num.dot { cursor: default; color: #ccc; font-size: 12px; }

/* Cute loading animation */
.loading-anime {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
}

/* Cute CSS cat face loader */
.loading-cat {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ff5c72, #D87CFF);
  border-radius: 50%;
  position: relative;
  animation: catBounce 1s ease-in-out infinite;
  box-shadow: 0 4px 20px rgba(216, 124, 255, 0.25);
}

/* Cat ears */
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

/* Cat eyes */
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

/* Cat mouth */
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

.articles-main {
  flex: 1;
  min-width: 0;
}

/* ====== Article Grid ====== */
.article-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  padding-bottom: 40px;
}

/* Page flip transition */
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

.page-next-leave-to {
  opacity: 0;
  transform: translateX(-40px) scale(0.97);
}

.page-next-enter-from {
  opacity: 0;
  transform: translateX(40px) scale(0.97);
}

.page-prev-leave-to {
  opacity: 0;
  transform: translateX(40px) scale(0.97);
}

.page-prev-enter-from {
  opacity: 0;
  transform: translateX(-40px) scale(0.97);
}

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

.post-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-meta-item i {
  font-size: 13px;
}

@media (max-width: 1024px) {
  .article-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .page-banner { height: 300px; }
  .banner-title { font-size: 24px; }
  .banner-desc { font-size: 14px; }
  .article-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .post-card { height: 280px; }
  .card-img { height: 150px; }
  .card-title { font-size: 14px; }
  .card-summary { font-size: 12px; -webkit-line-clamp: 1; }
  .card-body { padding: 10px 12px 12px; }
  .content-wrap { padding: 0 12px; }
  .category-tabs { gap: 6px; }
  .cat-tab { padding: 4px 12px; font-size: 12px; }

  /* Sidebar pagination → bottom on mobile */
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
  .page-banner { height: 220px; }
  .banner-title { font-size: 20px; }
  .article-grid { grid-template-columns: 1fr; }
  .post-card { height: 260px; }
  .card-img { height: 140px; }
  .banner-skew { height: 30px; }
  .banner-skew svg { display: none; }
}
</style>
