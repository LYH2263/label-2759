<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <el-header class="header">
        <div class="header-left">
          <div class="logo">
            <el-icon class="logo-icon"><Reading /></el-icon>
            <span class="logo-text">图书管理系统</span>
          </div>
          <el-button class="menu-toggle" link @click="toggleSidebar">
            <el-icon><Menu /></el-icon>
          </el-button>
        </div>
        <div class="user-info">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ nickname }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside :width="isCollapse ? '64px' : '220px'" class="aside" :class="{ 'mobile-hidden': isMobile && isCollapse }">
          <el-menu
            router
            :default-active="$route.path"
            class="el-menu-vertical"
            :collapse="isCollapse"
            :collapse-transition="false"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <template #title>首页</template>
            </el-menu-item>
            <el-menu-item index="/books">
              <el-icon><Reading /></el-icon>
              <template #title>图书管理</template>
            </el-menu-item>
            <el-menu-item index="/borrow" v-if="isAdmin">
              <el-icon><List /></el-icon>
              <template #title>借阅管理</template>
            </el-menu-item>
            <el-menu-item index="/my-borrow">
              <el-icon><User /></el-icon>
              <template #title>我的借阅</template>
            </el-menu-item>
            <el-menu-item index="/settings">
              <el-icon><Setting /></el-icon>
              <template #title>系统设置</template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- Mobile Overlay -->
        <div v-if="isMobile && !isCollapse" class="mobile-overlay" @click="toggleSidebar"></div>

        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { House, Reading, List, User, Setting, Menu, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('ROLE_ADMIN'))
const nickname = computed(() => userStore.userInfo.nickname || userStore.userInfo.username || '用户')
const isCollapse = ref(false)
const isMobile = ref(false)

const checkScreenSize = () => {
  const width = window.innerWidth
  if (width < 768) {
    isMobile.value = true
    isCollapse.value = true
  } else {
    isMobile.value = false
    isCollapse.value = false
  }
}

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style scoped>
.common-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
.layout-container {
  height: 100%;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
}
.logo {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  display: flex;
  align-items: center;
  margin-right: 20px;
}
.logo-icon {
  margin-right: 8px;
  font-size: 24px;
}
.menu-toggle {
  display: none;
  font-size: 20px;
  color: #606266;
}
.user-info {
  cursor: pointer;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
  color: #606266;
}
.username {
  margin: 0 8px;
  font-weight: 500;
}
.aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  transition: width 0.3s;
  height: calc(100vh - 60px);
  position: relative;
  z-index: 9;
}
.el-menu-vertical {
  border-right: none;
  height: 100%;
}
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

/* Mobile Responsiveness */
@media screen and (max-width: 768px) {
  .logo-text {
    display: none;
  }
  .menu-toggle {
    display: block;
  }
  .aside {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  }
  .aside.mobile-hidden {
    width: 0 !important;
    overflow: hidden;
  }
  .mobile-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
    z-index: 8;
  }
}

/* Transitions */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
