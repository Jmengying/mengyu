<template>
  <div class="page-container">
  <div class="card" style="max-width: 900px; margin: 0 auto;">
    <h2 class="text-gradient" style="margin-bottom: 8px;">
      {{ pageTitle }}
    </h2>
    <p class="page-subtitle">{{ pageSubtitle }}</p>

    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="文章标题" prop="title">
        <el-input v-model="form.title" placeholder="输入文章标题" size="large" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="分类">
        <el-radio-group v-model="form.category">
          <el-radio-button value="general">📝 随笔</el-radio-button>
          <el-radio-button value="resource">📦 资源分享</el-radio-button>
          <el-radio-button value="discussion">💬 讨论</el-radio-button>
          <el-radio-button value="art">🎨 绘画</el-radio-button>
          <el-radio-button value="notice" v-if="isAdmin">📢 通知</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="封面图片（选填）">
        <el-upload
          class="cover-uploader"
          :show-file-list="false"
          :http-request="handleCoverUpload"
          accept="image/*"
        >
          <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
          <div v-else class="cover-upload-placeholder">
            <el-icon :size="32"><Plus /></el-icon>
            <span>上传封面</span>
          </div>
        </el-upload>
      </el-form-item>

      <el-form-item label="文章摘要（选填）">
        <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="写一段简短的摘要..." maxlength="300" show-word-limit />
      </el-form-item>

      <!-- Resource links (only for resource category) -->
      <el-form-item label="资源链接" v-if="form.category === 'resource'">
        <el-input
          v-model="form.resourceLinks"
          type="textarea"
          :rows="5"
          placeholder="输入网盘链接等信息，每行一个&#10;例如：&#10;百度网盘: https://pan.baidu.com/s/xxxxx&#10;提取码: 1234&#10;天翼云: https://cloud.189.cn/t/xxxxx&#10;备注: 解压密码 xxxx"
        />
        <div style="font-size: 12px; color: #999; margin-top: 6px;">
          <i class="fa fa-info-circle"></i> 评论后可见，请确保链接有效
        </div>
      </el-form-item>

      <!-- Resource file upload (only for resource category) -->
      <el-form-item label="上传文件" v-if="form.category === 'resource'">
        <div style="width: 100%;">
          <el-upload
            :show-file-list="false"
            :http-request="handleFileUpload"
          >
            <div class="cover-upload-placeholder" style="height: 100px; width: 100%;">
              <el-icon :size="24"><Upload /></el-icon>
              <span style="font-size: 13px; margin-top: 4px;">{{ form.resourceFile ? '已上传' : '上传压缩包' }}</span>
            </div>
          </el-upload>
          <el-progress v-if="uploadProgress > 0 && uploadProgress < 100"
            :percentage="uploadProgress"
            :stroke-width="8"
            style="margin-top: 8px;" />
          <div v-if="form.resourceFile && uploadProgress === 0" style="margin-top: 8px; font-size: 13px; color: #D87CFF;">
            <i class="fa fa-check-circle"></i> 已上传: <a :href="form.resourceFile" target="_blank">{{ form.resourceFile.split('/').pop() }}</a>
          </div>
          <div style="font-size: 12px; color: #999; margin-top: 6px;">
            <i class="fa fa-info-circle"></i> 支持 zip / rar / 7z 格式，最大 50MB
          </div>
        </div>
      </el-form-item>

      <el-form-item label="文章内容" prop="content">
        <div style="border: 1px solid #ddd; border-radius: 8px; overflow: hidden; width: 100%;">
          <Toolbar
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            mode="default"
            style="border-bottom: 1px solid #ddd;"
          />
          <Editor
            v-model="form.content"
            :defaultConfig="editorConfig"
            mode="default"
            style="height: 500px; overflow-y: hidden;"
            @onCreated="handleCreated"
          />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button class="btn-custom" :loading="submitting" @click="handleSubmit">
          {{ submitText }}
        </el-button>
        <el-button @click="$router.back()" style="margin-left: 12px;">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, shallowRef, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { createArticle, getArticleDetail, updateArticle } from '../api/article'
import { uploadFile } from '../api/user'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const uploadProgress = ref(0)

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'admin'
const isEdit = route.name === 'EditArticle'
const articleId = route.params.id

