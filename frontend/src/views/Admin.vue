<template>
  <div class="page-container">
    <div style="margin-bottom: 24px;">
      <h2 class="text-gradient">⚙️ 管理面板</h2>
      <p class="page-subtitle">管理员专用 — 管理所有用户和文章</p>
    </div>

    <el-tabs v-model="activeTab" type="card" class="admin-tabs">
      <el-tab-pane label="👥 用户管理" name="users">
        <div class="card" v-loading="usersLoading">
          <el-table :data="users" stripe style="width: 100%" v-if="!usersLoading">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" width="120" />
            <el-table-column prop="email" label="邮箱" width="180" />
            <el-table-column prop="role" label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small" effect="plain">
                  {{ row.role === 'admin' ? '管理员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bio" label="简介" min-width="150" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="注册时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-popconfirm
                  title="确定删除此用户？（用户的所有文章和评论也会被删除）"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="handleDeleteUser(row)"
                >
                  <template #reference>
                    <el-button
                      size="small"
                      type="danger"
                      plain
                      :disabled="row.role === 'admin'"
                      :icon="Delete"
                    >
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="📝 文章管理" name="articles">
        <div class="card" v-loading="articlesLoading">
          <el-table :data="articles" stripe style="width: 100%" v-if="!articlesLoading">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorName" label="作者" width="120" />
            <el-table-column label="评论数" width="90">
              <template #default="{ row }">{{ row.commentCount || 0 }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-popconfirm
                  title="确定删除此文章？（评论也会被删除）"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                  @confirm="handleDeleteArticle(row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger" plain :icon="Delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="📰 每日新闻" name="news">
        <div class="card" style="text-align: center; padding: 60px 40px;">
          <el-button class="btn-custom" :loading="newsLoading" @click="triggerDailyNews" size="large" style="font-size: 18px; padding: 14px 40px !important;">
            <i class="fa fa-paper-plane"></i> 手动发布每日新闻
          </el-button>

          <div style="margin-top: 24px;">
            <p style="font-size: 14px; color: #666; margin-bottom: 12px;">自动发布时段（每天3次）：</p>
            <div style="display: flex; align-items: center; justify-content: center; gap: 8px; flex-wrap: wrap;">
              <template v-for="(h, i) in publishHours" :key="i">
                <el-select v-model="publishHours[i]" style="width: 100px;" @change="handleHourChange">
                  <el-option v-for="ho in 24" :key="ho-1" :label="(ho-1)+':00'" :value="ho-1" />
                </el-select>
                <span v-if="i < 2" style="color: #ccc;">|</span>
              </template>
            </div>
          </div>
          <div v-if="timeSaving" style="margin-top: 8px;">
            <span style="font-size: 13px; color: #D87CFF;">保存中...</span>
          </div>

          <p style="margin-top: 16px; color: #999; font-size: 13px;">
            <i class="fa fa-rss"></i> 来源：HOTACG 中文动漫新闻<br/>
            <i class="fa fa-filter"></i> 已过滤漫展/游戏/周边内容
          </p>
          <div v-if="lastNewsTitle" style="margin-top: 24px; text-align: left; background: rgba(255,255,255,0.5); padding: 16px; border-radius: 8px;">
            <p style="font-size: 13px; color: #888;">最近发布：</p>
            <p style="font-size: 14px; margin-top: 4px;">{{ lastNewsTitle }}</p>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getAdminUsers, adminDeleteUser, getAdminArticles, adminDeleteArticle } from '../api/admin'
import request from '../api/request'

const activeTab = ref('users')
const newsLoading = ref(false)
const lastNewsTitle = ref('')
const publishHours = ref([8, 14, 20])
const timeSaving = ref(false)

const loadPublishTime = async () => {
  try {
    const res = await request.get('/api/admin/daily-news-time')
    if (res.code === 200) publishHours.value = res.data.hours
  } catch (e) {}
}

const handleHourChange = async () => {
  timeSaving.value = true
  try {
    const res = await request.put('/api/admin/daily-news-time', { hours: publishHours.value })
    if (res.code === 200) {
      ElMessage.success(`发布时段已更新: ${publishHours.value.join(':00, ')}:00`)
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {}
  finally { timeSaving.value = false }
}

const triggerDailyNews = async () => {
  newsLoading.value = true
  try {
    const res = await request.post('/api/admin/trigger-daily-news')
    if (res.code === 200) {
      ElMessage.success('每日新闻已发布！')
      // Fetch last news title
      const articlesRes = await getAdminArticles()
      if (articlesRes.code === 200 && articlesRes.data.length > 0) {
        const botArticles = articlesRes.data.filter(a => a.authorName === '萌域小助手' || a.userId === 4)
        if (botArticles.length > 0) lastNewsTitle.value = botArticles[0].title
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {}
  finally { newsLoading.value = false }
}

const users = ref([])
const articles = ref([])
const usersLoading = ref(true)
const articlesLoading = ref(true)

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const loadUsers = async () => {
  usersLoading.value = true
  try {
    const res = await getAdminUsers()
    if (res.code === 200) {
      users.value = res.data || []
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // handled
  } finally {
    usersLoading.value = false
  }
}

const loadArticles = async () => {
  articlesLoading.value = true
  try {
    const res = await getAdminArticles()
    if (res.code === 200) {
      articles.value = res.data || []
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // handled
  } finally {
    articlesLoading.value = false
  }
}

const handleDeleteUser = async (user) => {
  try {
    const res = await adminDeleteUser(user.id)
    if (res.code === 200) {
      ElMessage.success(`已删除用户: ${user.username}`)
      loadUsers()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // handled
  }
}

const handleDeleteArticle = async (article) => {
  try {
    const res = await adminDeleteArticle(article.id)
    if (res.code === 200) {
      ElMessage.success(`已删除文章: ${article.title}`)
      loadArticles()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // handled
  }
}

onMounted(() => {
  loadUsers()
  loadArticles()
  loadPublishTime()
})
</script>

<style scoped>
.admin-tabs {
  margin-top: 8px;
}

.admin-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 24px;
}

@media (max-width: 768px) {
  .admin-tabs :deep(.el-tabs__item) { font-size: 13px; padding: 0 12px; }
  .admin-tabs :deep(.el-table) { font-size: 12px; }
}
</style>
