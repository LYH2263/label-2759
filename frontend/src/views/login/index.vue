<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left hidden-xs-only">
        <div class="login-bg-title">图书管理系统</div>
        <div class="login-bg-desc">高效、便捷、智能的图书管理解决方案</div>
      </div>
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header">
            <el-icon class="logo-icon"><Reading /></el-icon>
            <span>欢迎登录</span>
          </div>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">立即登录</el-button>
          </el-form-item>
        </el-form>
        <div class="test-account">
          <div class="account-row">
            <span class="label">管理员:</span>
            <span class="code">admin</span> / <span class="code">123456</span>
            <el-button type="primary" link size="small" @click="handleAutoFill('admin')">填充</el-button>
          </div>
          <div class="account-row">
            <span class="label">普通用户:</span>
            <span class="code">testuser</span> / <span class="code">123456</span>
            <el-button type="primary" link size="small" @click="handleAutoFill('user')">填充</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { Reading } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm)
        if (success) {
          router.push('/')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const handleAutoFill = (type: string = 'admin') => {
  if (type === 'admin') {
    loginForm.username = 'admin'
    loginForm.password = '123456'
  } else {
    loginForm.username = 'testuser'
    loginForm.password = '123456'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.login-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 1000px;
  padding: 20px;
}

.login-left {
  flex: 1;
  padding-right: 50px;
  color: #303133;
}

.login-bg-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #409eff;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.login-bg-desc {
  font-size: 20px;
  color: #606266;
  line-height: 1.6;
}

.login-card {
  width: 400px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #303133;
}

.logo-icon {
  margin-right: 8px;
  font-size: 24px;
  color: #409eff;
}

.login-button {
  width: 100%;
  font-size: 16px;
  padding: 12px 0;
  font-weight: 600;
  letter-spacing: 1px;
}

.test-account {
  margin-top: 20px;
  font-size: 14px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.label {
  min-width: 70px;
  text-align: right;
}

.code {
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  color: #606266;
  font-weight: bold;
}

@media screen and (max-width: 768px) {
  .hidden-xs-only {
    display: none !important;
  }
  .login-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
