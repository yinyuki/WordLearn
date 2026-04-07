<template>
  <div class="common-layout">
    <!-- 移动端顶部导航栏 -->
    <div class="mobile-header show-on-mobile">
      <div class="mobile-logo">WordLearn</div>
      <el-button :icon="Operation" text @click="drawerVisible = true" class="menu-btn" />
    </div>

    <!-- 移动端抽屉菜单 -->
    <el-drawer 
      v-model="drawerVisible" 
      direction="ltr" 
      size="220px"
      :with-header="false"
      class="mobile-drawer"
    >
      <div class="drawer-logo">WordLearn</div>
      <el-menu
        :default-active="$route.path"
        router
        class="drawer-menu"
        @select="drawerVisible = false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon> 
          <span>学习看板</span>
        </el-menu-item>
        
        <el-menu-item index="/books">
          <el-icon><Collection /></el-icon> 
          <span>词书管理</span>
        </el-menu-item>

        <el-menu-item index="/extension-learning">
          <el-icon><Reading /></el-icon> 
          <span>拓展学习</span>
        </el-menu-item>

        <el-menu-item index="/community">
          <el-icon><ChatDotRound /></el-icon> 
          <span>学习社区</span>
        </el-menu-item>

        <el-menu-item index="/mistakes">
          <el-icon><Warning /></el-icon> 
          <span>错题统计</span>
        </el-menu-item>

        <el-menu-item index="/rank">
          <el-icon><Trophy /></el-icon> 
          <span>成就榜单</span>
        </el-menu-item>

        <el-menu-item index="/stats">
          <el-icon><TrendCharts /></el-icon> 
          <span>数据统计</span>
        </el-menu-item>

        <el-menu-item index="/profile">
          <el-icon><User /></el-icon> 
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-container>
      <!-- 桌面端侧边栏 - 移动端隐藏 -->
      <el-aside width="200px" class="sidebar hide-on-mobile">
        <div class="logo">WordLearn</div>
        <el-menu
          :default-active="$route.path"
          router
          class="el-menu-vertical"
          background-color="#f5f7fa"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon> 
            <span>学习看板</span>
          </el-menu-item>
          
          <el-menu-item index="/books">
            <el-icon><Collection /></el-icon> 
            <span>词书管理</span>
          </el-menu-item>

          <el-menu-item index="/extension-learning">
            <el-icon><Reading /></el-icon> 
            <span>拓展学习</span>
          </el-menu-item>

          <el-menu-item index="/community">
            <el-icon><ChatDotRound /></el-icon> 
            <span>学习社区</span>
          </el-menu-item>

          <el-menu-item index="/mistakes">
            <el-icon><Warning /></el-icon> 
            <span>错题统计</span>
          </el-menu-item>

          <el-menu-item index="/rank">
            <el-icon><Trophy /></el-icon> 
            <span>成就榜单</span>
          </el-menu-item>

          <el-menu-item index="/stats">
            <el-icon><TrendCharts /></el-icon> 
            <span>数据统计</span>
          </el-menu-item>

          <el-menu-item index="/profile">
            <el-icon><User /></el-icon> 
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Odometer, 
  TrendCharts, 
  User, 
  ChatDotRound, 
  Warning, 
  Trophy,
  Collection,
  Operation,
  Reading
} from '@element-plus/icons-vue'

const drawerVisible = ref(false)
</script>

<style scoped>
/* 桌面端样式 - 保持原有效果 */
.common-layout, .el-container { height: 100vh; }
.sidebar { background-color: #f5f7fa; border-right: 1px solid #e6e6e6; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 20px; font-weight: bold; color: #409EFF; border-bottom: 1px solid #e6e6e6; }
.el-menu-vertical { border-right: none; flex: 1; }
.main-content { background-color: #f0f2f5; padding: 0; overflow-x: hidden; }
.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.3s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-10px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(10px); }

/* 移动端顶部导航栏 */
.mobile-header {
  display: none; /* 默认隐藏，由 show-on-mobile 控制 */
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 16px;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.mobile-logo {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.menu-btn {
  font-size: 22px;
}

/* 抽屉菜单样式 */
.drawer-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  border-bottom: 1px solid #e6e6e6;
  background: #f5f7fa;
}

.drawer-menu {
  border-right: none;
  background-color: #f5f7fa;
}

/* 移动端布局调整 */
@media (max-width: 768px) {
  .common-layout {
    display: flex;
    flex-direction: column;
    height: 100vh;
  }
  
  .el-container {
    flex: 1;
    height: auto;
  }
  
  .main-content {
    height: calc(100vh - 56px);
    overflow-y: auto;
  }
  
  /* 移动端显示/隐藏控制 */
  .hide-on-mobile {
    display: none !important;
  }
  
  .show-on-mobile {
    display: flex !important;
  }
}
</style>