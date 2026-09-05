<template>
  <div class="admin-manage">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon">
              <el-icon><User /></el-icon>
            </div>
            <div>
              <h3>管理员管理</h3>
              <p>系统管理员账号的新增、编辑与删除</p>
            </div>
          </div>
          <div class="card-stat">
            <el-icon><DataAnalysis /></el-icon>
            <span>共 <strong>{{ page.total }}</strong> 个管理员</span>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="🔍 输入账号搜索"
          style="width: 260px"
          clearable
          :prefix-icon="Search"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        <el-button type="success" :icon="Plus" @click="handleAdd">新增管理员</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="modern-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="adminName" label="账号">
          <template #default="scope">
            <div class="user-cell">
              <el-avatar :size="28" class="mini-avatar">{{ scope.row.adminName.charAt(0).toUpperCase() }}</el-avatar>
              <span>{{ scope.row.adminName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" :icon="Edit" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" :icon="Delete" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50]"
          :total="page.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" class="modern-dialog">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="adminName">
          <el-input v-model="formData.adminName" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" v-else>
          <el-input v-model="formData.password" type="password" placeholder="留空则不修改" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Plus, Edit, Delete, DataAnalysis } from '@element-plus/icons-vue'
import { getAdminPage, createAdmin, updateAdmin, deleteAdmin } from '@/api/admin.js'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const page = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑管理员' : '新增管理员')
const isEdit = ref(false)
const formRef = ref()

const formData = reactive({
  id: null,
  adminName: '',
  password: ''
})

const formRules = {
  adminName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminPage({
      current: page.current,
      size: page.size,
      keyword: searchKeyword.value
    })
    tableData.value = res.records
    page.total = res.total
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: null, adminName: '', password: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, { id: row.id, adminName: row.adminName, password: '' })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateAdmin(formData)
    } else {
      await createAdmin(formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (err) {
    console.error(err)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除管理员"${row.adminName}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteAdmin(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) {
    if (err !== 'cancel') console.error(err)
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-card { border-radius: 12px; border: none; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 14px;
}
.title-icon {
  width: 42px; height: 42px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
  box-shadow: 0 4px 12px rgba(45,124,245,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat {
  display: flex; align-items: center; gap: 6px;
  color: #606266; font-size: 13px;
  background: #f0f5ff; padding: 6px 14px; border-radius: 20px;
}
.card-stat strong { color: #2d7cf5; font-size: 16px; margin: 0 2px; }

.search-bar {
  display: flex; gap: 10px; margin-bottom: 16px;
}

.modern-table {
  border-radius: 10px; overflow: hidden;
}
.modern-table :deep(th.el-table__cell) {
  background: #f5f7fa !important;
  color: #606266; font-weight: 600;
}
.user-cell { display: flex; align-items: center; gap: 8px; }
.mini-avatar {
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  color: #fff; font-size: 12px; font-weight: 500;
}

.pagination-container {
  margin-top: 18px; display: flex; justify-content: flex-end;
}
</style>
