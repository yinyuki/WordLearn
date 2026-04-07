<template>
  <div class="extension-container">
    <!-- 头部区域 -->
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2>📚 拓展学习</h2>
          <p class="sub-text">日语、英语、俄语精选文章、音频与视频资源</p>
        </div>
        <div v-if="isAdmin" class="header-actions">
          <el-button v-if="!sortMode" type="info" size="large" @click="enterSortMode">
            🔢 编辑顺序
          </el-button>
          <el-button v-else type="success" size="large" @click="saveSortOrder">
            ✅ 保存顺序
          </el-button>
          <el-button v-if="sortMode" type="default" size="large" @click="exitSortMode">
            取消
          </el-button>
          <el-button type="primary" size="large" @click="openAddDialog" class="add-btn">
            <el-icon class="el-icon--left"><Plus /></el-icon> 上传资源
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <div class="filter-group">
          <span class="filter-label">语言：</span>
          <el-radio-group v-model="filterLanguage" @change="loadResources">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="ENGLISH">🇬🇧 英语</el-radio-button>
            <el-radio-button label="JAPANESE">🇯🇵 日语</el-radio-button>
            <el-radio-button label="RUSSIAN">🇷🇺 俄语</el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">类型：</span>
          <el-radio-group v-model="filterType" @change="loadResources">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="ARTICLE">📄 文章</el-radio-button>
            <el-radio-button label="AUDIO">🎵 音频</el-radio-button>
            <el-radio-button label="VIDEO">🎬 视频</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 资源列表 -->
    <div v-loading="loading" class="resource-list">
      <el-empty v-if="resources.length === 0" description="暂无资源" />
      
      <!-- 排序模式提示 -->
      <div v-if="sortMode" class="sort-mode-hint">
        <el-alert title="排序模式" description="使用 ⬆️⬇️ 按钮调整顺序，完成后点击保存" type="info" :closable="false" />
      </div>
      
      <!-- 按类型分组显示（正常模式和排序模式共用） -->
      <template v-if="resources.length > 0">
        <!-- 文章区域 -->
        <div v-if="articleResources.length > 0" class="resource-section">
          <h3 class="section-title">📄 文章</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" v-for="(item, index) in articleResources" :key="item.id">
              <el-card class="resource-card" :class="{ 'is-pinned': item.isPinned, 'sort-mode-card': sortMode }" shadow="hover" @click="sortMode ? null : openDetail(item)">
                <div class="card-cover" :class="getCoverClass(item.type, item.language)">
                  <div class="cover-overlay">
                    <el-tag v-if="item.isPinned" type="warning" size="small" effect="dark">📌 置顶</el-tag>
                    <el-tag type="success" size="small">文章</el-tag>
                    <el-tag type="info" size="small" style="margin-left: 6px;">{{ getLanguageLabel(item.language) }}</el-tag>
                  </div>
                  <el-icon class="play-icon"><Document /></el-icon>
                  <!-- 排序模式显示序号 -->
                  <div v-if="sortMode" class="sort-number">{{ getResourceIndex(item.id) }}</div>
                </div>
                <div class="card-body">
                  <h4 class="card-title">{{ item.title }}</h4>
                  <p class="card-desc">{{ item.content ? truncate(item.content, 40) : '点击查看详情' }}</p>
                  <div class="card-footer">
                    <span class="card-time">{{ formatTime(item.createdAt) }}</span>
                    <div v-if="isAdmin" class="admin-actions" @click.stop>
                      <!-- 排序模式下显示上移下移按钮 -->
                      <template v-if="sortMode">
                        <el-button link type="primary" @click.stop="moveUp(item.id)">⬆️</el-button>
                        <el-button link type="primary" @click.stop="moveDown(item.id)">⬇️</el-button>
                      </template>
                      <template v-else>
                        <el-tooltip :content="item.isPinned ? '取消置顶' : '置顶'" placement="top">
                          <el-button link :type="item.isPinned ? 'warning' : 'info'" @click.stop="handlePin(item)">
                            <span style="font-size: 14px;">📌</span>
                          </el-button>
                        </el-tooltip>
                        <el-button link type="primary" :icon="Edit" @click.stop="openEditDialog(item)" />
                        <el-popconfirm title="确定删除？" @confirm="handleDelete(item.id)">
                          <template #reference>
                            <el-button link type="danger" :icon="Delete" />
                          </template>
                        </el-popconfirm>
                      </template>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 音频区域 -->
        <div v-if="audioResources.length > 0" class="resource-section">
          <h3 class="section-title">🎵 音频</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" v-for="(item, index) in audioResources" :key="item.id">
              <el-card class="resource-card" :class="{ 'is-pinned': item.isPinned, 'sort-mode-card': sortMode }" shadow="hover" @click="sortMode ? null : openDetail(item)">
                <div class="card-cover" :class="getCoverClass(item.type, item.language)">
                  <div class="cover-overlay">
                    <el-tag v-if="item.isPinned" type="warning" size="small" effect="dark">📌 置顶</el-tag>
                    <el-tag type="warning" size="small">音频</el-tag>
                    <el-tag type="info" size="small" style="margin-left: 6px;">{{ getLanguageLabel(item.language) }}</el-tag>
                  </div>
                  <el-icon class="play-icon"><Headset /></el-icon>
                  <div v-if="sortMode" class="sort-number">{{ getResourceIndex(item.id) }}</div>
                </div>
                <div class="card-body">
                  <h4 class="card-title">{{ item.title }}</h4>
                  <p class="card-desc">{{ item.content ? truncate(item.content, 40) : '点击查看详情' }}</p>
                  <div class="card-footer">
                    <span class="card-time">{{ formatTime(item.createdAt) }}</span>
                    <div v-if="isAdmin" class="admin-actions" @click.stop>
                      <template v-if="sortMode">
                        <el-button link type="primary" @click.stop="moveUp(item.id)">⬆️</el-button>
                        <el-button link type="primary" @click.stop="moveDown(item.id)">⬇️</el-button>
                      </template>
                      <template v-else>
                        <el-tooltip :content="item.isPinned ? '取消置顶' : '置顶'" placement="top">
                          <el-button link :type="item.isPinned ? 'warning' : 'info'" @click.stop="handlePin(item)">
                            <span style="font-size: 14px;">📌</span>
                          </el-button>
                        </el-tooltip>
                        <el-button link type="primary" :icon="Edit" @click.stop="openEditDialog(item)" />
                        <el-popconfirm title="确定删除？" @confirm="handleDelete(item.id)">
                          <template #reference>
                            <el-button link type="danger" :icon="Delete" />
                          </template>
                        </el-popconfirm>
                      </template>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 视频区域 -->
        <div v-if="videoResources.length > 0" class="resource-section">
          <h3 class="section-title">🎬 视频</h3>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" v-for="(item, index) in videoResources" :key="item.id">
              <el-card class="resource-card" :class="{ 'is-pinned': item.isPinned, 'sort-mode-card': sortMode }" shadow="hover" @click="sortMode ? null : openDetail(item)">
                <div class="card-cover" :class="getCoverClass(item.type, item.language)">
                  <div class="cover-overlay">
                    <el-tag v-if="item.isPinned" type="warning" size="small" effect="dark">📌 置顶</el-tag>
                    <el-tag type="danger" size="small">视频</el-tag>
                    <el-tag type="info" size="small" style="margin-left: 6px;">{{ getLanguageLabel(item.language) }}</el-tag>
                  </div>
                  <el-icon class="play-icon"><VideoPlay /></el-icon>
                  <div v-if="sortMode" class="sort-number">{{ getResourceIndex(item.id) }}</div>
                </div>
                <div class="card-body">
                  <h4 class="card-title">{{ item.title }}</h4>
                  <p class="card-desc">{{ item.content ? truncate(item.content, 40) : '点击查看详情' }}</p>
                  <div class="card-footer">
                    <span class="card-time">{{ formatTime(item.createdAt) }}</span>
                    <div v-if="isAdmin" class="admin-actions" @click.stop>
                      <template v-if="sortMode">
                        <el-button link type="primary" @click.stop="moveUp(item.id)">⬆️</el-button>
                        <el-button link type="primary" @click.stop="moveDown(item.id)">⬇️</el-button>
                      </template>
                      <template v-else>
                        <el-tooltip :content="item.isPinned ? '取消置顶' : '置顶'" placement="top">
                          <el-button link :type="item.isPinned ? 'warning' : 'info'" @click.stop="handlePin(item)">
                            <span style="font-size: 14px;">📌</span>
                          </el-button>
                        </el-tooltip>
                        <el-button link type="primary" :icon="Edit" @click.stop="openEditDialog(item)" />
                        <el-popconfirm title="确定删除？" @confirm="handleDelete(item.id)">
                          <template #reference>
                            <el-button link type="danger" :icon="Delete" />
                          </template>
                        </el-popconfirm>
                      </template>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </template>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentResource?.title || '资源详情'" width="90%" class="detail-dialog" @close="handleDetailClose">
      <div v-if="currentResource" class="detail-content">
        <!-- 文章内容 -->
        <div v-if="currentResource.type === 'ARTICLE'" class="article-content">
          <p>{{ currentResource.content }}</p>
        </div>
        <!-- 音频播放器 - 并排布局 -->
        <div v-else-if="currentResource.type === 'AUDIO'" class="media-layout">
          <div class="media-player">
            <audio ref="audioRef" controls :src="currentResource.url" style="width: 100%;"></audio>
          </div>
          <div v-if="currentResource.content" class="media-text">
            <h4>文本内容：</h4>
            <p>{{ currentResource.content }}</p>
          </div>
        </div>
        <!-- 视频播放器 - 并排布局 -->
        <div v-else-if="currentResource.type === 'VIDEO'" class="media-layout">
          <div class="media-player">
            <video ref="videoRef" controls :src="currentResource.url" style="width: 100%; max-height: 400px;"></video>
          </div>
          <div v-if="currentResource.content" class="media-text">
            <h4>简介：</h4>
            <p>{{ currentResource.content }}</p>
          </div>
        </div>
        <!-- 文件下载 -->
        <div v-if="currentResource.url" class="file-download">
          <el-button type="success" :icon="Download" @click="downloadFile(currentResource.url)">
            下载资源文件
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="isEditing ? '编辑资源' : '上传资源'" width="90%" class="form-dialog">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%;">
            <el-option label="📄 文章" value="ARTICLE" />
            <el-option label="🎵 音频" value="AUDIO" />
            <el-option label="🎬 视频" value="VIDEO" />
          </el-select>
        </el-form-item>
        <el-form-item label="语言" prop="language">
          <el-select v-model="formData.language" placeholder="请选择语言" style="width: 100%;">
            <el-option label="🇬🇧 英语" value="ENGLISH" />
            <el-option label="🇯🇵 日语" value="JAPANESE" />
            <el-option label="🇷🇺 俄语" value="RUSSIAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="文章内容或简介" maxlength="50000" show-word-limit />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="true"
            :limit="1"
            :accept="getAcceptTypes()"
            :file-list="fileList"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ getUploadTip() }}，大小不超过200MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="formData.url" label="资源URL">
          <el-input v-model="formData.url" placeholder="已上传文件的URL" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ isEditing ? '保存' : '上传' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getLearningResources, addLearningResource, updateLearningResource, deleteLearningResource, getUserInfo, pinLearningResource, updateResourceOrder } from '../api'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, VideoPlay, Headset, Document, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const resources = ref([])
