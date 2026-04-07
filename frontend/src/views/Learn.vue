<template>
  <div class="learn-container">
    <div class="mode-header">
      <el-tag :type="isPractice ? 'info' : 'success'" effect="dark">
        {{ isPractice ? '🏹 巩固模式：不计入进度' : '📚 记忆模式：同步记忆曲线' }}
      </el-tag>
      <div class="book-info hide-on-mobile" v-if="currentWord.originalText">
        <el-tag size="small" type="warning" plain>书本 ID: {{ currentBookId }}</el-tag>
      </div>
    </div>

    <el-progress 
      :percentage="progress" 
      :format="() => `${currentIndex + 1} / ${wordList.length}`" 
      class="progress-bar"
    />
    
    <div v-if="!isFinished" class="card-area">
      <el-card class="word-card" shadow="always">
        <template #header>
          <div class="card-header">
            <span class="word-title">{{ currentWord.originalText }}</span>
          </div>
        </template>

        <div class="word-body">
          <div v-if="!showAnswer" class="mask" @click="showAnswer = true">
            <el-icon :size="40"><View /></el-icon>
            <p>点击卡片显示释义</p>
          </div>

          <div v-else class="answer-content">
            <div class="trans-item">
              <span class="label">释义：</span>
              <span class="text">{{ currentWord.translation }}</span>
            </div>
            <el-divider border-style="dashed" />
            <div class="note-item" v-if="currentWord.note">
              <span class="label">笔记：</span>
              <p class="text">{{ currentWord.note }}</p>
            </div>
          </div>
        </div>

        <template #footer>
          <div v-if="!showAnswer">
            <el-button type="primary" size="large" class="full-btn" @click="showAnswer = true">
              想起来了
            </el-button>
          </div>
          <div v-else class="action-btns">
            <el-button type="danger" size="large" plain @click="next(false)">
              <el-icon><Close /></el-icon> 不认识
            </el-button>
            <el-button type="success" size="large" @click="next(true)">
              <el-icon><Check /></el-icon> 认识
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <div v-else class="finish-area">
      <el-result icon="success" title="任务已完成！" sub-title="今天的努力又让你离目标近了一步">
        <template #extra>
          <el-button type="primary" size="large" @click="$router.push('/dashboard')">
            返回看板
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitResult } from '../api'
import { View, Check, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const wordList = ref([])
const currentIndex = ref(0)
const showAnswer = ref(false)
const isFinished = ref(false)

const isPractice = computed(() => route.query.mode === 'practice')
const currentBookId = ref(Number(localStorage.getItem('selectedBookId')) || 1)

const currentWord = computed(() => wordList.value[currentIndex.value] || {})
const progress = computed(() => {
  if (wordList.value.length === 0) return 0
  return Math.round((currentIndex.value / wordList.value.length) * 100)
})

const next = async (remembered) => {
  if (!isPractice.value) {
    try {
      await submitResult({ 
        wordId: currentWord.value.id, 
        remembered: remembered,
        bookId: currentBookId.value 
      })
    } catch (error) {
      console.error("提交进度失败", error)
      ElMessage.error('网络异常，进度保存失败')
      return
    }
  }
  
  if (currentIndex.value < wordList.value.length - 1) {
    currentIndex.value++
    showAnswer.value = false
  } else {
    isFinished.value = true
  }
}

onMounted(() => {
  const data = sessionStorage.getItem('learningList')
  if (data) {
    wordList.value = JSON.parse(data)
  } else {
    ElMessage.warning('暂无学习任务')
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.learn-container {
  max-width: 500px;
  margin: 40px auto;
  padding: 0 20px;
}

.mode-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.progress-bar {
  margin-bottom: 30px;
}

.word-card {
  min-height: 420px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.card-header {
  text-align: center;
}

.word-title {
  font-size: 36px;
  font-weight: bold;
  color: #2c3e50;
  letter-spacing: 1px;
}

.word-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px 0;
}

.mask {
  height: 200px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.mask:hover {
  background: #f0f2f5;
}

.answer-content {
  padding: 10px;
}

.label {
  font-weight: bold;
  color: #909399;
  margin-right: 8px;
}

.text {
  font-size: 18px;
  color: #303133;
  line-height: 1.6;
}

.action-btns {
  display: flex;
  gap: 20px;
}

.action-btns .el-button {
  flex: 1;
}

.full-btn {
  width: 100%;
}

.finish-area {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .learn-container {
    max-width: 100%;
    margin: 20px auto;
    padding: 0 12px;
  }
  
  .word-title { font-size: 28px; }
  .word-card { min-height: 380px; }
  .mask { height: 160px; }
  .text { font-size: 16px; }
  .action-btns { gap: 12px; }
  .finish-area { padding: 24px; }
}
</style>