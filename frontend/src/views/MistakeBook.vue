<template>
  <div class="mistake-container">
    <el-card shadow="never" class="mistake-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="22" color="#F56C6C"><WarningFilled /></el-icon>
            <span class="title">我的错题本</span>
            <el-tag v-if="mistakes.length > 0" type="danger" round effect="dark" style="margin-left: 10px">
              {{ mistakes.length }} 题待攻克
            </el-tag>
          </div>
          <el-button type="primary" plain :icon="Refresh" @click="fetchData" :loading="loading" class="refresh-btn">
            <span class="hide-on-mobile">刷新列表</span>
          </el-button>
        </div>
      </template>

      <div class="table-wrapper">
        <el-table 
          :data="mistakes" 
          v-loading="loading" 
          style="width: 100%" 
          stripe
          empty-text="太棒了！目前没有错题，继续保持！"
        >
          <el-table-column type="index" label="#" width="60" align="center" />
          
          <el-table-column label="单词" min-width="140" prop="originalText" sortable>
            <template #default="scope">
              <span style="font-weight: bold; font-size: 16px; color: #303133">
                {{ scope.row.originalText }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="释义" prop="translation" min-width="180" show-overflow-tooltip />

          <el-table-column label="错误次数" prop="mistakeCount" width="100" align="center" sortable class-name="hide-on-mobile-col">
            <template #default="scope">
               <el-tag :type="scope.row.mistakeCount > 3 ? 'danger' : 'warning'">
                 {{ scope.row.mistakeCount }} 次
               </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="最后出错" prop="lastMistakeTime" width="140" align="center" sortable class-name="hide-on-mobile-col">
            <template #default="scope">
              <span style="font-size: 13px; color: #909399">
                {{ formatTime(scope.row.lastMistakeTime) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-popconfirm 
                title="确定删除该记录？" 
                confirm-button-text="删除"
                confirm-button-type="success"
                cancel-button-text="再等等"
                @confirm="handleDelete(scope.row)"
                width="200"
              >
                <template #reference>
                  <el-button type="success" size="small" :icon="CircleCheck" plain>
                    <span class="hide-on-mobile">删除记录</span>
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMistakes, deleteMistake } from '../api'
import { Refresh, WarningFilled, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const mistakes = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMistakes()
    mistakes.value = res.data || res || []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取错题失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await deleteMistake(row.id)
    ElMessage.success(`成功删除错题记录：${row.originalText}`)
    mistakes.value = mistakes.value.filter(item => item.id !== row.id)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (val) => {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.mistake-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 80px);
}

.mistake-card {
  border-radius: 8px;
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.table-wrapper {
  overflow-x: auto;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .mistake-container { padding: 12px; min-height: auto; }
  .mistake-card { min-height: auto; }
  .title { font-size: 16px; }
  .refresh-btn { padding: 8px; }
  
  /* 隐藏部分列 */
  :deep(.hide-on-mobile-col) {
    display: none;
  }
}
</style>