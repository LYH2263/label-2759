import service from '../utils/request'

export interface User {
  id?: number
  username?: string
  nickname?: string
  email?: string
  phone?: string
  createTime?: string
  roles?: string[]
}

export interface PasswordParams {
  oldPassword?: string
  newPassword?: string
  confirmPassword?: string
}

export const getProfile = () => {
  return service.get('/user/profile')
}

export const updateProfile = (data: User) => {
  return service.put('/user/profile', data)
}

export const changePassword = (data: PasswordParams) => {
  return service.post('/user/password', data)
}
