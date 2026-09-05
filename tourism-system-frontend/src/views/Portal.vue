<template>
  <div class="portal">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="filterForm.keyword"
          placeholder="输入景点名称搜索"
          style="width: 200px"
          clearable
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="filterForm.province" placeholder="选择省份" style="width: 150px" clearable @change="handleProvinceChange">
          <el-option v-for="p in provinceOptions" :key="p" :label="p" :value="p" />
        </el-select>
        <el-select v-model="filterForm.cityId" placeholder="选择城市" style="width: 150px" clearable>
          <el-option v-for="c in cityOptions" :key="c.id" :label="c.city" :value="c.id" />
        </el-select>
        <el-select v-model="filterForm.spotType" placeholder="景区等级" style="width: 150px" clearable>
          <el-option v-for="t in spotTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input-number v-model="filterForm.minScore" :min="0" :max="5" placeholder="最低评分" />
        <el-input-number v-model="filterForm.maxScore" :min="0" :max="5" placeholder="最高评分" />
        <el-input-number v-model="filterForm.minPrice" :min="0" placeholder="最低价格" />
        <el-input-number v-model="filterForm.maxPrice" :min="0" placeholder="最高价格" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <!-- 卡片网格展示 -->
      <el-row :gutter="16" v-loading="loading" class="spot-grid">
        <el-col
          v-for="item in tableData"
          :key="item.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card shadow="hover" class="spot-card" @click="handleRowClick(item)" :body-style="{ padding: '0' }">
            <!-- 顶部彩色装饰条, 按等级染色 -->
            <div class="card-accent" :class="accentClass(item.spotType)"></div>
            <div class="card-body">
              <!-- 第一行: 名称 + 等级标签 -->
              <div class="card-title-row">
                <div class="card-title" :title="item.spotName">{{ item.spotName }}</div>
                <el-tag size="small" :type="tagType(item.spotType)" effect="dark">{{ item.spotType }}</el-tag>
              </div>
              <!-- 第二行: 左评分 + 右价格 -->
              <div class="card-info-row">
                <div class="card-rating">
                  <el-rate :model-value="item.score" disabled size="small" />
                  <span class="card-score">{{ item.score }}</span>
                </div>
                <div class="card-price">
                  <span v-if="item.ticketPrice === 0 || item.ticketPrice === null" class="free-tag">免费</span>
                  <span v-else class="price-value">¥{{ item.ticketPrice }}</span>
                </div>
              </div>
              <!-- 第三行: 省市 + 地址 -->
              <div class="card-location" :title="item.address">
                <el-icon><LocationInformation /></el-icon>
                <span class="card-region">{{ item.province }} · {{ item.city }}</span>
                <span class="card-addr">{{ truncateAddr(item.address) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无符合条件的景点" />

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50]"
          :total="page.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 景点详情弹窗 -->
    <el-dialog v-model="detailVisible" title="景点详情" width="650px" @scroll="() => {}">
      <el-descriptions :column="1" v-if="currentDetail" border>
        <el-descriptions-item label="景点名称">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <span>{{ currentDetail.spotName }}</span>
            <el-button
              :type="isFavorited ? 'warning' : 'default'"
              size="small"
              @click="handleFavorite"
            >
              {{ isFavorited ? '★ 已收藏' : '☆ 收藏' }}
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="所属地区">{{ currentDetail.province }} {{ currentDetail.city }}</el-descriptions-item>
        <el-descriptions-item label="景区等级">{{ currentDetail.spotType }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentDetail.score" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="门票价格">
          <span v-if="currentDetail.ticketPrice === 0 || currentDetail.ticketPrice === null">免费</span>
          <span v-else>¥{{ currentDetail.ticketPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ currentDetail.address }}</el-descriptions-item>
        <el-descriptions-item label="景点介绍">{{ currentDetail.spotIntro || '暂无介绍' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 相似景点推荐(基于内容+协同过滤混合算法) -->
      <div class="similar-section" v-loading="recommendLoading">
        <div class="similar-title">🔍 相似景点推荐</div>
        <div class="similar-list" v-if="similarSpots.length">
          <div
            v-for="item in similarSpots"
            :key="item.id"
            class="similar-item"
            @click="handleRowClick(item)"
          >
            <div class="similar-name">{{ item.spotName }}</div>
            <el-tag size="small" :type="tagType(item.spotType)" effect="dark">{{ item.spotType }}</el-tag>
            <div class="similar-info">
              <span>评分{{ item.score }}</span>
              <span v-if="item.ticketPrice === 0 || !item.ticketPrice">免费</span>
              <span v-else>¥{{ item.ticketPrice }}</span>
            </div>
            <div class="similar-sim">相似度 {{ (item.similarity * 100).toFixed(1) }}%</div>
          </div>
        </div>
        <el-empty v-else-if="!recommendLoading" description="暂无推荐" :image-size="40" />
      </div>

      <!-- 评论区 -->
      <div class="comment-section" v-loading="commentLoading">
        <div class="comment-title">💬 用户评论 ({{ commentList.length }})</div>

        <!-- 发表评论 -->
        <div class="comment-form">
          <div class="comment-form-rating">
            <span>评分：</span>
            <el-rate v-model="commentForm.rating" />
          </div>
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="2"
            placeholder="说说你对这个景点的看法..."
            maxlength="500"
            show-word-limit
          />
          <div class="comment-form-btn">
            <el-button type="primary" size="small" @click="handleSubmitComment">发表评论</el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="c in commentList" :key="c.id" class="comment-item">
            <div class="comment-item-header">
              <span class="comment-user">{{ c.userName }}</span>
              <el-rate :model-value="c.rating" disabled size="small" />
              <span class="comment-time">{{ formatTime(c.createTime) }}</span>
            </div>
            <div class="comment-item-content">{{ c.content }}</div>
          </div>
          <el-empty v-if="!commentLoading && commentList.length === 0" description="暂无评论, 快来抢沙发" :image-size="40" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationInformation, Search } from '@element-plus/icons-vue'
import { getSpotPage } from '@/api/spot.js'
import { getProvinces, getCitiesByProvince } from '@/api/city.js'
import { recordBehavior, getSimilarSpots } from '@/api/recommend.js'
import { toggleFavorite, checkFavorite } from '@/api/favorite.js'
import { addComment, getCommentsBySpot } from '@/api/comment.js'

const loading = ref(false)
const tableData = ref([])
const provinceOptions = ref([])
const cityOptions = ref([])
const spotTypes = ['5A景区', '4A景区', '3A景区', '未评级']

const filterForm = reactive({
  keyword: '',
  province: '',
  cityId: null,
  spotType: '',
  minScore: null,
  maxScore: null,
  minPrice: null,
  maxPrice: null
})

const page = reactive({ current: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const currentDetail = ref(null)
// 相似景点推荐列表(基于内容+协同过滤混合算法)
const similarSpots = ref([])
const recommendLoading = ref(false)
// 收藏状态
const isFavorited = ref(false)
// 评论相关
const commentList = ref([])
const commentForm = reactive({ content: '', rating: 5 })
const commentLoading = ref(false)

async function loadProvinces() {
  try {
    const res = await getProvinces()
    provinceOptions.value = res || []
  } catch (err) {
    console.error(err)
  }
}

async function handleProvinceChange() {
  if (filterForm.province) {
    try {
      const res = await getCitiesByProvince(filterForm.province)
      cityOptions.value = res || []
      filterForm.cityId = null
    } catch (err) {
      console.error(err)
    }
  } else {
    cityOptions.value = []
    filterForm.cityId = null
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      current: page.current,
      size: page.size
    }
    if (filterForm.keyword) {
      params.spotName = filterForm.keyword
    }
    if (filterForm.province) {
      params.province = filterForm.province
    }
    if (filterForm.cityId) {
      params.cityId = filterForm.cityId
    }
    if (filterForm.spotType) {
      params.spotType = filterForm.spotType
    }
    if (filterForm.minScore != null) {
      params.minScore = filterForm.minScore
    }
    if (filterForm.maxScore != null) {
      params.maxScore = filterForm.maxScore
    }
    if (filterForm.minPrice != null) {
      params.minPrice = filterForm.minPrice
    }
    if (filterForm.maxPrice != null) {
      params.maxPrice = filterForm.maxPrice
    }
    const res = await getSpotPage(params)
    tableData.value = res.records
    page.total = res.total
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 搜索动作(输入关键字/点查询/改每页条数): 重置到第1页再查询
function handleSearch() {
  page.current = 1
  loadData()
}

function resetFilter() {
  Object.assign(filterForm, { keyword: '', province: '', cityId: null, spotType: '', minScore: null, maxScore: null, minPrice: null, maxPrice: null })
  cityOptions.value = []
  handleSearch()
}

function handleRowClick(row) {
  currentDetail.value = row
  detailVisible.value = true
  similarSpots.value = []
  commentList.value = []
  commentForm.content = ''
  commentForm.rating = 5
  // 上报浏览行为(异步不阻塞, 异常静默, 不影响详情展示)
  recordBehavior({ spotId: row.id, behaviorType: 1 }).catch(() => {})
  // 加载相似景点推荐(混合推荐算法)
  loadSimilarSpots(row.id)
  // 加载收藏状态
  loadFavoriteStatus(row.id)
  // 加载评论列表
  loadComments(row.id)
}

// 加载相似景点推荐
async function loadSimilarSpots(spotId) {
  recommendLoading.value = true
  try {
    const res = await getSimilarSpots({ spotId, topN: 6 })
    similarSpots.value = res || []
  } catch (err) {
    console.error(err)
  } finally {
    recommendLoading.value = false
  }
}

// 加载收藏状态
async function loadFavoriteStatus(spotId) {
  try {
    const res = await checkFavorite(spotId)
    isFavorited.value = res.favorited
  } catch (err) {
    console.error(err)
  }
}

// 切换收藏
async function handleFavorite() {
  try {
    const res = await toggleFavorite(currentDetail.value.id)
    isFavorited.value = res.favorited
    ElMessage.success(res.favorited ? '已收藏' : '已取消收藏')
  } catch (err) {
    console.error(err)
  }
}

// 加载评论列表
async function loadComments(spotId) {
  commentLoading.value = true
  try {
    const res = await getCommentsBySpot(spotId, { current: 1, size: 10 })
    commentList.value = res.records || []
  } catch (err) {
    console.error(err)
  } finally {
    commentLoading.value = false
  }
}

// 发表评论
async function handleSubmitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    await addComment({
      spotId: currentDetail.value.id,
      content: commentForm.content,
      rating: commentForm.rating
    })
    ElMessage.success('评论成功')
    commentForm.content = ''
    commentForm.rating = 5
    loadComments(currentDetail.value.id)
  } catch (err) {
    console.error(err)
  }
}

// 格式化时间
function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

// 根据等级返回 el-tag 的 type (颜色), 5A红色/4A橙色/3A蓝色/其他灰色
function tagType(type) {
  if (!type) return 'info'
  if (type.indexOf('5A') >= 0) return 'danger'
  if (type.indexOf('4A') >= 0) return 'warning'
  if (type.indexOf('3A') >= 0) return 'primary'
  return 'info'
}

// 根据等级返回顶部彩条 class (CSS对应不同颜色)
function accentClass(type) {
  if (!type) return 'accent-gray'
  if (type.indexOf('5A') >= 0) return 'accent-red'
  if (type.indexOf('4A') >= 0) return 'accent-orange'
  if (type.indexOf('3A') >= 0) return 'accent-blue'
  return 'accent-gray'
}

// 地址截断: 取前 14 字
function truncateAddr(addr) {
  if (!addr) return '暂无地址信息'
  return addr.length > 14 ? addr.substring(0, 14) + '…' : addr
}

onMounted(() => {
  loadProvinces()
  loadData()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* ===== 卡片网格 ===== */
.spot-grid {
  min-height: 120px;
}

.spot-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.spot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
  border-color: transparent;
}

/* 顶部彩条 */
.card-accent {
  height: 4px;
  width: 100%;
}
.accent-red { background: linear-gradient(90deg, #f56c6c, #e6a23c); }
.accent-orange { background: linear-gradient(90deg, #e6a23c, #f7c948); }
.accent-blue { background: linear-gradient(90deg, #409eff, #79bbff); }
.accent-gray { background: linear-gradient(90deg, #c0c4cc, #dcdfe6); }

/* 卡片内容容器 */
.card-body {
  padding: 14px 16px 12px;
}

/* 标题行: 名称 + 等级 tag */
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

/* 信息行: 左评分 + 右价格 */
.card-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-rating :deep(.el-rate) {
  height: auto;
}

.card-score {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.card-price {
  text-align: right;
}

.free-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  border-radius: 4px;
  font-weight: 500;
}

.price-value {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}

/* 位置行: 省市 + 地址合并一行 */
.card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.card-location .el-icon {
  color: #c0c4cc;
  flex-shrink: 0;
}

.card-region {
  color: #606266;
  font-weight: 500;
  flex-shrink: 0;
}

.card-addr {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 相似景点推荐区域 ===== */
.similar-section {
  margin-top: 16px;
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
}
.similar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.similar-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.similar-item {
  width: calc(50% - 5px);
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.similar-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
  transform: translateY(-2px);
}
.similar-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.similar-info {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.similar-sim {
  font-size: 11px;
  color: #f56c6c;
  margin-top: 2px;
}

/* ===== 评论区 ===== */
.comment-section {
  margin-top: 16px;
  border-top: 1px dashed #ebeef5;
  padding-top: 12px;
}
.comment-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.comment-form {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 12px;
}
.comment-form-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}
.comment-form-btn {
  text-align: right;
  margin-top: 8px;
}
.comment-list {
  max-height: 260px;
  overflow-y: auto;
}
.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}
.comment-time {
  font-size: 12px;
  color: #909399;
  flex: 1;
  text-align: right;
}
.comment-item-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  padding-left: 4px;
}
</style>
