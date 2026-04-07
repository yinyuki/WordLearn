<template>
  <div class="book-manager-container">
    <el-row :gutter="20" class="full-height-row">
      
      <el-col :xs="24" :sm="24" :md="8" class="full-height-col book-list-col">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Collection /></el-icon> 
                <span>我的词书</span>
              </div>
              <el-button type="primary" size="small" :icon="Plus" @click="openCreateDialog">新建</el-button>
            </div>
          </template>
          
          <el-table 
            :data="books" 
            v-loading="loadingBooks" 
            highlight-current-row 
            @current-change="handleBookSelect"
            style="cursor: pointer;"
            height="100%"
          >
            <el-table-column prop="bookName" label="名称" show-overflow-tooltip>
              <template #default="scope">
                <span :class="{ 'system-book-name': scope.row.userId === 0 }">
                  {{ scope.row.bookName }}
                </span>
                <el-tag v-if="scope.row.userId === 0" size="small" type="success" effect="plain" style="margin-left:5px; zoom: 0.8;">官方</el-tag>
                <el-tag v-else-if="scope.row.userId === currentUserId" size="small" type="primary" effect="plain" style="margin-left:5px; zoom: 0.8;">我的</el-tag>
              </template>
            </el-table-column>
            
            <el-table-column width="90" align="center">
              <template #default="scope">
                <div v-if="isAdmin || scope.row.userId === currentUserId">
                  <el-button 
                    type="primary" :icon="Edit" link 
                    @click.stop="openEditDialog(scope.row)" 
                  />
                  <el-button 
                    type="danger" :icon="Delete" link 
                    @click.stop="handleDeleteBook(scope.row)" 
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="16" class="full-height-col word-list-col">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <span v-if="selectedBook">📖 {{ selectedBook.bookName }}</span>
                <span v-else class="placeholder-text">请在上方选择一本词书</span>
                
                <el-tag v-if="selectedBook" type="info" round style="margin-left: 10px">
                  共 {{ wordTotal }} 词
                </el-tag>
              </div>
              <el-button 
                v-if="selectedBook" 
                type="primary" 
                size="small" 
                :icon="Plus" 
                @click="openAddWordDialog"
              >添加单词</el-button>
            </div>
          </template>

          <el-table 
            :data="words" 
            v-loading="loadingWords" 
            height="calc(100% - 50px)" 
            empty-text="暂无单词或未选择词书"
            stripe
            class="word-table"
          >
            <el-table-column type="index" label="#" width="50" />
            
            <el-table-column prop="originalText" label="单词" min-width="120" sortable>
              <template #default="scope">
                <span style="font-weight: 500">{{ scope.row.originalText }}</span>
                <el-tag v-if="scope.row.languageType === 'JAPANESE'" size="small" type="warning" style="margin-left: 5px; zoom: 0.8">日</el-tag>
                <el-tag v-else-if="scope.row.languageType === 'RUSSIAN'" size="small" type="danger" style="margin-left: 5px; zoom: 0.8">俄</el-tag>
                <el-tag v-else-if="scope.row.languageType === 'ENGLISH'" size="small" type="primary" style="margin-left: 5px; zoom: 0.8">英</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="translation" label="释义" min-width="150" />
            
            <el-table-column prop="note" label="备注" show-overflow-tooltip min-width="120" class-name="hide-on-mobile-col">
              <template #default="scope">
                <span v-if="scope.row.note" style="color: #909399; font-size: 13px;">{{ scope.row.note }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <div v-if="canEditWord(scope.row)">
                  <el-button 
                    type="primary" :icon="Edit" link 
                    @click="openEditWordDialog(scope.row)" 
                  />
                  <el-button 
                    type="danger" :icon="Delete" link 
                    @click="handleDeleteWord(scope.row)" 
                  />
                </div>
                <el-icon v-else color="#C0C4CC"><Lock /></el-icon>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar" v-if="selectedBook && wordTotal > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="wordTotal"
              :page-size="wordPageSize"
              v-model:current-page="wordPage"
              @current-change="handlePageChange"
              size="small"
              :pager-count="5"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="bookDialogVisible" :title="isEditMode ? '编辑词书' : '新建词书'" class="responsive-dialog">
      <el-form label-position="top">
        <el-form-item label="词书名称">
          <el-input 
            v-model="newBookName" 
            placeholder="例如：考研核心词汇" 
            maxlength="30" 
            show-word-limit 
            @keyup.enter="submitBook"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBook">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="wordDialogVisible" :title="isEditWordMode ? '编辑单词' : '添加新单词'" class="responsive-dialog">
      <el-form label-width="80px" class="word-form">
        <el-form-item label="语言类型">
          <el-radio-group v-model="wordForm.languageType">
            <el-radio label="ENGLISH">英语</el-radio>
            <el-radio label="JAPANESE">日语</el-radio>
            <el-radio label="RUSSIAN">俄语</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单词/原文">
          <el-input v-model="wordForm.originalText" placeholder="例如：Apple / りんご" />
        </el-form-item>
        <el-form-item label="中文释义">
          <el-input v-model="wordForm.translation" placeholder="例如：苹果" />
        </el-form-item>
        <el-form-item label="备注/例句">
          <el-input v-model="wordForm.note" type="textarea" :rows="2" placeholder="例句或助记提示（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="wordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWord">{{ isEditWordMode ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getBooks, addBook, deleteBook, updateBook, getWordList, addWord, updateWord, deleteWord, getUserInfo } from '../api'
import { Plus, Delete, Collection, Lock, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const currentUserId = ref(null)
const isAdmin = ref(false)

const loadingBooks = ref(false)
const books = ref([])
const selectedBook = ref(null)

const bookDialogVisible = ref(false)
const isEditMode = ref(false)
const editingBookId = ref(null)
const newBookName = ref('')

const loadingWords = ref(false)
const words = ref([])
const wordTotal = ref(0)
const wordPage = ref(1)
const wordPageSize = ref(20)
const wordDialogVisible = ref(false)
const isEditWordMode = ref(false)
const editingWordId = ref(null)
const wordForm = reactive({ originalText: '', translation: '', note: '', languageType: 'ENGLISH' })

const init = async () => {
  try {
    const userRes = await getUserInfo()
    const user = userRes.data || userRes
    currentUserId.value = user.id
    isAdmin.value = user.role === 'ADMIN' || user.username === 'admin'
    await loadBooks()
  } catch (e) { console.error(e) }
}

const loadBooks = async () => {
  loadingBooks.value = true
  try {
    const res = await getBooks()
    books.value = res.data || res
    
    if (selectedBook.value) {
      const updatedBook = books.value.find(b => b.id === selectedBook.value.id)
      if (updatedBook) {
        selectedBook.value = updatedBook
      } else {
        selectedBook.value = null
      }
    }
  } finally {
    loadingBooks.value = false
  }
}

const openCreateDialog = () => {
  isEditMode.value = false
  editingBookId.value = null
  newBookName.value = ''
  bookDialogVisible.value = true
}

const openEditDialog = (book) => {
  isEditMode.value = true
  editingBookId.value = book.id
  newBookName.value = book.bookName
  bookDialogVisible.value = true
}

const submitBook = async () => {
  if (!newBookName.value.trim()) return ElMessage.warning('请输入名称')
  
  try {
    if (isEditMode.value) {
      await updateBook({ id: editingBookId.value, bookName: newBookName.value })
      ElMessage.success('修改成功')
    } else {
      await addBook({ bookName: newBookName.value })
      ElMessage.success('创建成功')
    }
    
    bookDialogVisible.value = false
    loadBooks()
  } catch (e) {
    if (!e.msg) { 
        ElMessage.error('操作失败')
    } else {
        console.error(e.msg)
    }
  }
}

const handleDeleteBook = (book) => {
  ElMessageBox.confirm(`确定删除《${book.bookName}》及其所有单词吗？`, '高风险操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBook(book.id)
      ElMessage.success('已删除')
      loadBooks()
    } catch (e) { 
        if (!e.msg) ElMessage.error('删除失败')
    }
  })
}