const isAdmin = ref(false)
const filterLanguage = ref('')
const filterType = ref('')
const sortMode = ref(false)
const sortedResourceIds = ref([])

// 按类型过滤的计算属性
const articleResources = computed(() => {
  let list = resources.value.filter(r => r.type === 'ARTICLE')
  if (filterLanguage.value) {
    list = list.filter(r => r.language === filterLanguage.value)
  }
  if (filterType.value && filterType.value !== 'ARTICLE') {
    return []
  }
  return list
})

const audioResources = computed(() => {
  let list = resources.value.filter(r => r.type === 'AUDIO')
  if (filterLanguage.value) {
    list = list.filter(r => r.language === filterLanguage.value)
  }
  if (filterType.value && filterType.value !== 'AUDIO') {
    return []
  }
  return list
})

const videoResources = computed(() => {
  let list = resources.value.filter(r => r.type === 'VIDEO')
  if (filterLanguage.value) {
    list = list.filter(r => r.language === filterLanguage.value)
  }
  if (filterType.value && filterType.value !== 'VIDEO') {
    return []
  }
  return list
})

// 详情弹窗
const detailVisible = ref(false)
const currentResource = ref(null)
const audioRef = ref(null)
const videoRef = ref(null)

// 表单弹窗
const formVisible = ref(false)
const isEditing = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  title: '',
  type: 'ARTICLE',
  language: 'ENGLISH',
  content: '',
  url: ''
})
const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  language: [{ required: true, message: '请选择语言', trigger: 'change' }]
}

