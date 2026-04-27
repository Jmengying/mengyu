<template>
  <div class="auth-page">
    <div class="auth-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
    <div class="auth-overlay"></div>
    <div class="auth-container">
      <div class="auth-card card">
        <div class="title text-gradient">🌸 加入我们</div>
        <div class="subtitle">创建一个萌域账号</div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="输入用户名" :prefix-icon="User" size="large" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="输入昵称（选填）" :prefix-icon="EditPen" size="large" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="输入邮箱（选填）" :prefix-icon="Message" size="large" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="btn-custom" style="width: 100%" :loading="loading" @click="handleRegister">
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div style="text-align: center; margin-top: 16px;">
          <span style="color: #999;">已有账号？</span>
          <router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, EditPen, Message } from '@element-plus/icons-vue'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const bgImage = 'https://cdn.nekosia.cat/images/catgirl/66a6d05857d49d61385d0312-compressed.jpg'

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass2, trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return

  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      email: form.email
    })
    if (res.code === 200) {
      ElMessage.success('注册成功！请登录')
      router.push('/login')
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
