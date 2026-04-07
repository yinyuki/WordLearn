<template>
  <div class="community-container">
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2>🌍 学习社区</h2>
          <p class="sub-text">分享你的学习心得，与伙伴们一起进步</p>
        </div>
        <el-button type="primary" size="large" @click="dialogVisible = true" class="post-btn">
          <el-icon class="el-icon--left"><EditPen /></el-icon> 发布动态
        </el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="post-list">
      <el-empty v-if="posts.length === 0" description="暂无动态，快来抢沙发吧！" />
      
      <el-card v-else v-for="post in posts" :key="post.id" class="post-card" shadow="hover">
        <div class="post-header">
          <div class="header-left">
            <el-avatar :size="50" :src="post.avatar || defaultAvatar" class="post-avatar" />
            <div class="user-info">
              <span class="username">
                {{ post.username }}
                <el-tag v-if="post.username === 'admin'" size="small" type="danger" effect="plain" style="margin-left:5px; height: 18px; line-height: 16px;">ADMIN</el-tag>
              </span>
              <span class="time">{{ formatTime(post.createdAt) }}</span>
            </div>
          </div>
          <div v-if="post.userId === currentUserId || isAdmin" class="header-right">
            <el-popconfirm title="确定删除这条动态吗？" @confirm="handleDeletePost(post)">
              <template #reference>
                <el-button link type="danger" :icon="Delete" circle />
              </template>
            </el-popconfirm>
          </div>
        </div>
        
        <div class="post-content">
          {{ post.content }}
        </div>

        <el-divider />

        <div class="post-actions">
          <div class="action-item" @click="handleLike(post)" :class="{ liked: post.isLiked }">
            <el-icon v-if="post.isLiked"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            <span>{{ post.likes || 0 }} 点赞</span>
          </div>
          
          <div class="action-item" @click="toggleComments(post)">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ post.commentCount || 0 }} 评论</span>
          </div>
        </div>

        <div v-if="post.showComments" class="comment-section">
          <div class="comment-input">
            <el-input 
              v-model="post.newComment" 
              placeholder="写下你的评论..." 
              @keyup.enter="submitComment(post)"
            >
              <template #append>
                <el-button @click="submitComment(post)">发送</el-button>
              </template>
            </el-input>
          </div>

          <div v-loading="post.loadingComments" class="comment-list">
            <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
              <el-avatar :size="30" :src="comment.avatar || defaultAvatar" class="comment-avatar"/>
              <div class="comment-body">
                <div class="comment-top">
                  <span class="comment-user">{{ comment.username }}</span>
                  <div v-if="comment.userId === currentUserId || post.userId === currentUserId || isAdmin">
                    <el-popconfirm title="确定删除这条评论？" @confirm="handleDeleteComment(post, comment)">
                      <template #reference>
                        <el-icon class="delete-icon" title="删除评论"><Delete /></el-icon>
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
              </div>
            </div>
            <div v-if="post.comments.length === 0 && !post.loadingComments" class="no-comment">
              暂无评论，快来评论吧~
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="发布新动态" class="responsive-dialog">
      <el-input
        v-model="newPostContent"
        type="textarea"
        :rows="5"
        placeholder="分享你的内容吧..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  getPosts, createPost, deletePost, 
  likePost, unlikePost, 
  getPostComments, addPostComment, deletePostComment,
  getUserInfo 
} from '../api'
import { ElMessage } from 'element-plus'
import { EditPen, Star, StarFilled, ChatDotRound, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const posts = ref([])
const newPostContent = ref('')
const currentUserId = ref(null)
const isAdmin = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const initUser = async () => {
  try {
    const res = await getUserInfo()
    const data = res.data || res
    currentUserId.value = data.id
    if (data.role === 'ADMIN' || data.username === 'admin') {
      isAdmin.value = true
    }
  } catch (error) {
    console.error('获取用户信息失败')
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPosts()
    const data = res.data || res
    posts.value = data.map(p => ({
      ...p,
      showComments: false,
      loadingComments: false,
      comments: [],
      newComment: '',
      isLiked: false 
    }))
  } catch (error) {
    ElMessage.error('加载动态失败')
  } finally {
    loading.value = false
  }
}

const submitPost = async () => {
  if (!newPostContent.value.trim()) return ElMessage.warning('内容不能为空')
  submitting.value = true
  try {
    await createPost({ content: newPostContent.value })
    ElMessage.success('发布成功')
    dialogVisible.value = false
    newPostContent.value = ''
    loadPosts()
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

const handleDeletePost = async (post) => {
  try {
    await deletePost(post.id)
    ElMessage.success('动态已删除')
    posts.value = posts.value.filter(p => p.id !== post.id)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleLike = async (post) => {
  try {
    if (post.isLiked) {
      await unlikePost(post.id)
      post.likes = Math.max(0, post.likes - 1)
      post.isLiked = false
    } else {
      await likePost(post.id)
      post.likes++
      post.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error(error)
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && post.comments.length === 0) {
    loadComments(post)
  }
}

const loadComments = async (post) => {
  post.loadingComments = true
  try {
    const res = await getPostComments(post.id)
    post.comments = res.data || res
  } finally {
    post.loadingComments = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) return
  try {
    await addPostComment(post.id, { content: post.newComment })
    post.newComment = ''
    post.commentCount++
    loadComments(post)
    ElMessage.success('评论成功')
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const handleDeleteComment = async (post, comment) => {
  try {
    await deletePostComment(comment.id)
    ElMessage.success('评论已删除')
    post.comments = post.comments.filter(c => c.id !== comment.id)
    post.commentCount = Math.max(0, post.commentCount - 1)
  } catch (error) {
    ElMessage.error('删除评论失败')
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(async () => {
  await initUser()
  await loadPosts()
})
</script>

<style scoped>
.community-container { padding: 20px; max-width: 850px; margin: 0 auto; }
.header-card { margin-bottom: 20px; border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.sub-text { color: #909399; font-size: 14px; margin-top: 5px; }

.post-card { margin-bottom: 20px; border-radius: 15px; border: 1px solid #ebeef5; }
.post-header { display: flex; justify-content: space-between; align-items: flex-start; }
.header-left { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; flex-direction: column; }
.username { font-weight: 600; color: #303133; }
.time { font-size: 12px; color: #909399; }

.post-content { padding: 10px 0; font-size: 15px; line-height: 1.6; color: #3c3f41; white-space: pre-wrap; }

.post-actions { display: flex; gap: 30px; padding: 10px 0 0; }
.action-item { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #606266; font-size: 14px; }
.action-item:hover { color: #409EFF; }
.action-item.liked { color: #e6a23c; }

.comment-section { background: #f8f9fa; margin-top: 15px; padding: 15px; border-radius: 10px; }
.comment-list { margin-top: 15px; }
.comment-item { display: flex; gap: 12px; margin-bottom: 12px; }
.comment-avatar { flex-shrink: 0; }
.comment-body { flex: 1; display: flex; flex-direction: column; background: #fff; padding: 8px 12px; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }

.comment-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.comment-user { font-weight: 600; color: #409EFF; font-size: 13px; }
.comment-text { color: #333; font-size: 14px; line-height: 1.4; word-break: break-all; }

.delete-icon { 
  font-size: 14px; 
  color: #909399; 
  cursor: pointer; 
  transition: color 0.2s;
}
.delete-icon:hover { color: #F56C6C; }

.no-comment { text-align: center; color: #999; font-size: 13px; padding: 10px; }

/* 响应式对话框 */
.responsive-dialog {
  width: 90%;
  max-width: 500px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .community-container { padding: 12px; max-width: 100%; }
  .header-content { flex-direction: column; align-items: flex-start; }
  .header-content h2 { font-size: 18px; }
  .post-btn { width: 100%; }
  .post-avatar { width: 40px !important; height: 40px !important; }
  .post-actions { gap: 20px; }
  .action-item { font-size: 13px; }
}
</style>