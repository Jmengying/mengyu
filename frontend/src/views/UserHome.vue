<template>
  <div class="page-container">
    <div class="card user-header">
      <div style="text-align: center;">
        <el-avatar :size="100" :src="user.avatar">
          {{ user.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <h2 style="margin-top: 16px;">{{ user.nickname || user.username }}</h2>
        <p style="color: #888; font-size: 14px;">{{ user.bio || '这个用户很懒，什么都没写~' }}</p>
        <p style="color: #aaa; font-size: 13px; margin-top: 8px;">
          共 {{ articles.length }} 篇文章
        </p>
      </div>
    </div>

    <div v-if="articles.length === 0" class="empty-state card" style="margin-top: 24px;">
      <el-icon><Notebook /></el-icon>
      <p>还没有文章哦~</p>
    </div>

    <div v-else class="article-grid" style="margin-top: 24px;">
      <div v-for="article in articles" :key="article.id" class="article-card card" @click="$router.push(`/articles/${article.id}`)">
        <div class="article-cover" v-if="article.coverImage">
          <img :src="article.coverImage" :alt="article.title" />
        </div>
        <div class="article-info">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
          <div class="article-meta">
            <span>{{ formatDate(article.createdAt) }}</span>
            <span>{{ article.commentCount || 0 }} 评论</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Notebook } from '@element-plus/icons-vue'
import { getUserById } from '../api/user'
import { getUserArticles } from '../api/article'

const route = useRoute()
const user = ref({})
const articles = ref([])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}-${day}`
}

onMounted(async () => {
  const userId = route.params.id
  try {
    const [userRes, articleRes] = await Promise.all([
      getUserById(userId),
      getUserArticles(userId)
    ])
    if (userRes.code === 200) {
      user.value = userRes.data
    }
    if (articleRes.code === 200) {
      articles.value = articleRes.data || []
    }
  } catch (e) {
    console.error('Failed to load user home:', e)
  }
})
</script>

<style scoped>
.user-header {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px;
}

.article-card {
  cursor: pointer;
  overflow: hidden;
  padding: 0;
}

.article-cover {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

.article-info {
  padding: 20px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.article-summary {
  font-size: 14px;
  color: #888;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #aaa;
}
</style>
