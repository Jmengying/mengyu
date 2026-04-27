<template>
  <div class="profile-page">
    <div class="card profile-header">
      <div class="profile-avatar-section">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :http-request="handleAvatarUpload"
        >
          <el-avatar :size="120" :src="user.avatar" class="profile-avatar">
            {{ user.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
        </el-upload>
        <h2>{{ user.nickname || user.username }}</h2>
        <p class="user-bio">{{ user.bio || '这个用户很懒，什么都没写~' }}</p>
      </div>
    </div>

    <div class="card" style="margin-top: 24px;">
      <h3 style="margin-bottom: 24px;">编辑个人信息</h3>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="user.username" disabled size="large" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="输入昵称" size="large" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="输入邮箱" size="large" />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input v-model="form.bio" type="textarea" :rows="4" placeholder="介绍一下自己吧~" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button class="btn-custom" :loading="saving" @click="handleUpdate">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card" style="margin-top: 24px;">
      <h3 style="margin-bottom: 4px;">🔑 修改密码</h3>
      <p style="font-size: 13px; color: #999; margin-bottom: 20px;">修改后需要重新登录</p>
      <el-form :model="pwForm" :rules="pwRules" ref="pwFormRef" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwForm.oldPassword" type="password" placeholder="输入原密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwForm.newPassword" type="password" placeholder="输入新密码（至少6位）" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwForm.confirmPassword" type="password" placeholder="再次输入新密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button class="btn-custom" :loading="pwLoading" @click="handleChangePw">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, uploadFile } from '../api/user'
import request from '../api/request'

const formRef = ref(null)
const saving = ref(false)

const pwFormRef = ref(null)
const pwLoading = ref(false)
const pwForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, {
    validator: (rule, value, callback) => value === pwForm.newPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur'
  }]
}

const handleChangePw = async () => {
  const valid = await pwFormRef.value.validate().catch(() => {})
  if (!valid) return
  pwLoading.value = true
  try {
    const res = await request.put('/api/user/change-password', {
      oldPassword: pwForm.oldPassword,
      newPassword: pwForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      pwForm.oldPassword = ''
      pwForm.newPassword = ''
      pwForm.confirmPassword = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setTimeout(() => window.location.href = '/login', 1500)
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {}
  finally { pwLoading.value = false }
}

const user = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  bio: ''
})

const form = reactive({
  nickname: '',
  email: '',
  bio: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }]
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    if (res.code === 200) {
      const avatarUrl = res.data.url
      user.value.avatar = avatarUrl
      // Update server
      await updateUserInfo({ avatar: avatarUrl })
      // Update local storage
      const stored = JSON.parse(localStorage.getItem('user') || '{}')
      stored.avatar = avatarUrl
      localStorage.setItem('user', JSON.stringify(stored))
      ElMessage.success('头像更新成功')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const handleUpdate = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return

  saving.value = true
  try {
    const res = await updateUserInfo({
      nickname: form.nickname,
      email: form.email,
      bio: form.bio
    })
    if (res.code === 200) {
      localStorage.setItem('user', JSON.stringify(res.data))
      user.value = res.data
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // handled
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      user.value = res.data
      form.nickname = res.data.nickname || ''
      form.email = res.data.email || ''
      form.bio = res.data.bio || ''
      localStorage.setItem('user', JSON.stringify(res.data))
    }
  } catch (e) {
    console.error('Failed to load user info:', e)
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.profile-header {
  text-align: center;
  padding: 40px;
}

.profile-avatar-section {
  position: relative;
  display: inline-block;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.profile-avatar {
  border: 4px solid rgba(255, 107, 157, 0.2);
  transition: all 0.3s;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  font-size: 12px;
  margin-top: 4px;
}

.user-bio {
  color: #888;
  font-size: 14px;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .profile-page { padding: 0; }
  .profile-header { padding: 24px 16px; }
}
</style>