const handleBookSelect = (row) => {
  if (!row) return
  selectedBook.value = row
  wordPage.value = 1
  loadWords()
}

const loadWords = async () => {
  if (!selectedBook.value) return
  loadingWords.value = true
  try {
    const params = { bookId: selectedBook.value.id, page: wordPage.value, size: wordPageSize.value }
    const res = await getWordList(params)
    const data = res.data || res
    
    if (data.records) {
      words.value = data.records
      wordTotal.value = data.total
    } else if (Array.isArray(data)) {
      words.value = data
      wordTotal.value = data.length
    } else {
      words.value = []
      wordTotal.value = 0
    }
  } catch (e) { words.value = [] } 
  finally { loadingWords.value = false }
}

const handlePageChange = (val) => {
  wordPage.value = val
  loadWords()
}

const canEditWord = (word) => isAdmin.value || word.userId === currentUserId.value
const canDeleteWord = (word) => isAdmin.value || word.userId === currentUserId.value

const openAddWordDialog = () => {
  isEditWordMode.value = false
  editingWordId.value = null
  wordForm.originalText = ''
  wordForm.translation = ''
  wordForm.note = ''
  wordForm.languageType = 'ENGLISH'
  wordDialogVisible.value = true
}

const openEditWordDialog = (word) => {
  isEditWordMode.value = true
  editingWordId.value = word.id
  wordForm.originalText = word.originalText
  wordForm.translation = word.translation
  wordForm.note = word.note || ''
  wordForm.languageType = word.languageType || 'ENGLISH'
  wordDialogVisible.value = true
}

