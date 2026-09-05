<template>
  <div class="spot-manage">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon" style="background: linear-gradient(135deg, #00b09b, #96c93d);">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div>
              <h3>景点管理</h3>
              <p>全国景点数据维护, 支持 Excel 批量导入导出</p>
            </div>
          </div>
          <div class="card-stat" style="background: #edfaef;">
            <el-icon><DataAnalysis /></el-icon>
            <span>共 <strong style="color:#00b09b;">{{ page.total }}</strong> 个景点</span>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="searchForm.cityId" placeholder="选择城市" style="width: 200px" clearable>
          <el-option v-for="city in cityOptions" :key="city.id" :label="city.province + '·' + city.city" :value="city.id" />
        </el-select>
        <el-input v-model="searchForm.spotName" placeholder="景点名称" style="width: 170px" clearable :prefix-icon="Search" />
        <el-select v-model="searchForm.spotType" placeholder="景区等级" style="width: 150px" clearable>
          <el-option v-for="type in spotTypes" :key="type" :label="type" :value="type" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        <el-divider direction="vertical" />
        <el-button type="success" :icon="Plus" @click="handleAdd">新增景点</el-button>
        <el-button type="warning" :icon="UploadFilled" @click="handleImport">导入Excel</el-button>
        <el-button :icon="Download" @click="handleExport">导出Excel</el-button>
        <el-button type="danger" :icon="Delete" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="modern-table" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="province" label="省份" width="90" />
        <el-table-column prop="city" label="城市" width="90" />
        <el-table-column prop="spotName" label="景点名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="等级" width="100">
          <template #default="scope">
            <el-tag :type="getSpotTagType(scope.row.spotType)" effect="light" round size="small">{{ scope.row.spotType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="scope">
            <span :style="{ color: scope.row.score >= 4 ? '#00b09b' : scope.row.score >= 3 ? '#e6a23c' : '#c0c4cc', fontWeight: 600 }">
              {{ scope.row.score?.toFixed(1) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="90">
          <template #default="scope">
            <span v-if="scope.row.ticketPrice === 0" style="color:#00b09b;font-weight:600;">免费</span>
            <span v-else>¥{{ scope.row.ticketPrice?.toFixed(0) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="城市" prop="cityId">
          <el-select v-model="formData.cityId" placeholder="请选择城市" style="width: 100%">
            <el-option v-for="city in cityOptions" :key="city.id" :label="city.province + city.city" :value="city.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="景点名称" prop="spotName">
          <el-input v-model="formData.spotName" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="景区等级">
          <el-select v-model="formData.spotType" placeholder="请选择景区等级" style="width: 100%">
            <el-option v-for="type in spotTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="formData.score" :min="0" :max="5" :precision="1" />
        </el-form-item>
        <el-form-item label="门票价格">
          <el-input-number v-model="formData.ticketPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="formData.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="景点介绍">
          <el-input v-model="formData.spotIntro" type="textarea" :rows="4" placeholder="请输入景点介绍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importVisible" title="导入景点数据" width="420px">
      <el-upload ref="uploadRef" :auto-upload="false" :limit="1" accept=".xlsx,.xls,.csv" :on-change="handleFileChange">
        <el-button type="primary" :icon="UploadFilled">选择文件</el-button>
        <template #tip>
          <div class="upload-tip">支持 xlsx、xls、csv 格式</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Search, Refresh, Plus, UploadFilled, Download, Delete, Edit, DataAnalysis } from '@element-plus/icons-vue'
import { getSpotPage, createSpot, updateSpot, deleteSpot, batchDeleteSpot, importSpot, exportSpot } from '@/api/spot.js'
import { getCityPage } from '@/api/city.js'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const cityOptions = ref([])
const spotTypes = ['5A景区', '4A景区', '3A景区', '未评级']

function getSpotTagType(type) {
  const map = { '5A景区': 'danger', '4A景区': 'warning', '3A景区': 'primary', '未评级': 'info' }
  return map[type] || 'info'
}

const searchForm = reactive({ cityId: null, spotName: '', spotType: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑景点' : '新增景点')
const isEdit = ref(false)
const formRef = ref()

const formData = reactive({
  id: null, cityId: null, district: '', spotName: '', spotType: '',
  score: 5, ticketPrice: 0, salesVolume: 0, spotIntro: '', address: '', starLevel: 3
})

const formRules = {
  cityId: [{ required: true, message: '请选择城市', trigger: 'change' }],
  spotName: [{ required: true, message: '请输入景点名称', trigger: 'blur' }]
}

const importVisible = ref(false)
const importing = ref(false)
const uploadRef = ref()
const importFile = ref(null)

async function loadCities() {
  try {
    const res = await getCityPage({ current: 1, size: 1000 })
    cityOptions.value = res.records || []
  } catch (err) { console.error(err) }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSpotPage({
      current: page.current, size: page.size,
      cityId: searchForm.cityId, spotName: searchForm.spotName, spotType: searchForm.spotType
    })
    tableData.value = res.records
    page.total = res.total
  } catch (err) { console.error(err) }
  finally { loading.value = false }
}

function resetSearch() {
  Object.assign(searchForm, { cityId: null, spotName: '', spotType: '' })
  loadData()
}

function handleSelectionChange(rows) { selectedRows.value = rows }

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, { id: null, cityId: null, district: '', spotName: '', spotType: '', score: 5, ticketPrice: 0, salesVolume: 0, spotIntro: '', address: '', starLevel: 3 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id, cityId: row.cityId, district: row.district || '', spotName: row.spotName,
    spotType: row.spotType, score: row.score, ticketPrice: row.ticketPrice,
    salesVolume: row.salesVolume, spotIntro: row.spotIntro || '', address: row.address, starLevel: row.starLevel
  })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
    if (isEdit.value) await updateSpot(formData)
    else await createSpot(formData)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (err) { console.error(err) }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除景点"${row.spotName}"吗？`, '提示', { type: 'warning' })
    await deleteSpot(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) { if (err !== 'cancel') console.error(err) }
}

async function handleBatchDelete() {
  if (selectedRows.value.length === 0) { ElMessage.warning('请先选择要删除的景点'); return }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条景点吗？`, '提示', { type: 'warning' })
    const ids = selectedRows.value.map(r => r.id)
    await batchDeleteSpot(ids)
    ElMessage.success('批量删除成功')
    loadData()
  } catch (err) { if (err !== 'cancel') console.error(err) }
}

function handleImport() { importFile.value = null; importVisible.value = true }
function handleFileChange(file) { importFile.value = file.raw }

async function doImport() {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    const res = await importSpot(importFile.value)
    ElMessage.success(`导入完成：成功${res.success}条，丢弃${res.fail}条`)
    importVisible.value = false
    loadData()
  } catch (err) { console.error(err) }
  finally { importing.value = false }
}

async function handleExport() {
  try {
    const res = await exportSpot(searchForm)
    const blob = new Blob([res.data], { type: 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `景点数据导出_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
  } catch (err) { console.error(err) }
}

onMounted(() => { loadCities(); loadData() })
</script>

<style scoped>
.page-card { border-radius: 12px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { display: flex; align-items: center; gap: 14px; }
.title-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 20px;
  box-shadow: 0 4px 12px rgba(0,176,155,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 13px; padding: 6px 14px; border-radius: 20px; }

.search-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }

.modern-table { border-radius: 10px; overflow: hidden; }
.modern-table :deep(th.el-table__cell) { background: #f5f7fa !important; color: #606266; font-weight: 600; }

.pagination-container { margin-top: 18px; display: flex; justify-content: flex-end; }
.upload-tip { margin-top: 10px; color: #999; font-size: 12px; }
</style>
