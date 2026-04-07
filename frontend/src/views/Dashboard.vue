<template>
  <div class="dashboard-container">
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2>👋 欢迎回来!</h2>
          <p class="sub-text">当前学习进度相互独立，切换词书可开启新任务</p>
        </div>
        <div class="book-selector">
          <span class="label hide-on-mobile">当前词书：</span>
          <el-select 
            v-model="currentBookId" 
            @change="handleBookChange" 
            placeholder="请选择词书" 
            class="book-select"
          >
            <el-option 
              v-for="b in books" 
              :key="b.id" 
              :label="b.bookName" 
              :value="b.id" 
            />
          </el-select>
        </div>
      </div>
    </el-card>
    
    <el-row :gutter="20" class="card-row">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="box-card">
          <template #header><span class="bold">📚 曲线复习</span></template>
          <div class="text-item">
            <h1 class="val-warn">{{ taskInfo.reviews ? taskInfo.reviews.length : 0 }}</h1>
            <p class="sub-label">个单词需巩固</p>
            <el-button 
              type="warning" 
              @click="startLearn('reviews')" 
              :disabled="!taskInfo.reviews || taskInfo.reviews.length === 0"
              class="action-btn"
            >
              开始复习
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="box-card">
          <template #header><span class="bold">🌱 学习新词</span></template>
          <div class="text-item">
            <h1 class="val-success">{{ taskInfo.newWords ? taskInfo.newWords.length : 0 }}</h1>
            <p class="sub-label">个单词待探索</p>
            <el-button 
              type="success" 
              @click="startLearn('newWords')" 
              :disabled="!taskInfo.newWords || taskInfo.newWords.length === 0"
              class="action-btn"
            >
              开始学习
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="hover" class="box-card">
          <template #header><span class="bold">🏹 今日重温</span></template>
          <div class="text-item">
            <h1 class="val-primary">{{ taskInfo.todayWords ? taskInfo.todayWords.length : 0 }}</h1>
            <p class="sub-label">个单词已打卡</p>
            <el-button 
              type="primary" 
              plain 
              @click="startLearn('todayWords', true)" 
              :disabled="!taskInfo.todayWords || taskInfo.todayWords.length === 0"
              class="action-btn"
            >
              无限巩固
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDailyTask, getBooks } from '../api' 

const router = useRouter()
const taskInfo = ref({ reviews: [], newWords: [], todayWords: [] })
const books = ref([])
const currentBookId = ref(Number(localStorage.getItem('selectedBookId')) || 1)

const loadBooks = async () => {
  try {
    const res = await getBooks()
    const data = res.data || res
    if (Array.isArray(data)) {
      books.value = data
      if (books.value.length > 0) {
        const exists = books.value.find(b => b.id === currentBookId.value)
        if (!exists) {
          currentBookId.value = books.value[0].id
        }
      }
    }
  } catch (err) {
    console.error('加载书单失败', err)
  }
}

const loadData = async () => {
  if (!currentBookId.value) return
  
  localStorage.setItem('selectedBookId', currentBookId.value)
  
  try {
    const res = await getDailyTask({ 
      limit: 20, 
      bookId: currentBookId.value 
    })
    
    if (res && res.data) {
      taskInfo.value = res.data
    } else {
      taskInfo.value = res || { reviews: [], newWords: [], todayWords: [] }
    }
  } catch (error) {
    console.error('加载任务失败', error)
  }
}

const handleBookChange = () => {
  loadData()
}

const startLearn = (type, isPractice = false) => {
  const list = taskInfo.value[type]
  if (list && list.length > 0) {
    sessionStorage.setItem('learningList', JSON.stringify(list))
    router.push({ 
      path: '/learn', 
      query: { mode: isPractice ? 'practice' : 'normal' } 
    })
  }
}

onMounted(async () => {
  await loadBooks()
  await loadData()
})
</script>

<style scoped>
.dashboard-container { padding: 20px; }
.header-card { background: transparent; border: none; }
.header-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.book-selector { background: #fff; padding: 10px 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.book-select { width: 200px; }
.card-row { margin-top: 20px; }
.text-item { text-align: center; padding: 10px 0; }
.bold { font-weight: bold; }
.sub-label { color: #909399; font-size: 13px; margin-bottom: 15px; }
.val-warn { color: #E6A23C; font-size: 36px; margin: 5px 0; }
.val-success { color: #67C23A; font-size: 36px; margin: 5px 0; }
.val-primary { color: #409EFF; font-size: 36px; margin: 5px 0; }
.box-card { border-radius: 12px; transition: transform 0.3s; }
.box-card:hover { transform: translateY(-5px); }
.action-btn { min-width: 100px; }

/* 移动端适配 */
@media (max-width: 768px) {
  .dashboard-container { padding: 12px; }
  .header-content { flex-direction: column; align-items: flex-start; }
  .header-content h2 { font-size: 20px; margin-bottom: 5px; }
  .book-selector { width: 100%; padding: 10px 15px; }
  .book-select { width: 100%; }
  .card-row { margin-top: 15px; }
  .box-card { margin-bottom: 12px; }
  .box-card:hover { transform: none; }
  .val-warn, .val-success, .val-primary { font-size: 30px; }
  .action-btn { width: 100%; }
}
</style>