const form = reactive({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  category: 'general',
  resourceLinks: '',
  resourceFile: ''
})

const pageTitle = computed(() => {
  if (isEdit) return '📝 编辑文章'
  const titles = {
    resource: '📦 分享资源',
    discussion: '💬 发起讨论',
    art: '🎨 发布绘画',
    notice: '📢 发布通知',
    general: '📝 写文章'
  }
  return titles[form.category] || '📝 写文章'
})

const pageSubtitle = computed(() => {
  if (isEdit) return '修改你的文章内容'
  const subs = {
    resource: '分享网盘资源，评论后可见链接',
    discussion: '发起一个话题和大家讨论',
    art: '展示你的绘画作品',
    notice: '发布站内通知公告',
    general: '分享你的想法和故事'
  }
  return subs[form.category] || '分享你的想法和故事'
})

const submitText = computed(() => {
  if (isEdit) return '保存修改'
  const btns = {
    resource: '发布资源',
    discussion: '发布话题',
    art: '发布绘画',
    notice: '发布通知',
    general: '发布文章'
  }
  return btns[form.category] || '发布文章'
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'change' }]
}

// WangEditor
const editorRef = shallowRef(null)
const toolbarConfig = {}
const editorConfig = {
  placeholder: '开始写点什么吧...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const res = await uploadFile(file)
          if (res.code === 200) {
            insertFn(res.data.url)
          } else {
            ElMessage.error('图片上传失败')
          }
        } catch (e) {
          ElMessage.error('图片上传失败')
        }
      }
    }
  }
}

const handleCreated = (editor) => {
  editorRef.value = editor
  import('../utils/clickEffect').then(({ triggerDeleteEffect }) => {
    const el = editor.getEditableContainer?.() || document.querySelector('.w-e-text')
    if (el) {
      el.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' || e.key === 'Delete') {
          triggerDeleteEffect(el)
        }
      })
    }
  })
}

const handleCoverUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    if (res.code === 200) {
      form.coverImage = res.data.url
      ElMessage.success('封面上传成功')
    }
  } catch (e) {
    ElMessage.error('封面上传失败')
  }
}

const handleFileUpload = async (options) => {
  uploadProgress.value = 0
  try {
    const res = await uploadFile(options.file, (e) => {
      if (e.total) uploadProgress.value = Math.round((e.loaded / e.total) * 100)
    })
    if (res.code === 200) {
      form.resourceFile = res.data.url
      uploadProgress.value = 100
      setTimeout(() => { uploadProgress.value = 0 }, 2000)
      ElMessage.success('文件上传成功')
    } else {
      uploadProgress.value = 0
      ElMessage.error(res.message || '文件上传失败')
    }
  } catch (e) {
    uploadProgress.value = 0
    ElMessage.error('文件上传失败: ' + (e.message || ''))
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (isEdit) {
      res = await updateArticle(articleId, form)
    } else {
      res = await createArticle(form)
    }

    if (res.code === 200) {
      const successMsg = isEdit ? '修改成功！' : (pageTitle.value.replace(/^[^\s]+\s/, '') + '成功！')
      ElMessage.success(successMsg)
      if (isEdit) {
        window.location.href = `/articles/${articleId}`
      } else {
        router.push(`/articles/${res.data.id}`)
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (isEdit && articleId) {
    try {
      const res = await getArticleDetail(articleId)
      if (res.code === 200) {
        form.title = res.data.title
        form.content = res.data.content
        form.summary = res.data.summary || ''
        form.coverImage = res.data.coverImage || ''
      }
    } catch (e) {
      ElMessage.error('加载文章失败')
    }
  }
})

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})
</script>

<style src="@wangeditor/editor/dist/css/style.css"></style>

<style scoped>
.cover-uploader {
  display: block;
}

.cover-preview {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.cover-upload-placeholder {
  width: 100%;
  height: 180px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #aaa;
  transition: all 0.3s;
}

.cover-upload-placeholder:hover {
  border-color: #ff6b9d;
  color: #ff6b9d;
}

.cover-upload-placeholder span {
  margin-top: 8px;
  font-size: 14px;
}
</style>