// 文件上传相关
const uploadRef = ref(null)
const fileList = ref([])
const uploadUrl = '/api/extension/learning/upload'
const uploadHeaders = {
  get Authorization() {
    const token = localStorage.getItem('token')
    return token ? `Bearer ${token}` : ''
  }
}

// 支持的音频格式
const audioExtensions = ['.mp3', '.wav', '.ogg', '.flac', '.aac', '.m4a', '.wma']
// 支持的视频格式
const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm']
// 支持的文档格式
const docExtensions = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt', '.md', '.rtf']

// 上传前校验 - 限制200MB
const beforeUpload = (file) => {
  const maxSize = 200 * 1024 * 1024 // 200MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过200MB')
    return false
  }
  
  // 获取文件扩展名
  const fileName = file.name.toLowerCase()
  const ext = fileName.substring(fileName.lastIndexOf('.'))
  
  // 校验文件类型
  if (formData.type === 'AUDIO') {
    if (!audioExtensions.includes(ext) && !file.type.startsWith('audio/')) {
      ElMessage.error('请上传音频文件 (MP3/WAV/OGG/FLAC/AAC/M4A/WMA)')
      return false
    }
  }
  if (formData.type === 'VIDEO') {
    if (!videoExtensions.includes(ext) && !file.type.startsWith('video/')) {
      ElMessage.error('请上传视频文件 (MP4/AVI/MOV/WMV/FLV/MKV/WebM)')
      return false
    }
  }
  if (formData.type === 'ARTICLE') {
    if (!docExtensions.includes(ext)) {
      ElMessage.error('请上传文档文件 (DOC/DOCX/XLS/XLSX/PPT/PPTX/PDF/TXT/MD)')
      return false
    }
  }
  
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  console.log('上传响应:', response)
  // 后端返回格式: { code: 200, msg: 'success', data: '/uploads/learning/xxx.mp3' }
  if (response && response.code === 200 && response.data) {
    formData.url = response.data
    ElMessage.success('文件上传成功')
  } else if (response && response.msg) {
    ElMessage.error('上传失败: ' + response.msg)
  } else {
    ElMessage.error('上传失败，请重试')
  }
}

