<template>
  <nav class="navbar navbar-custom">
    <div class="navbar-inner">
      <router-link to="/" class="navbar-brand">
        <span class="logo-text">萌域</span>
        <span class="logo-sub">✦ 分享热爱</span>
      </router-link>

      <div class="navbar-menu">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          <i class="fa fa-home"></i> 首页
        </router-link>

        <router-link to="/resources" class="nav-item" :class="{ active: $route.path === '/resources' }">
          <i class="fa fa-archive"></i> 资源分享
        </router-link>

        <router-link to="/articles/create" class="nav-item" v-if="isLoggedIn">
          <i class="fa fa-pencil"></i> 写文章
        </router-link>

        <router-link to="/admin" class="nav-item" v-if="isAdmin">
          <i class="fa fa-dashboard"></i> 管理
        </router-link>
      </div>

      <div class="navbar-actions">
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="nav-btn">登录</router-link>
          <router-link to="/register" class="nav-btn nav-btn-primary">注册</router-link>
        </template>

        <template v-else>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar" class="user-avatar">
                {{ user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ user?.nickname || user?.username }}</span>
              <i class="fa fa-chevron-down" style="font-size: 10px; color: rgba(255,255,255,0.6); margin-left: 4px;"></i>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/profile">
                  <i class="fa fa-user"></i> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="/logout" divided>
                  <i class="fa fa-sign-out"></i> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const refreshAuth = () => {
  const token = localStorage.getItem('token')
  const raw = localStorage.getItem('user') || '{}'
  try {
    const u = JSON.parse(raw)
    isLoggedIn.value = !!token
    user.value = u
    isAdmin.value = u.role === 'admin'
  } catch { }
}

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const isLoggedIn = ref(!!localStorage.getItem('token'))
const isAdmin = ref(user.value.role === 'admin')

// Re-check auth on every route change
watch(() => route.path, refreshAuth, { immediate: true })

const handleCommand = (cmd) => {
  if (cmd === '/logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    refreshAuth()
    router.push('/')
    return
  }
  router.push(cmd)
}
</script>

<style scoped>
.navbar-custom {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(30, 30, 40, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  height: 64px;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}

.navbar-brand {
  display: flex;
  align-items: baseline;
  gap: 6px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
}

.logo-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-item {
  padding: 6px 14px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.nav-item i {
  margin-right: 4px;
  font-size: 13px;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.nav-btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s;
}

.nav-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.nav-btn-primary {
  background: #ff5c72;
  color: #fff !important;
}

.nav-btn-primary:hover {
  background: #ff7a8a;
  box-shadow: 0 2px 8px rgba(255, 92, 114, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.user-name {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .navbar-inner { gap: 12px; padding: 0 12px; }
  .logo-text { font-size: 17px; }
  .logo-sub { display: none; }
  .navbar-menu { gap: 2px; }
  .nav-item { padding: 4px 8px; font-size: 12px; }
  .nav-btn { padding: 4px 10px; font-size: 12px; }
  .user-name { max-width: 60px; font-size: 12px; }
}

@media (max-width: 480px) {
  .navbar-menu .nav-item i { margin-right: 0; }
  .user-name { display: none; }
}
</style>
