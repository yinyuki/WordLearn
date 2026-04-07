<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :xs="24" :sm="8" :md="6">
        <el-card class="user-info-card" shadow="never">
          <div class="user-identity">
            <div class="avatar-wrapper">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="customUpload"
              >
                <el-avatar v-if="avatarUrl" :size="110" :src="avatarUrl" class="profile-avatar" />
                <el-avatar v-else :size="110" :src="defaultAvatar" class="profile-avatar" />
                
                <div class="avatar-edit-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更新头像</span>
                </div>
              </el-upload>
              <el-tooltip content="重置头像" placement="right">
                <div class="reset-avatar-icon" @click="handleResetAvatar">
                  <el-icon><RefreshLeft /></el-icon>
                </div>
              </el-tooltip>
            </div>
            <h2 class="user-name">{{ username }}</h2>
          </div>
          
          <div class="logout-section">
            <el-button type="danger" plain class="w-full logout-btn" @click="handleLogout">
              退出当前账号
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="16" :md="18">
        <div class="settings-wrapper">
          <el-card class="settings-card" shadow="hover">
            <template #header>
              <div class="card-title-box">
                <el-icon><Calendar /></el-icon> 学习计划管理
              </div>
            </template>
            <div class="plan-setting">
              <span class="setting-label">每日新词上限</span>
              <div class="setting-action">
                <el-input-number v-model="dailyLimit" :min="1" :max="500" @change="saveDailyLimit" />
                <span class="setting-hint hide-on-mobile">数值修改后首页将实时更新任务量</span>
              </div>
            </div>
          </el-card>

          <el-card class="settings-card security-card" shadow="hover">
            <template #header>
              <div class="card-title-box">
                <el-icon><Lock /></el-icon> 账号安全中心
              </div>
            </template>
            
            <div class="config-section">
              <h4 class="config-title">变更账号名称</h4>
              <el-form :model="usernameForm" label-position="top">
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="新用户名">
                      <el-input v-model="usernameForm.newUsername" placeholder="请输入新的登录账号" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="身份验证 (当前密码)">
                      <el-input v-model="usernameForm.password" type="password" show-password placeholder="请输入密码以核实身份" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-button type="primary" class="save-btn" @click="handleUpdateUsername">保存账号变更</el-button>
              </el-form>
            </div>

            <el-divider />

            <div class="config-section">
              <h4 class="config-title">重置登录密码</h4>
              <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top">
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="8">
                    <el-form-item label="原密码" prop="oldPassword">
                      <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="8">
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input v-model="pwdForm.newPassword" type="password" show-password />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="8">
                    <el-form-item label="确认新密码" prop="confirmPassword">
                      <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-button type="warning" class="save-btn" @click="submitPwdChange">确认修改密码</el-button>
              </el-form>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Calendar, Lock, RefreshLeft } from '@element-plus/icons-vue'
import { updatePassword, updateUsername, updateAvatar, resetAvatar } from '../api'

const router = useRouter()
const username = ref(localStorage.getItem('username') || 'User')
const avatarUrl = ref(localStorage.getItem('avatar') || '')
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

/* 头像逻辑 */
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('仅支持图片格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('图片须小于 10MB')
    return false
  }
  return true
}

const customUpload = (options) => {
  const reader = new FileReader()
  reader.readAsDataURL(options.file)
  reader.onload = async () => {
    const base64Str = reader.result
    try {
      const res = await updateAvatar({ avatar: base64Str })
      if (res.code === 200) {
        avatarUrl.value = base64Str
        localStorage.setItem('avatar', base64Str)
        ElMessage.success('头像已同步至服务器')
      }
    } catch (e) { ElMessage.error('网络请求失败') }
  }
}

