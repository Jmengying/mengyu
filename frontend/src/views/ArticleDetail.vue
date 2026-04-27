<template>
  <div class="page-container">
    <div v-if="loading" style="text-align: center; padding: 80px 0;">
      <div class="loading-anime">
        <div class="loading-cat"><div class="eyes"></div><div class="mouth"></div></div>
      </div>
      <p style="margin-top: 16px; color: #D87CFF; font-size: 13px;">正在拼命加载中...</p>
    </div>

    <div v-else-if="!article" class="empty-state card">
      <i class="fa fa-warning" style="font-size: 48px;"></i>
      <p>文章不存在</p>
      <el-button class="btn-custom" style="margin-top: 16px;" @click="$router.push('/')">返回首页</el-button>
    </div>

    <template v-else>
      <div class="article-header card">
        <div class="article-cover-banner" :style="!article.coverImage ? 'background: #1a1a3e;' : ''">
          <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
          <img v-else :src="defaultCovers[article.id % defaultCovers.length]" :alt="article.title" />
        </div>

        <div class="article-meta-top">
          <el-tag round effect="plain" color="#fce4ec" style="color: #ff5c72;">
            <i class="fa fa-clock-o"></i> {{ formatDate(article.createdAt) }}
          </el-tag>
          <el-tag round effect="plain" color="#e8eaf6" v-if="article.updatedAt !== article.createdAt" style="color: #D87CFF;">
            <i class="fa fa-edit"></i> 更新于 {{ formatDate(article.updatedAt) }}
          </el-tag>
          <el-tag round effect="plain" color="#ede0f5" style="color: #D87CFF;">
            <i class="fa fa-eye"></i> {{ article.views || 0 }} 次浏览
          </el-tag>
        </div>

        <h1 class="article-title">
          <el-tag v-if="article.isPinned" size="small" color="#ff5c72" style="color: #fff; border: none; vertical-align: middle; margin-right: 8px;">
            <i class="fa fa-thumb-tack"></i> 置顶
          </el-tag>
          {{ article.title }}
        </h1>

        <div class="author-bar">
          <router-link :to="`/user/${article.userId}`" class="author-info">
            <el-avatar :size="40" :src="article.authorAvatar">
              {{ article.authorName?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="author-name">{{ article.authorName || '匿名' }}</span>
          </router-link>

          <div class="article-actions" v-if="isOwner">
            <el-button @click="$router.push(`/articles/${article.id}/edit`)" :icon="Edit" size="small">
              编辑
            </el-button>
            <el-button @click="handleDelete" :icon="Delete" size="small" type="danger" plain>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="article-content card" v-html="article.content"></div>

      <!-- Resource links section (only for resource category, visible after comment) -->
      <div class="card resource-section" v-if="article.category === 'resource' && (article.resourceLinks || article.resourceFile)">
        <h3 style="margin-bottom: 16px;">
          <i class="fa fa-download"></i> 资源下载
        </h3>

        <template v-if="hasCommented">
          <!-- File download -->
          <div v-if="article.resourceFile" style="margin-bottom: 12px;">
            <a :href="article.resourceFile" class="resource-download-btn" target="_blank" download>
              <i class="fa fa-file-archive-o"></i> 下载文件：{{ article.resourceFile.split('/').pop() }}
            </a>
          </div>
          <!-- Links -->
          <div v-if="article.resourceLinks" class="resource-links">
            <div v-for="(line, idx) in resourceLinkLines" :key="idx" class="resource-link-item">
              <i class="fa fa-link"></i> {{ line }}
            </div>
          </div>
        </template>

        <template v-else>
          <div class="resource-locked">
            <i class="fa fa-lock" style="font-size: 32px; color: #D87CFF;"></i>
            <p style="margin: 12px 0 4px; font-weight: 500;">评论后可见资源链接</p>
            <p style="font-size: 13px; color: #999;">发表评论后即可查看下载内容</p>
          </div>
        </template>
      </div>

      <div class="comment-section card">
        <h3 style="margin-bottom: 20px;">
          <i class="fa fa-comments-o"></i>
          评论 ({{ comments.length }})
        </h3>

        <div v-if="isLoggedIn" class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            :placeholder="replyTo ? `回复 @${replyToName}：` : '写下你的评论...'"
          />
          <div style="margin-top: 12px; display: flex; gap: 8px; justify-content: flex-end;">
            <el-button v-if="replyTo" size="small" @click="cancelReply">取消回复</el-button>
            <el-button class="btn-custom" size="small" :loading="commentLoading" @click="handleComment">
              <i class="fa fa-send-o"></i> 发表评论
            </el-button>
          </div>
        </div>

        <div v-else style="text-align: center; padding: 20px; color: #999;">
          请 <router-link to="/login">登录</router-link> 后发表评论
        </div>

        <div v-if="comments.length === 0" style="text-align: center; padding: 40px; color: #aaa;">
          <i class="fa fa-comments-o" style="font-size: 32px;"></i>
          <p style="margin-top: 8px;">暂无评论，快来抢沙发吧~</p>
        </div>

        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <router-link :to="`/user/${comment.userId}`" class="comment-author">
              <el-avatar :size="32" :src="comment.authorAvatar">
                {{ comment.authorName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="comment-name">{{ comment.authorName || '匿名' }}</span>
            </router-link>
            <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
          </div>

          <div class="comment-body">
            <span v-if="comment.replyTo" style="color: #ff5c72;">@{{ comment.replyTo }} </span>
            {{ comment.content }}
          </div>

          <div class="comment-actions">
            <el-button size="small" text @click="replyToComment(comment)"><i class="fa fa-reply"></i> 回复</el-button>
            <el-button
              v-if="isLoggedIn && comment.userId === currentUserId"
              size="small"
              text
              type="danger"
              @click="handleDeleteComment(comment.id)"
            >
              <i class="fa fa-trash-o"></i> 删除
            </el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { getArticleDetail, deleteArticle } from '../api/article'
import { getComments, createComment, deleteComment } from '../api/comment'

const route = useRoute()
const router = useRouter()

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

const article = ref(null)
const comments = ref([])
const loading = ref(true)
const newComment = ref('')
const commentLoading = ref(false)
const replyTo = ref(null)
const replyToName = ref('')

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isLoggedIn = ref(!!localStorage.getItem('token'))
const currentUserId = ref(user.id)

const isOwner = computed(() => {
  return isLoggedIn.value && article.value && article.value.userId === currentUserId.value
})

const hasCommented = ref(false)
const checkHasCommented = () => {
  if (!isLoggedIn.value || !comments.value.length || !currentUserId.value) {
    hasCommented.value = false
    return
  }
  hasCommented.value = comments.value.some(c => c.userId === currentUserId.value)
}

const resourceLinkLines = computed(() => {
  if (!article.value?.resourceLinks) return []
  return article.value.resourceLinks.split('\n').filter(l => l.trim())
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteArticle(article.value.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const cancelReply = () => {
  replyTo.value = null
  replyToName.value = ''
}

const replyToComment = (comment) => {
  replyTo.value = comment.id
  replyToName.value = comment.authorName
}

const handleComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true
  try {
    const data = {
      articleId: parseInt(route.params.id),
      content: newComment.value
    }
    if (replyTo.value) {
      data.parentId = replyTo.value
    }

    const res = await createComment(data)
    if (res.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      replyTo.value = null
      replyToName.value = ''
      loadComments()
    }
  } catch (e) {
    // handled
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    const res = await deleteComment(commentId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadComments()
    }
  } catch (e) {
    // handled
  }
}

const loadComments = async () => {
  try {
    const res = await getComments(route.params.id)
    if (res.code === 200) {
      comments.value = res.data || []
      checkHasCommented()
    }
  } catch (e) {
    console.error('Failed to load comments:', e)
  }
}

onMounted(async () => {
  try {
    const res = await getArticleDetail(route.params.id)
    if (res.code === 200) {
      article.value = res.data
    }
  } catch (e) {
    console.error('Failed to load article:', e)
  } finally {
    loading.value = false
  }

  if (route.params.id) {
    loadComments()
  }
})
</script>

<style scoped>
.article-header {
  margin-bottom: 24px;
  padding: 0;
  overflow: hidden;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.article-cover-banner {
  width: 100%;
  height: 360px;
  overflow: hidden;
  background: #f5f0f8;
}

.article-cover-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.article-meta-top {
  padding: 24px 24px 0;
  display: flex;
  gap: 8px;
}

.article-title {
  font-size: 26px;
  font-weight: 600;
  padding: 12px 24px 0;
  line-height: 1.4;
}

.author-bar {
  padding: 20px 24px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: inherit;
}

.author-name {
  font-size: 15px;
  font-weight: 500;
}

.article-actions {
  display: flex;
  gap: 8px;
}

.article-content {
  margin-bottom: 24px;
  padding: 40px;
  line-height: 1.9;
  font-size: 15px;
  overflow: hidden;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px auto;
  display: block;
  width: auto;
}

.article-content :deep(img[style*="float"]) {
  margin: 8px 16px;
}

/* Responsive table */
.article-content :deep(table) {
  max-width: 100%;
  width: 100% !important;
  overflow-x: auto;
  display: block;
}

/* Responsive video embeds */
.article-content :deep(iframe) {
  max-width: 100%;
  width: 100%;
}

/* Images in paragraphs */
.article-content :deep(p) {
  max-width: 100%;
  overflow-wrap: break-word;
}

.article-content :deep(p > img:only-child) {
  margin: 24px auto;
}

.article-content :deep(p > img:only-child) {
  margin: 24px auto;
}

.article-content :deep(table) {
  max-width: 100%;
  overflow-x: auto;
  display: block;
}

.article-content :deep(pre) {
  max-width: 100%;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.resource-section {
  margin-bottom: 24px;
}

.resource-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-download-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #ff5c72, #D87CFF);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
}

.resource-download-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(216, 124, 255, 0.3);
  color: #fff;
}

.resource-link-item {
  padding: 10px 14px;
  background: rgba(216, 124, 255, 0.08);
  border-radius: 8px;
  font-size: 14px;
  color: #444;
  word-break: break-all;
}

.resource-link-item i {
  color: #D87CFF;
  margin-right: 8px;
}

.resource-locked {
  text-align: center;
  padding: 24px;
  background: rgba(216, 124, 255, 0.05);
  border-radius: 12px;
  border: 1px dashed rgba(216, 124, 255, 0.2);
}

.comment-section {
  margin-bottom: 24px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.resource-section {
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.comment-input {
  margin-bottom: 24px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: inherit;
}

.comment-name {
  font-weight: 500;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #aaa;
}

.comment-body {
  font-size: 14px;
  line-height: 1.6;
  padding-left: 40px;
}

.comment-actions {
  padding-left: 40px;
  margin-top: 4px;
}

/* Loading animation */
.loading-anime {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 60px;
}

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

@media (max-width: 768px) {
  .article-header { max-width: 100%; }
  .article-title { font-size: 20px; padding: 8px 16px 0; }
  .article-meta-top { padding: 16px 16px 0; flex-wrap: wrap; }
  .article-meta-top .el-tag { font-size: 11px; }
  .author-bar { padding: 12px 16px 16px; flex-wrap: wrap; gap: 8px; }
  .article-content { padding: 20px 16px; max-width: 100%; font-size: 14px; }
  .comment-section { max-width: 100%; }
  .resource-section { max-width: 100%; }
  .comment-body { padding-left: 0; }
  .comment-actions { padding-left: 0; }
  .article-cover-banner { height: 220px; }
}
</style>
