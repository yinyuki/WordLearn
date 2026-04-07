<template>
  <div class="stats-container">
    <el-card shadow="never" class="control-bar">
      <div class="header-flex">
        <div>
          <h2>📊 数据统计</h2>
          <p class="sub-text">当前查看词书：<span style="color: #409EFF; font-weight: bold;">{{ currentBookName }}</span></p>
        </div>
        <div class="right-action">
          <span class="label hide-on-mobile">切换统计词书：</span>
          <el-select 
            v-model="currentBookId" 
            placeholder="请选择词书" 
            class="book-select"
            @focus="savePreviousId"
            @change="handleBookChange"
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

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="data-card">
          <template #header>📚 词书总词汇量</template>
          <div class="num-display">{{ stats.totalWords || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="data-card">
          <template #header>🌱 已学习单词</template>
          <div class="num-display" style="color: #67C23A">{{ stats.learnedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="hover" class="data-card">
          <template #header>🏆 已掌握单词</template>
          <div class="num-display" style="color: #409EFF">{{ stats.masteredCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>
             <span>记忆深度分布 (4阶段)</span>
          </template>
          <div id="barChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover">
          <template #header>学习进度占比</template>
          <div id="pieChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStats, getBooks } from '../api'
import { ElMessageBox, ElMessage } from 'element-plus'

const stats = ref({})
const books = ref([])
const currentBookId = ref(Number(localStorage.getItem('selectedBookId')) || 1)
const previousBookId = ref(currentBookId.value) 

let barChartInstance = null
let pieChartInstance = null

const currentBookName = computed(() => {
  const book = books.value.find(b => b.id === currentBookId.value)
  return book ? book.bookName : '未知词书'
})

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
        previousBookId.value = currentBookId.value
      }
    }
  } catch (err) {
    console.error('加载书单失败', err)
  }
}

const loadStats = async () => {
  if (!currentBookId.value) return
  
  localStorage.setItem('selectedBookId', currentBookId.value)

  try {
    const res = await getStats({ bookId: currentBookId.value })
    const data = res.data || res
    if (data) {
      stats.value = data
      updateCharts()
    }
  } catch (err) {
    console.error('加载统计失败', err)
    ElMessage.error('获取统计数据失败')
  }
}

const savePreviousId = () => {
  previousBookId.value = currentBookId.value
}

const handleBookChange = (newVal) => {
  ElMessageBox.confirm(
    '切换词书将刷新统计数据，确定切换吗？',
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => {
      previousBookId.value = newVal
      loadStats()
      ElMessage.success('已切换统计视图')
    })
    .catch(() => {
      currentBookId.value = previousBookId.value
      ElMessage.info('已取消切换')
    })
}

const updateCharts = () => {
  nextTick(() => {
    initBarChart()
    initPieChart()
  })
}

const initBarChart = () => {
  const dom = document.getElementById('barChart')
  if (!dom) return
  if (barChartInstance) barChartInstance.dispose()
  
  barChartInstance = echarts.init(dom)
  
  const fourStagesData = [0, 0, 0, 0]

  if (stats.value.stageStats) {
    stats.value.stageStats.forEach(item => {
      const stage = item.stage
      const count = item.count
      
      if (stage === 0) {
        fourStagesData[0] += count
      } else if (stage === 1 || stage === 2) {
        fourStagesData[1] += count
      } else if (stage === 3 || stage === 4) {
        fourStagesData[2] += count
      } else if (stage >= 5) {
        fourStagesData[3] += count
      }
    })
  }

  barChartInstance.setOption({
    tooltip: { 
      trigger: 'axis',
      formatter: '{b}: {c} 词',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: { 
      type: 'category', 
      data: ['陌生', '初识', '熟悉', '掌握'],
      axisLabel: { 
        color: '#606266', 
        fontWeight: 'bold', 
        fontSize: 14,
        margin: 15
      },
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#DCDFE6' } }
    },
    yAxis: { 
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#EBEEF5' } }
    },
    series: [{
      name: '单词数量',
      data: fourStagesData,
      type: 'bar',
      barWidth: '45%',
      itemStyle: { 
        borderRadius: [6, 6, 0, 0],
        color: function(params) {
            const colorList = [
                '#909399',
                '#E6A23C',
                '#409EFF',
                '#67C23A'
            ];
            return colorList[params.dataIndex] || '#409EFF';
        }
      },
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 13,
        fontWeight: '500'
      }
    }]
  })
}

const initPieChart = () => {
  const dom = document.getElementById('pieChart')
  if (!dom) return
  if (pieChartInstance) pieChartInstance.dispose()

  pieChartInstance = echarts.init(dom)
  
  const learned = stats.value.learnedCount || 0
  const total = stats.value.totalWords || 0
  const unlearned = total > learned ? total - learned : 0

  pieChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center' },
    series: [{
      name: '学习进度',
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { 
        borderRadius: 10, 
        borderColor: '#fff', 
        borderWidth: 2 
      },
      label: { show: false, position: 'center' },
      emphasis: { 
        label: { 
          show: true, 
          fontSize: 22, 
          fontWeight: 'bold',
          formatter: '{b}\n{d}%',
          color: '#303133'
        } 
      },
      data: [
        { value: learned, name: '已学习', itemStyle: { color: '#67C23A' } },
        { value: unlearned, name: '未学习', itemStyle: { color: '#E6A23C' } }
      ]
    }]
  })
}

onMounted(async () => {
  await loadBooks()
  await loadStats()
  
  window.addEventListener('resize', () => {
    barChartInstance && barChartInstance.resize()
    pieChartInstance && pieChartInstance.resize()
  })
})
</script>

<style scoped>
.stats-container { 
  padding: 20px; 
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.control-bar { margin-bottom: 20px; background: #fff; border-radius: 8px; }
.header-flex { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.sub-text { color: #909399; font-size: 14px; margin-top: 5px; }
.book-select { width: 200px; }
.stats-row { margin-top: 20px; }
.chart-row { margin-top: 20px; }
.data-card { text-align: center; border-radius: 8px; }
.num-display { font-size: 42px; font-weight: bold; margin: 15px 0; color: #303133; font-family: 'DIN Alternate', sans-serif; }
.right-action { display: flex; align-items: center; }
.label { font-size: 14px; color: #606266; margin-right: 10px; }
.chart-container { height: 350px; }

/* 移动端适配 */
@media (max-width: 768px) {
  .stats-container { padding: 12px; min-height: auto; }
  .header-flex { flex-direction: column; align-items: flex-start; }
  .header-flex h2 { font-size: 18px; }
  .right-action { width: 100%; }
  .book-select { width: 100%; }
  .data-card { margin-bottom: 12px; }
  .num-display { font-size: 32px; }
  .chart-container { height: 280px; }
}

/* 平板端适配 */
@media (min-width: 769px) and (max-width: 1024px) {
  .chart-container { height: 300px; }
}
</style>