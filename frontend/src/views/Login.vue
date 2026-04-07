<template>
  <div class="login-container">
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>

    <el-card class="login-card">
      <div class="card-header">
        <h2 class="title">WordLearn</h2>
        <p class="subtitle">为者常成 行者常至</p>
      </div>

      <el-tabs v-model="activeTab" class="custom-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" class="auth-form" @keyup.enter="handleLogin">
            <el-form-item>
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名" 
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                :prefix-icon="Lock" 
                show-password
                size="large"
              />
            </el-form-item>
            <el-button type="primary" class="submit-btn" size="large" @click="handleLogin" :loading="loading" round>
              立即登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" class="auth-form" @keyup.enter="handleRegister">
            <el-form-item>
              <el-input 
                v-model="registerForm.username" 
                placeholder="设置用户名" 
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="设置密码" 
                :prefix-icon="Lock" 
                show-password
                size="large"
              />
            </el-form-item>
            <el-button type="success" class="submit-btn" size="large" @click="handleRegister" round>
              注册账号
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <div class="footer-copyright">
      &copy; 2025 WordLearn by Maki -- @Yinyuki
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { login, register } from '../api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '' })

const handleLogin = async () => {
  if(!loginForm.value.username || !loginForm.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  
  loading.value = true
  try {
    const res = await login(loginForm.value)
    if (res.code === 200 && res.data) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('avatar', res.data.avatar || '')
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if(!registerForm.value.username || !registerForm.value.password) {
    return ElMessage.warning('请填写注册信息')
  }
  
  try {
    const res = await register(registerForm.value)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.value.username = registerForm.value.username
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
/* 1. 全屏背景：清新渐变 */
.login-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); 
  /* 备用更绿色的方案：background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* 2. 背景装饰圆球 */
.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(5px);
  z-index: 0;
  animation: float 8s infinite ease-in-out;
}
.circle-1 {
  width: 300px;
  height: 300px;
  top: -50px;
  left: -50px;
}
.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 50px;
  right: -50px;
  animation-delay: 2s;
}

@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(20px); }
  100% { transform: translateY(0); }
}

/* 3. 登录卡片 */
.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85); /* 半透明白 */
  backdrop-filter: blur(12px); /* 毛玻璃 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  z-index: 1;
  text-align: center;
}

/* 标题区 */
.card-header {
  margin-bottom: 30px;
}
.title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  font-weight: bold;
}
.subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin-top: 5px;
  letter-spacing: 1px;
}

/* 表单控件 */
.auth-form {
  padding: 10px 10px 0;
}
.submit-btn {
  width: 100%;
  margin-top: 10px;
  font-weight: bold;
  height: 45px;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: transform 0.2s;
}
.submit-btn:active {
  transform: scale(0.98);
}

/* 底部版权 */
.footer-copyright {
  position: absolute;
  bottom: 20px;
  color: rgba(0, 0, 0, 0.4);
  font-size: 12px;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 10px;
  }
}
</style>