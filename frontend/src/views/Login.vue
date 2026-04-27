<template>
  <div class="auth-page">
    <div class="auth-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
    <div class="auth-overlay"></div>
    <div class="auth-container">
      <div class="auth-card card">
        <div class="title text-gradient">🌸 欢迎回来</div>
        <div class="subtitle">登录你的萌域账号</div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="输入用户名" :prefix-icon="User" size="large" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="btn-custom" style="width: 100%" :loading="loading" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div style="text-align: center; margin-top: 16px;">
          <span style="color: #999;">还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { getUserInfo } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const bgImage = 'https://cdn.nekosia.cat/images/catgirl/66e5ff45d3588e33bdb758dd-compressed.jpg'

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      const userRes = await getUserInfo()
      if (userRes.code === 200) {
        localStorage.setItem('user', JSON.stringify(userRes.data))
      }
      ElMessage.success('登录成功！')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center 30%;
  background-color: #1a1a3e;
  animation: fadeIn 1s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(1.05); }
  to { opacity: 1; transform: scale(1); }
}

.auth-overlay {
  position: absolute;
  inset: 0;
  background: rgba(20, 20, 40, 0.35);
}

.auth-container {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.auth-card {
  max-width: 420px;
  width: 100%;
  padding: 36px 40px;
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: none !important;
  -webkit-backdrop-filter: none !important;
}

@media (max-width: 768px) {
  .auth-page { position: fixed; top: 56px; }
  .auth-card { padding: 24px 20px; max-width: 92%; }
}
</style>