const submitWord = async () => {
  if (!wordForm.originalText || !wordForm.translation) return ElMessage.warning('单词和释义必填')
  
  try {
    if (isEditWordMode.value) {
      await updateWord({
        id: editingWordId.value,
        originalText: wordForm.originalText,
        translation: wordForm.translation,
        note: wordForm.note,
        languageType: wordForm.languageType
      })
      ElMessage.success('修改成功')
    } else {
      await addWord({
        bookId: selectedBook.value.id,
        originalText: wordForm.originalText,
        translation: wordForm.translation,
        note: wordForm.note,
        languageType: wordForm.languageType
      })
      ElMessage.success('添加成功')
    }
    wordDialogVisible.value = false
    loadWords()
  } catch (e) { 
    console.error('操作单词异常:', e)
  }
}

const handleDeleteWord = (word) => {
  ElMessageBox.confirm(`确定删除单词 "${word.originalText}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteWord(word.id)
        ElMessage.success('删除成功')
        loadWords()
      } catch (e) { 
        if (!e.msg) ElMessage.error('无法删除')
      }
    })
}

onMounted(() => { init() })
</script>

<style scoped>
/* 桌面端样式 - 保持原有布局 */
.book-manager-container { height: calc(100vh - 40px); padding: 20px; background-color: #f0f2f5; box-sizing: border-box; }
.full-height-row, .full-height-col, .panel-card { height: 100%; }
.panel-card { display: flex; flex-direction: column; border: none; }
:deep(.el-card__body) { flex: 1; padding: 10px; overflow: hidden; display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 16px; color: #303133; }
.placeholder-text { color: #909399; font-size: 14px; }
.system-book-name { color: #67C23A; font-weight: 600; }
.pagination-bar { margin-top: 10px; display: flex; justify-content: flex-end; padding-right: 10px; }

/* 响应式对话框 */
.responsive-dialog {
  width: 90%;
  max-width: 450px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .book-manager-container { 
    height: auto; 
    min-height: calc(100vh - 56px);
    padding: 12px; 
  }
  
  .full-height-row { 
    height: auto; 
    flex-direction: column;
  }
  
  .book-list-col {
    height: 280px;
    margin-bottom: 12px;
  }
  
  .word-list-col {
    height: 400px;
  }
  
  .full-height-col, .panel-card { 
    height: 100%; 
  }
  
  .header-title { font-size: 14px; }
  
  .word-form :deep(.el-form-item__label) {
    width: 70px !important;
  }
  
  /* 隐藏备注列 */
  :deep(.hide-on-mobile-col) {
    display: none;
  }
}

/* 平板端适配 */
@media (min-width: 769px) and (max-width: 1024px) {
  .book-manager-container { padding: 15px; }
}
</style>