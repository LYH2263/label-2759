<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>欢迎回来，{{ nickname }}</h1>
        <p>今天是 {{ currentDate }}，{{ welcomeMessage }}</p>
      </div>
      <div class="welcome-img">
        <!-- Optional: Add an illustration here if available, or just keep it clean -->
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="stat-card blue-gradient" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">图书总数</div>
              <div class="stat-value">
                <span class="number">{{ stats.bookCount }}</span>
                <span class="unit">本</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8" v-if="isAdmin">
        <el-card shadow="hover" class="stat-card green-gradient" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">
                <span class="number">{{ stats.userCount }}</span>
                <span class="unit">位</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
        <el-card shadow="hover" class="stat-card orange-gradient" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ isAdmin ? '借阅记录' : '我的借阅' }}</div>
              <div class="stat-value">
                <span class="number">{{ stats.borrowCount }}</span>
                <span class="unit">次</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- Potential future chart section -->
    <!-- <el-row :gutter="20" class="chart-row">
       ...
    </el-row> -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getStatistics } from '../../api/report'
import { getMyBorrowRecords } from '../../api/borrow'
import { Reading, User, Collection } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('ROLE_ADMIN'))
const nickname = computed(() => userStore.userInfo.nickname || userStore.userInfo.username || '用户')

const loading = ref(true)
const stats = ref({
  bookCount: 0,
  userCount: 0,
  borrowCount: 0
})

const currentDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const welcomeMessage = computed(() => {
  return isAdmin.value ? '祝您工作愉快！' : '祝您阅读愉快！'
})

const fetchStats = async () => {
  loading.value = true
  try {
    // Always fetch global stats first to get book count
    const res = await getStatistics()
    if (res.data) {
      stats.value = res.data
    }
    
    // If not admin, override borrow count with personal count
    if (!isAdmin.value) {
      const myBorrowRes = await getMyBorrowRecords({ page: 1, size: 1 })
      if (myBorrowRes.data) {
        stats.value.borrowCount = myBorrowRes.data.total
      }
    }
  } catch (error) {
    console.error('Failed to fetch statistics', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.welcome-section {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px 0;
}

.welcome-content p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.stat-card {
  border: none;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 20px;
  font-size: 30px;
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.stat-info {
  flex: 1;
  color: white;
}

.stat-label {
  font-size: 14px;
  margin-bottom: 5px;
  opacity: 0.9;
}

.stat-value .number {
  font-size: 32px;
  font-weight: bold;
}

.stat-value .unit {
  font-size: 14px;
  margin-left: 5px;
  opacity: 0.8;
}

.blue-gradient {
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
}

.green-gradient {
  background: linear-gradient(135deg, #11998e, #38ef7d);
}

.orange-gradient {
  background: linear-gradient(135deg, #fc4a1a, #f7b733);
}

/* Responsive adjustments */
@media screen and (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    align-items: flex-start;
    padding: 15px;
  }
  
  .stat-card {
    margin-bottom: 15px;
  }
}
</style>
