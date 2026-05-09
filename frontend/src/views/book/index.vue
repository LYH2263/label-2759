<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="listQuery.title" placeholder="书名" class="filter-item search-input" @keyup.enter="handleFilter" />
      <el-input v-model="listQuery.category" placeholder="分类" class="filter-item search-input" @keyup.enter="handleFilter" />
      <el-button class="filter-item" type="primary" icon="Search" @click="handleFilter">
        搜索
      </el-button>
      <el-button v-if="isAdmin" class="filter-item" type="primary" icon="Edit" @click="handleCreate">
        新增图书
      </el-button>
    </div>

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
      <el-table-column label="ID" prop="id" align="center" width="80" />
      <el-table-column label="书名" prop="title" align="center" min-width="120" />
      <el-table-column label="作者" prop="author" align="center" min-width="100" />
      <el-table-column label="ISBN" prop="isbn" align="center" min-width="120" v-if="!isMobile" />
      <el-table-column label="分类" prop="category" align="center" width="100" />
      <el-table-column label="价格" prop="price" align="center" width="80" v-if="!isMobile" />
      <el-table-column label="库存" prop="stock" align="center" width="80" />
      <el-table-column label="描述" prop="description" align="center" show-overflow-tooltip v-if="!isMobileOrTablet" />
      <el-table-column label="操作" align="center" :width="isMobile ? 100 : 230" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <div :class="{'action-column': true, 'mobile': isMobile}">
            <el-button v-if="isAdmin" type="primary" size="small" @click="handleUpdate(row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="handleBorrow(row)" :disabled="row.stock <= 0">
              借阅
            </el-button>
            <el-button v-if="isAdmin" size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
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

    <el-dialog :title="textMap[dialogStatus]" v-model="dialogFormVisible" :width="isMobile ? '90%' : '500px'">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="80px" style="max-width: 400px; margin: 0 auto;">
        <el-form-item label="书名" prop="title">
          <el-input v-model="temp.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="temp.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="temp.isbn" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="temp.category" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model.number="temp.price" type="number" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input v-model.number="temp.stock" type="number" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="temp.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">
            取消
          </el-button>
          <el-button type="primary" @click="dialogStatus === 'create' ? createData() : updateData()">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { getBookList, createBook, updateBook, deleteBook, type Book } from '@/api/book'
import { borrowBook } from '@/api/borrow'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useMobile } from '@/hooks/useMobile'
import { useUserStore } from '@/store/user'

const list = ref<Book[]>([])
const total = ref(0)
const listLoading = ref(true)
const listQuery = reactive({
  page: 1,
  size: 10,
  title: undefined,
  category: undefined
})

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('ROLE_ADMIN'))

const { isMobile, isMobileOrTablet } = useMobile()
const tableKey = computed(() => `${isMobile.value}-${isMobileOrTablet.value}`)

const temp = ref<Book>({
  title: '',
  author: '',
  isbn: '',
  category: '',
  price: 0,
  stock: 0,
  description: ''
})

const dialogFormVisible = ref(false)
const dialogStatus = ref<'create' | 'update'>('create')
const textMap = {
  update: '编辑',
  create: '新增'
}

const dataForm = ref<FormInstance>()

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', message: '价格必须为数字', trigger: 'blur' },
    { validator: (_rule: any, value: any, callback: any) => {
        if (value < 0) {
          callback(new Error('价格不能小于0'))
        } else {
          callback()
        }
      }, trigger: 'blur' 
    }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', message: '库存必须为数字', trigger: 'blur' },
    { validator: (_rule: any, value: any, callback: any) => {
        if (!Number.isInteger(value)) {
          callback(new Error('库存必须为整数'))
        } else if (value < 0) {
          callback(new Error('库存不能小于0'))
        } else {
          callback()
        }
      }, trigger: 'blur' 
    }
  ]
}

const getList = async () => {
  listLoading.value = true
  try {
    const response = await getBookList(listQuery)
    list.value = response.data.records
    total.value = response.data.total
  } finally {
    listLoading.value = false
  }
}

const handleFilter = () => {
  listQuery.page = 1
  getList()
}

const handleSizeChange = (val: number) => {
  listQuery.size = val
  getList()
}

const handleCurrentChange = (val: number) => {
  listQuery.page = val
  getList()
}

const resetTemp = () => {
  temp.value = {
    title: '',
    author: '',
    isbn: '',
    category: '',
    price: 0,
    stock: 0,
    description: ''
  }
}

const handleCreate = () => {
  resetTemp()
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
  nextTick(() => {
    dataForm.value?.clearValidate()
  })
}

const createData = () => {
  dataForm.value?.validate(async (valid) => {
    if (valid) {
      await createBook(temp.value)
      dialogFormVisible.value = false
      ElMessage.success('创建成功')
      getList()
    }
  })
}

const handleUpdate = (row: Book) => {
  temp.value = { ...row }
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
  nextTick(() => {
    dataForm.value?.clearValidate()
  })
}

const updateData = () => {
  dataForm.value?.validate(async (valid) => {
    if (valid) {
      await updateBook(temp.value)
      dialogFormVisible.value = false
      ElMessage.success('更新成功')
      getList()
    }
  })
}

const handleDelete = (row: Book) => {
  ElMessageBox.confirm('确认删除该图书吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (row.id) {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      getList()
    }
  })
}

const handleBorrow = (row: Book) => {
  ElMessageBox.confirm(`确认借阅《${row.title}》吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    if (row.id) {
      await borrowBook(row.id)
      ElMessage.success('借阅成功')
      getList() // Refresh stock
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  padding-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.search-input {
  width: 200px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  background: #fff;
  padding: 10px;
}

.action-column.mobile {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.action-column.mobile .el-button {
  margin-left: 0;
  width: 100%;
}

@media screen and (max-width: 768px) {
  .search-input {
    width: 100%;
  }
  
  .filter-item {
    width: 100%;
    margin-left: 0 !important;
  }
  
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