// 上传失败回调
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('文件上传失败，请重试')
}

// 获取上传接受的文件类型
const getAcceptTypes = () => {
  if (formData.type === 'AUDIO') {
    return '.mp3,.wav,.ogg,.flac,.aac,.m4a,.wma'
  } else if (formData.type === 'VIDEO') {
    return '.mp4,.avi,.mov,.wmv,.flv,.mkv,.webm'
  } else {
    return '.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.txt,.md,.rtf'
  }
}

// 获取上传提示文本
const getUploadTip = () => {
  if (formData.type === 'AUDIO') {
    return '支持 MP3/WAV/OGG/FLAC/AAC/M4A/WMA 等音频格式'
  } else if (formData.type === 'VIDEO') {
    return '支持 MP4/AVI/MOV/WMV/FLV/MKV/WebM 等视频格式'
  } else {
    return '支持 DOC/DOCX/XLS/XLSX/PPT/PPTX/PDF/TXT/MD 等文档格式'
  }
}

// 弹窗关闭时暂停播放
const handleDetailClose = () => {
  audioRef.value?.pause()
  videoRef.value?.pause()
}

// 下载文件
const downloadFile = (url) => {
  const link = document.createElement('a')
  link.href = url
  link.download = url.split('/').pop() || 'file'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 初始化用户信息
const initUser = async () => {
  try {
    const res = await getUserInfo()
    const data = res.data || res
    if (data.role === 'ADMIN' || data.username === 'admin') {
      isAdmin.value = true
    }
  } catch (error) {
    console.error('获取用户信息失败')
  }
}

// 加载资源列表
const loadResources = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterLanguage.value) params.language = filterLanguage.value
    if (filterType.value) params.type = filterType.value
    
    const res = await getLearningResources(params)
    resources.value = res.data || res
  } catch (error) {
    ElMessage.error('加载资源失败')
  } finally {
    loading.value = false
  }
}

