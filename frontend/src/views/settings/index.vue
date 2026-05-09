<template>
  <div class="settings-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统设置 / 个人中心</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="profile">
          <el-form 
            :model="profileForm" 
            :label-width="isMobile ? 'auto' : '100px'" 
            :label-position="isMobile ? 'top' : 'right'"
            class="settings-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="修改密码" name="password">
          <el-form 
            :model="passwordForm" 
            :label-width="isMobile ? 'auto' : '100px'" 
            :label-position="isMobile ? 'top' : 'right'"
            class="settings-form"
            :rules="passwordRules" 
            ref="passwordFormRef"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProfile, updateProfile, changePassword, type User } from '../../api/user'
import { ElMessage, type FormInstance } from 'element-plus'
import { useMobile } from '@/hooks/useMobile'

const activeTab = ref('profile')
const passwordFormRef = ref<FormInstance>()
const { isMobile } = useMobile()
const profileLoading = ref(false)
const passwordLoading = ref(false)

const profileForm = reactive<User>({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const fetchProfile = async () => {
  try {
    const res = await getProfile()
    if (res.data) {
      Object.assign(profileForm, res.data)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleUpdateProfile = async () => {
  profileLoading.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error(error)
  } finally {
    profileLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error) {
        console.error(error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
}
.settings-form {
  max-width: 500px;
}
@media screen and (max-width: 768px) {
  .settings-form {
    max-width: 100%;
  }
}
</style>
