<template>
  <div class="user-manage">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d);">
              <el-icon><Avatar /></el-icon>
            </div>
            <div>
              <h3>用户管理</h3>
              <p>普通用户账号的启用、禁用与密码重置</p>
            </div>
          </div>
          <div class="card-stat">
            <el-icon><DataAnalysis /></el-icon>
            <span>共 <strong>{{ page.total }}</strong> 个用户</span>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="🔍 输入账号搜索用户"
          style="width: 260px"
          clearable
          :prefix-icon="Search"
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
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
        <el-table-column prop="phone" label="手机号" width="160">
          <template #default="scope">
            {{ scope.row.phone || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="light" round>
              {{ scope.row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              link
              :icon="scope.row.status === 1 ? 'Close' : 'Check'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="primary" link :icon="Key" @click="handleResetPwd(scope.row)">
              重置密码
            </el-button>
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

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="重置用户密码" width="420px">
      <el-form label-width="100px">
        <el-form-item label="用户账号">
          <el-tag type="info">{{ currentUser.adminName }}</el-tag>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" placeholder="请输入新密码(6-20字符)" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, Search, Refresh, DataAnalysis, Key } from '@element-plus/icons-vue'
import { getUserPage, toggleUserStatus, resetUserPassword } from '@/api/admin.js'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const page = reactive({ current: 1, size: 10, total: 0 })

const pwdDialogVisible = ref(false)
const currentUser = ref({})
const newPassword = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage({
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

function handleReset() {
  searchKeyword.value = ''
  page.current = 1
  loadData()
}

async function handleToggleStatus(row) {
  try {
    await ElMessageBox.confirm(
      `确定${row.status === 1 ? '禁用' : '启用'}用户"${row.adminName}"吗?`,
      '提示',
      { type: 'warning' }
    )
    const res = await toggleUserStatus(row.id)
    ElMessage.success(res.msg)
    loadData()
  } catch (err) {
    if (err !== 'cancel') console.error(err)
  }
}

function handleResetPwd(row) {
  currentUser.value = row
  newPassword.value = ''
  pwdDialogVisible.value = true
}

async function confirmResetPwd() {
  if (!newPassword.value) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    await resetUserPassword(currentUser.value.id, newPassword.value)
    ElMessage.success('密码已重置')
    pwdDialogVisible.value = false
  } catch (err) {
    console.error(err)
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-card { border-radius: 12px; border: none; }
.card-header {
  display: flex; justify-content: space-between; align-items: center;
}
.card-title {
  display: flex; align-items: center; gap: 14px;
}
.title-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
  box-shadow: 0 4px 12px rgba(17,153,142,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat {
  display: flex; align-items: center; gap: 6px;
  color: #606266; font-size: 13px;
  background: #e8f8f5; padding: 6px 14px; border-radius: 20px;
}
.card-stat strong { color: #11998e; font-size: 16px; margin: 0 2px; }

.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }

.modern-table { border-radius: 10px; overflow: hidden; }
.modern-table :deep(th.el-table__cell) {
  background: #f5f7fa !important;
  color: #606266; font-weight: 600;
}
.user-cell { display: flex; align-items: center; gap: 8px; }
.mini-avatar {
  background: linear-gradient(135deg, #11998e, #38ef7d);
  color: #fff; font-size: 12px; font-weight: 500;
}

.pagination-container { margin-top: 18px; display: flex; justify-content: flex-end; }
</style>
