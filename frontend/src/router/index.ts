import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '../layout/index.vue'
import { useUserStore } from '../store/user'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: 'books',
        name: 'Books',
        component: () => import('../views/book/index.vue'),
        meta: { title: '图书管理', icon: 'Reading' }
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('../views/borrow/index.vue'),
        meta: { title: '借阅管理', icon: 'List', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'my-borrow',
        name: 'MyBorrow',
        component: () => import('../views/my-borrow/index.vue'),
        meta: { title: '我的借阅', icon: 'User' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/settings/index.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()

  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    if (token && userStore.roles.length === 0) {
      await userStore.getUserInfo()
    }

    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const requiredRoles = to.meta.roles as string[]
      const hasPermission = userStore.roles.some(role => requiredRoles.includes(role))
      if (!hasPermission) {
        next('/dashboard')
        return
      }
    }
    
    next()
  }
})

export default router
