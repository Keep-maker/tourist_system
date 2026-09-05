<template>
  <div class="city-manage">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
              <el-icon><Location /></el-icon>
            </div>
            <div>
              <h3>城市管理</h3>
              <p>全国城市数据维护, 按行政区划代码排序</p>
            </div>
          </div>
          <div class="card-stat" style="background: #fff0f3;">
            <el-icon><DataAnalysis /></el-icon>
            <span>共 <strong style="color:#f5576c;">{{ page.total }}</strong> 个城市</span>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.province" placeholder="省份" style="width: 160px" clearable :prefix-icon="Location" />
        <el-input v-model="searchForm.city" placeholder="城市" style="width: 160px" clearable :prefix-icon="OfficeBuilding" />
        <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        <el-button type="success" :icon="Plus" @click="handleAdd">新增城市</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="modern-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="province" label="省份" />
        <el-table-column prop="city" label="城市" />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="省份" prop="province">
          <el-input v-model="formData.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="formData.city" placeholder="请输入城市" />
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
import { Location, Search, Refresh, Plus, Edit, Delete, DataAnalysis, OfficeBuilding } from '@element-plus/icons-vue'
import { getCityPage, createCity, updateCity, deleteCity } from '@/api/city.js'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ province: '', city: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑城市' : '新增城市')
const isEdit = ref(false)
const formRef = ref()

const formData = reactive({ id: null, province: '', city: '' })

const formRules = {
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCityPage({
      current: page.current, size: page.size,
      province: searchForm.province, city: searchForm.city
    })
    tableData.value = res.records
    page.total = res.total
  } catch (err) { console.error(err) }
  finally { loading.value = false }
}

function resetSearch() {
  searchForm.province = ''; searchForm.city = ''
  page.current = 1; loadData()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: null, province: '', city: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, { id: row.id, province: row.province, city: row.city })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
    if (isEdit.value) await updateCity(formData)
    else await createCity(formData)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (err) { console.error(err) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除城市"${row.city}"吗？`, '提示', { type: 'warning' })
    await deleteCity(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) { if (err !== 'cancel') console.error(err) }
}

onMounted(loadData)
</script>

<style scoped>
.page-card { border-radius: 12px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { display: flex; align-items: center; gap: 14px; }
.title-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
  box-shadow: 0 4px 12px rgba(240,147,251,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat {
  display: flex; align-items: center; gap: 6px;
  color: #606266; font-size: 13px;
  padding: 6px 14px; border-radius: 20px;
}

.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }

.modern-table { border-radius: 10px; overflow: hidden; }
.modern-table :deep(th.el-table__cell) {
  background: #f5f7fa !important; color: #606266; font-weight: 600;
}

.pagination-container { margin-top: 18px; display: flex; justify-content: flex-end; }
</style>
