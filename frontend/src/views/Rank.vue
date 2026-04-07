<template>
  <div class="rank-container">
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="left">
          <h2>🏆 单词达人榜</h2>
          <p class="sub-text">记录你的每一步成长，与全站学霸一较高下</p>
        </div>
        <div class="right-decoration hide-on-mobile">
          <el-icon :size="48" color="#FFD700"><Trophy /></el-icon>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="list-card">
      <el-table :data="rankList" style="width: 100%" v-loading="loading" empty-text="暂无排名数据">
        <el-table-column label="排名" width="80" align="center">
          <template #default="scope">
            <div class="rank-index">
              <el-icon v-if="scope.$index === 0" color="#FFD700" :size="28" class="rank-icon"><Trophy /></el-icon>
              <el-icon v-else-if="scope.$index === 1" color="#C0C0C0" :size="26" class="rank-icon"><Trophy /></el-icon>
              <el-icon v-else-if="scope.$index === 2" color="#CD7F32" :size="24" class="rank-icon"><Trophy /></el-icon>
              <span v-else class="rank-number">{{ scope.$index + 1 }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="用户" min-width="180">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="40" :src="scope.row.avatar || defaultAvatar" class="avatar">
                {{ scope.row.username?.charAt(0) }}
              </el-avatar>
              
              <div class="name-wrapper">
                <span class="username">{{ scope.row.username }}</span>
                <el-tag v-if="scope.$index === 0" type="danger" size="small" effect="dark" round class="king-tag hide-on-mobile">
                  👑 榜首
                </el-tag>
                <el-tag v-if="scope.row.username === currentUsername" size="small" type="success" effect="plain" style="margin-left:5px">
                  我
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="已掌握单词" prop="wordCount" align="right" min-width="120">
          <template #default="scope">
            <div class="score-wrapper">
              <span class="score">{{ scope.row.wordCount || 0 }}</span>
              <span class="unit">词</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLeaderboard, getUserInfo } from '../api' 
import { ElMessage } from 'element-plus'
import { Trophy } from '@element-plus/icons-vue'

const loading = ref(false)
const rankList = ref([])
const currentUsername = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const loadRank = async () => {
  loading.value = true
  try {
    try {
        const userRes = await getUserInfo()
        const user = userRes.data || userRes
        currentUsername.value = user.username
    } catch(e) {}

    const res = await getLeaderboard()
    let data = []
    if (res.code === 200 || res.code === 1) {
      data = res.data || []
    } else if (Array.isArray(res)) {
      data = res
    }

    rankList.value = data.filter(u => u.username !== 'admin' && u.username !== 'ADMIN')

  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRank()
})
</script>

<style scoped>
.rank-container { padding: 24px; max-width: 960px; margin: 0 auto; }
.header-card { background: linear-gradient(120deg, #fdfbfb 0%, #ebedee 100%); margin-bottom: 24px; border-radius: 12px; border: none; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-content h2 { font-size: 24px; color: #303133; margin-bottom: 6px; font-weight: 600; }
.sub-text { color: #909399; font-size: 14px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
.rank-index { display: flex; justify-content: center; align-items: center; height: 40px; }
.rank-icon { filter: drop-shadow(0 2px 3px rgba(0,0,0,0.1)); }
.rank-number { font-size: 16px; font-weight: bold; color: #909399; font-family: 'Helvetica Neue', Helvetica, sans-serif; }
.user-info { display: flex; align-items: center; gap: 12px; }
.avatar { border: 2px solid #fff; box-shadow: 0 2px 6px rgba(0,0,0,0.08); flex-shrink: 0; }
.name-wrapper { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.username { font-weight: 600; font-size: 15px; color: #303133; }
.king-tag { font-weight: bold; font-size: 12px; transform: scale(0.9); }
.score-wrapper { display: flex; align-items: baseline; justify-content: flex-end; }
.score { font-size: 22px; font-weight: bold; color: #409EFF; margin-right: 4px; font-family: 'Arial', sans-serif; }
.unit { font-size: 12px; color: #909399; transform: translateY(-2px); }

/* 移动端适配 */
@media (max-width: 768px) {
  .rank-container { padding: 12px; max-width: 100%; }
  .header-content h2 { font-size: 18px; }
  .avatar { width: 36px !important; height: 36px !important; }
  .user-info { gap: 10px; }
  .username { font-size: 14px; }
  .score { font-size: 18px; }
}
</style>