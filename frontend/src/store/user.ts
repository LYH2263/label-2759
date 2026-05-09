import { defineStore } from 'pinia'
import { ref } from 'vue'
import service from '../utils/request'
import { ElMessage } from 'element-plus'
import type { User } from '../api/user'
import { getProfile } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User>({})
  const roles = ref<string[]>([])

  const getUserInfo = async () => {
    try {
      const res = await getProfile()
      userInfo.value = res.data
      roles.value = res.data.roles || []
      return res.data
    } catch (error) {
      console.error(error)
      return null
    }
  }

  const login = async (loginForm: any) => {
    try {
      const res = await service.post('/auth/login', loginForm)
      token.value = res.data
      localStorage.setItem('token', res.data)
      ElMessage.success('登录成功')
      await getUserInfo()
      return true
    } catch (error) {
      return false
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    roles,
    login,
    logout,
    getUserInfo
  }
})