const handleResetAvatar = async () => {
  try {
    await ElMessageBox.confirm('确定要重置头像吗？头像将恢复为默认样式。', '重置头像', { type: 'warning' })
    const res = await resetAvatar()
    if (res.code === 200) {
      avatarUrl.value = ''
      localStorage.removeItem('avatar')
      ElMessage.success('头像已重置')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('重置失败')
  }
}

/* 学习偏好 */
const dailyLimit = ref(Number(localStorage.getItem('dailyLimit')) || 20)
const saveDailyLimit = (val) => {
  localStorage.setItem('dailyLimit', val)
  ElMessage.success('学习计划已保存')
}

/* 账号管理 */
const usernameForm = reactive({ newUsername: '', password: '' })
const handleUpdateUsername = () => {
  if (!usernameForm.newUsername.trim() || !usernameForm.password) return ElMessage.warning('请完整填写信息')
  ElMessageBox.confirm(`确定要变更账号名为 "${usernameForm.newUsername}" 吗？`, '安全提示', { type: 'warning' })
    .then(async () => {
      const res = await updateUsername({ username: usernameForm.newUsername, password: usernameForm.password })
      if (res.code === 200) logout()
    }).catch(() => {})
}

const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: (rule, val, cb) => val !== pwdForm.newPassword ? cb(new Error('两次输入不一致')) : cb(), trigger: 'blur' }]
}

const submitPwdChange = () => {
  pwdFormRef.value.validate((valid) => {
    if (valid) {
      ElMessageBox.confirm('密码更改后需重新登录，确定继续？', '确认', { type: 'warning' })
        .then(async () => {
          const res = await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
          if (res.code === 200) logout()
        }).catch(() => {})
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前系统吗？', '登出确认', { type: 'warning' }).then(() => logout())
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.profile-container { padding: 40px; background-color: #fcfdfe; min-height: 90vh; }

/* 左侧卡片美化 */
.user-info-card { border-radius: 16px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.user-identity { padding: 30px 0 20px; text-align: center; }
.profile-avatar { border: 5px solid #fff; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }

.default-bg { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); font-size: 44px; }

.user-name { margin-top: 20px; font-size: 24px; color: #334155; font-weight: 700; }

.logout-section { padding: 20px; border-top: 1px solid #f1f5f9; margin-top: 10px; }
.logout-btn { height: 42px; font-weight: 500; }
.w-full { width: 100%; }

/* 头像上传交互 */
.avatar-uploader { position: relative; width: 110px; margin: 0 auto; cursor: pointer; border-radius: 50%; }
.avatar-edit-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.45); color: #fff; opacity: 0;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  transition: 0.3s; border-radius: 50%; font-size: 13px;
}
.avatar-uploader:hover .avatar-edit-overlay { opacity: 1; }

/* 头像包装器与重置按钮 */
.avatar-wrapper { position: relative; display: inline-block; }
.reset-avatar-icon {
  position: absolute; right: -5px; bottom: 5px;
  width: 28px; height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%; display: flex; justify-content: center; align-items: center;
  cursor: pointer; box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease; color: #fff; font-size: 14px;
  border: 2px solid #fff;
}
.reset-avatar-icon:hover {
  transform: scale(1.1) rotate(-30deg);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.6);
}

/* 右侧内容堆叠 */
.settings-wrapper { display: flex; flex-direction: column; gap: 24px; }
.settings-card { border-radius: 16px; border: 1px solid #f1f5f9; }
.card-title-box { display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 17px; color: #1e293b; }

.plan-setting { display: flex; align-items: center; padding: 15px 0; flex-wrap: wrap; gap: 10px; }
.setting-label { width: 140px; font-size: 15px; color: #64748b; }
.setting-action { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.setting-hint { font-size: 13px; color: #94a3b8; }

.config-section { padding: 10px 0; }
.config-title { margin-bottom: 25px; font-size: 16px; color: #475569; font-weight: 600; display: inline-block; border-bottom: 2px solid #3b82f6; padding-bottom: 4px; }
.save-btn { padding: 10px 25px; font-weight: 500; border-radius: 8px; }

:deep(.el-form-item__label) { padding-bottom: 8px; font-weight: 500; color: #64748b; }

/* 移动端适配 */
@media (max-width: 768px) {
  .profile-container { padding: 12px; min-height: auto; }
  
  .user-info-card { margin-bottom: 16px; }
  .user-identity { padding: 20px 0 15px; }
  .user-name { font-size: 20px; margin-top: 15px; }
  
  .settings-wrapper { gap: 16px; }
  .setting-label { width: 100%; margin-bottom: 8px; }
  .plan-setting { flex-direction: column; align-items: flex-start; }
  
  .config-title { font-size: 14px; margin-bottom: 15px; }
  .save-btn { width: 100%; margin-top: 10px; }
}
</style>