// 打开详情
const openDetail = (item) => {
  currentResource.value = item
  detailVisible.value = true
}

// 打开新增弹窗
const openAddDialog = () => {
  isEditing.value = false
  resetForm()
  formVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (item) => {
  isEditing.value = true
  Object.assign(formData, {
    id: item.id,
    title: item.title,
    type: item.type,
    language: item.language,
    content: item.content || '',
    url: item.url || ''
  })
  fileList.value = []
  formVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    title: '',
    type: 'ARTICLE',
    language: 'ENGLISH',
    content: '',
    url: ''
  })
  fileList.value = []
}

// 根据类型和语言获取封面CSS类
const getCoverClass = (type, language) => {
  const typeClass = {
    ARTICLE: 'cover-article',
    AUDIO: 'cover-audio',
    VIDEO: 'cover-video'
  }
  const langClass = {
    ENGLISH: 'lang-en',
    JAPANESE: 'lang-jp',
    RUSSIAN: 'lang-ru'
  }
  return `${typeClass[type] || ''} ${langClass[language] || ''}`
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditing.value) {
        await updateLearningResource(formData)
        ElMessage.success('更新成功')
      } else {
        await addLearningResource(formData)
        ElMessage.success('上传成功')
      }
      formVisible.value = false
      loadResources()
    } catch (error) {
      ElMessage.error(isEditing.value ? '更新失败' : '上传失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除资源
const handleDelete = async (id) => {
  try {
    await deleteLearningResource(id)
    ElMessage.success('删除成功')
    loadResources()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 置顶/取消置顶资源
const handlePin = async (item) => {
  try {
    const res = await pinLearningResource(item.id)
    if (res.code === 200) {
      ElMessage.success(res.data || res.msg)
      loadResources()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 排序模式 - 进入
const enterSortMode = () => {
  sortMode.value = true
  sortedResourceIds.value = resources.value.map(r => r.id)
  ElMessage.info('已进入排序模式，可调整资源顺序')
}

// 排序模式 - 退出
const exitSortMode = () => {
  sortMode.value = false
  sortedResourceIds.value = []
}

// 保存排序
const saveSortOrder = async () => {
  if (sortedResourceIds.value.length === 0) {
    exitSortMode()
    return
  }
  loading.value = true
  try {
    const res = await updateResourceOrder(sortedResourceIds.value)
    if (res.code === 200) {
      ElMessage.success('排序保存成功')
      sortMode.value = false
      sortedResourceIds.value = []
      await loadResources()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 上移资源（在resources数组中直接操作）
const moveUp = (id) => {
  const idx = resources.value.findIndex(r => r.id === id)
  if (idx > 0) {
    // 交换位置
    const temp = resources.value[idx - 1]
    resources.value[idx - 1] = resources.value[idx]
    resources.value[idx] = temp
    // 更新ID列表
    sortedResourceIds.value = resources.value.map(r => r.id)
  }
}

// 下移资源
const moveDown = (id) => {
  const idx = resources.value.findIndex(r => r.id === id)
  if (idx < resources.value.length - 1) {
    const temp = resources.value[idx + 1]
    resources.value[idx + 1] = resources.value[idx]
    resources.value[idx] = temp
    sortedResourceIds.value = resources.value.map(r => r.id)
  }
}

// 获取资源在全局排序中的序号
const getResourceIndex = (id) => {
  return resources.value.findIndex(r => r.id === id) + 1
}

// 工具函数
const getTypeLabel = (type) => {
  const map = { ARTICLE: '文章', AUDIO: '音频', VIDEO: '视频' }
  return map[type] || type
}

const getTypeTagColor = (type) => {
  const map = { ARTICLE: 'success', AUDIO: 'warning', VIDEO: 'danger' }
  return map[type] || ''
}

const getLanguageLabel = (lang) => {
  const map = { ENGLISH: '英语', JAPANESE: '日语', RUSSIAN: '俄语' }
  return map[lang] || lang
}

const truncate = (str, len) => {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

onMounted(async () => {
  await initUser()
  await loadResources()
})
</script>

<style scoped>
.extension-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
.header-card { margin-bottom: 20px; border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.sub-text { color: #909399; font-size: 14px; margin-top: 5px; }

.filter-card { margin-bottom: 20px; border-radius: 12px; }
.filter-row { display: flex; flex-wrap: wrap; gap: 20px; }
.filter-group { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-label { font-weight: 500; color: #606266; white-space: nowrap; }

.resource-list { min-height: 300px; }

/* 分组区域 */
.resource-section { margin-bottom: 30px; }
.section-title { 
  font-size: 18px; 
  font-weight: 600; 
  color: #303133; 
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.resource-card { 
  margin-bottom: 20px; 
  border-radius: 15px; 
  overflow: hidden; 
  cursor: pointer; 
  transition: transform 0.2s;
  height: 280px; /* 固定卡片总高度 */
  display: flex;
  flex-direction: column;
}
.resource-card :deep(.el-card__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.resource-card:hover { transform: translateY(-4px); }

/* 置顶卡片样式 */
.resource-card.is-pinned {
  border: 2px solid #e6a23c;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

/* 排序模式样式 */
.sort-mode-hint { margin-bottom: 20px; }
.sort-mode-card {
  border: 2px dashed #409eff !important;
}
.sort-number {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}
.sort-footer {
  flex-direction: column !important;
  align-items: flex-start !important;
  gap: 10px;
}
.sort-buttons {
  display: flex;
  gap: 8px;
  width: 100%;
}
.sort-buttons .el-button {
  flex: 1;
}
.header-actions { display: flex; gap: 10px; align-items: center; }

.card-cover { 
  height: 140px; 
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-overlay { 
  position: absolute; 
  top: 10px; 
  left: 10px; 
}
.play-icon { 
  font-size: 48px; 
  color: rgba(255, 255, 255, 0.9); 
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

/* 封面默认颜色 - 按类型 */
.cover-article { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.cover-audio { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.cover-video { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

/* 封面颜色 - 按语言叠加效果 */
.lang-en.cover-article { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.lang-en.cover-audio { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }
.lang-en.cover-video { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

.lang-jp.cover-article { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }
.lang-jp.cover-audio { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.lang-jp.cover-video { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }

.lang-ru.cover-article { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
.lang-ru.cover-audio { background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%); }
.lang-ru.cover-video { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }

.card-body { padding: 15px; flex: 1; display: flex; flex-direction: column; }
.card-title { 
  margin: 0 0 8px; 
  font-size: 16px; 
  font-weight: 600; 
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
.card-desc { 
  font-size: 13px; 
  color: #909399; 
  margin: 0 0 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}
.card-footer { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-top: auto;
}
.card-time { font-size: 12px; color: #c0c4cc; }
.admin-actions { display: flex; gap: 5px; }

/* 详情弹窗 */
.detail-dialog { max-width: 900px; }
.detail-content { max-height: 70vh; overflow-y: auto; }
.article-content { 
  white-space: pre-wrap; 
  line-height: 1.8; 
  font-size: 15px; 
  color: #3c3f41;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 媒体并排布局 */
.media-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.media-player {
  flex: 0 0 60%;
  max-width: 60%;
}
.media-text {
  flex: 1;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  max-height: 400px;
  overflow-y: auto;
}
.media-text h4 { margin: 0 0 10px; color: #303133; font-size: 14px; }
.media-text p { 
  white-space: pre-wrap; 
  line-height: 1.6; 
  color: #606266;
  margin: 0;
  font-size: 14px;
}

/* 附件下载 */
.attachment-download {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.audio-text, .video-text { margin-top: 20px; }
.audio-text h4, .video-text h4 { margin-bottom: 10px; color: #303133; }
.audio-text p, .video-text p { 
  white-space: pre-wrap; 
  line-height: 1.6; 
  color: #606266;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
}

/* 表单弹窗 */
.form-dialog { max-width: 600px; }

/* 响应式布局 */
@media (max-width: 768px) {
  .extension-container { padding: 12px; max-width: 100%; }
  .header-content { flex-direction: column; align-items: flex-start; gap: 12px; }
  .header-content h2 { font-size: 18px; margin: 0; }
  .header-actions { width: 100%; flex-wrap: wrap; }
  .header-actions .el-button { flex: 1; min-width: 45%; }
  .add-btn { width: 100%; }
  .filter-card { padding: 12px; }
  .filter-row { flex-direction: column; gap: 12px; }
  .filter-group { width: 100%; overflow-x: auto; }
  .filter-label { min-width: 50px; }
  :deep(.el-radio-group) { display: flex; flex-wrap: wrap; gap: 5px; }
  :deep(.el-radio-button) { margin: 0 !important; }
  :deep(.el-radio-button__inner) { padding: 6px 10px; font-size: 12px; }
  .section-title { font-size: 16px; }
  .resource-card { height: auto; min-height: 240px; }
  .card-cover { height: 100px; }
  .card-title { font-size: 14px; }
  .card-desc { font-size: 12px; -webkit-line-clamp: 1; }
  .card-footer { flex-wrap: wrap; gap: 8px; }
  /* 移动端媒体布局垂直堆叠 */
  .media-layout { flex-direction: column; gap: 15px; }
  .media-player { flex: none; max-width: 100%; width: 100%; }
  .media-player audio { 
    width: 100% !important; 
    min-height: 50px;
  }
  .media-player video { 
    width: 100% !important; 
    max-height: 250px;
  }
  .media-text { max-height: 150px; padding: 12px; }
  .media-text h4 { font-size: 13px; }
  .media-text p { font-size: 13px; }
  /* 弹窗优化 */
  .detail-dialog { width: 95% !important; }
  .form-dialog { width: 95% !important; }
  .detail-content { max-height: 60vh; padding: 10px 0; }
  .article-content { font-size: 14px; padding: 12px; }
  /* 文件下载按钮 */
  .file-download { margin-top: 15px; }
  .file-download .el-button { width: 100%; }
}
</style>
