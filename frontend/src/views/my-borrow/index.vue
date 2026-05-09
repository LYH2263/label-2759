<template>
  <div class="app-container">
    <el-table
      :key="tableKey"
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top: 20px;"
      class="responsive-table"
    >
      <el-table-column v-if="!isMobile" label="ID" prop="id" align="center" width="80" />
      <el-table-column label="图书" prop="bookTitle" align="center" min-width="120" />
      <el-table-column v-if="!isMobile" label="借阅时间" prop="borrowDate" align="center" min-width="160">
        <template #default="{ row }">
          {{ formatTime(row.borrowDate) }}
        </template>
      </el-table-column>
      <el-table-column v-if="!isMobileOrTablet" label="应还时间" prop="dueDate" align="center" min-width="160">
        <template #default="{ row }">
          {{ formatTime(row.dueDate) }}
        </template>
      </el-table-column>
      <el-table-column v-if="!isMobileOrTablet" label="归还时间" prop="returnDate" align="center" min-width="160">
        <template #default="{ row }">
          {{ row.returnDate ? formatTime(row.returnDate) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '已归还' : '借阅中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleReturn(row)">
            归还
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-show="total > 0"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.size"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        :layout="isMobile ? 'total, prev, next' : 'total, sizes, prev, pager, next, jumper'"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getMyBorrowRecords, returnBook, type BorrowRecord } from '@/api/borrow'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMobile } from '@/hooks/useMobile'

const list = ref<BorrowRecord[]>([])
const total = ref(0)
const listLoading = ref(true)
const listQuery = reactive({
  page: 1,
  size: 10
})

const { isMobile, isMobileOrTablet } = useMobile()
const tableKey = computed(() => `${isMobile.value}-${isMobileOrTablet.value}`)

const getList = async () => {
  listLoading.value = true
  try {
    const response = await getMyBorrowRecords(listQuery)
    list.value = response.data.records
    total.value = response.data.total
  } finally {
    listLoading.value = false
  }
}

const handleSizeChange = (val: number) => {
  listQuery.size = val
  getList()
}

const handleCurrentChange = (val: number) => {
  listQuery.page = val
  getList()
}

const handleReturn = (row: BorrowRecord) => {
  ElMessageBox.confirm(`确认归还图书《${row.bookTitle}》吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    if (row.id) {
      await returnBook(row.id)
      ElMessage.success('归还成功')
      getList()
    }
  })
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
  background: #fff;
  padding: 10px;
}
@media screen and (max-width: 768px) {
  .hidden-xs-only {
    display: none !important;
  }
}
@media screen and (max-width: 992px) {
  .hidden-sm-and-down {
    display: none !important;
  }
}
</style>
