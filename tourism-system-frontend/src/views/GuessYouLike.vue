<template>
  <div class="guess-page">
    <el-card>
      <div class="header">
        <div class="title">🎯 猜你喜欢</div>
        <el-button type="primary" @click="loadData" :loading="loading">刷新推荐</el-button>
      </div>
      <!-- 推荐说明 -->
      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="tip-alert"
      >
        基于您的浏览记录智能推荐(混合推荐算法: 内容相似度 + 协同过滤)。浏览更多景点后刷新, 推荐将更精准!
      </el-alert>

      <!-- 推荐景点卡片网格 -->
      <el-row :gutter="16" v-loading="loading" class="spot-grid">
        <el-col
          v-for="(item, idx) in recommendList"
          :key="item.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card shadow="hover" class="spot-card" @click="handleClick(item)" :body-style="{ padding: '0' }">
            <div class="card-accent" :class="accentClass(item.spotType)"></div>
            <div class="card-body">
              <!-- 推荐排名徽章 -->
              <div class="rank-badge" v-if="idx < 3">No.{{ idx + 1 }}</div>
              <div class="card-title-row">
                <div class="card-title" :title="item.spotName">{{ item.spotName }}</div>
                <el-tag size="small" :type="tagType(item.spotType)" effect="dark">{{ item.spotType }}</el-tag>
              </div>
              <div class="card-info-row">
                <div class="card-rating">
                  <span class="card-score">⭐ {{ item.score }}</span>
                </div>
                <div class="card-price">
                  <span v-if="!item.ticketPrice" class="free-tag">免费</span>
                  <span v-else class="price-value">¥{{ item.ticketPrice }}</span>
                </div>
              </div>
              <div class="card-location">
                <span>{{ item.province }} · {{ item.city }}</span>
              </div>
              <div class="recommend-score">推荐分 {{ Number(item.similarity).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && recommendList.length === 0" description="暂无推荐数据" />
    </el-card>

    <!-- 景点详情弹窗 -->
    <el-dialog v-model="detailVisible" title="景点详情" width="600px">
      <el-descriptions :column="1" v-if="currentDetail" border>
        <el-descriptions-item label="景点名称">{{ currentDetail.spotName }}</el-descriptions-item>
        <el-descriptions-item label="所属地区">{{ currentDetail.province }} {{ currentDetail.city }}</el-descriptions-item>
        <el-descriptions-item label="景区等级">{{ currentDetail.spotType }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentDetail.score }}</el-descriptions-item>
        <el-descriptions-item label="门票价格">
          <span v-if="!currentDetail.ticketPrice">免费</span>
          <span v-else>¥{{ currentDetail.ticketPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ currentDetail.address }}</el-descriptions-item>
        <el-descriptions-item label="景点介绍">{{ currentDetail.spotIntro || '暂无介绍' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGuessYouLike, recordBehavior } from '@/api/recommend.js'

const loading = ref(false)
const recommendList = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

// 加载猜你喜欢推荐(基于用户浏览历史的混合推荐, 新用户冷启动退化为热门)
async function loadData() {
  loading.value = true
  try {
    const res = await getGuessYouLike({ topN: 12 })
    recommendList.value = res || []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 点击推荐卡片: 打开详情 + 上报浏览行为
function handleClick(item) {
  currentDetail.value = item
  detailVisible.value = true
  // 上报浏览行为(异步, 异常静默)
  recordBehavior({ spotId: item.id, behaviorType: 1 }).catch(() => {})
}

// 等级标签颜色
function tagType(type) {
  if (!type) return 'info'
  if (type.indexOf('5A') >= 0) return 'danger'
  if (type.indexOf('4A') >= 0) return 'warning'
  if (type.indexOf('3A') >= 0) return 'primary'
  return 'info'
}

// 顶部彩条颜色
function accentClass(type) {
  if (!type) return 'accent-gray'
  if (type.indexOf('5A') >= 0) return 'accent-red'
  if (type.indexOf('4A') >= 0) return 'accent-orange'
  if (type.indexOf('3A') >= 0) return 'accent-blue'
  return 'accent-gray'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.tip-alert {
  margin-bottom: 16px;
}
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
  position: relative;
}
.spot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
  border-color: transparent;
}
.card-accent {
  height: 4px;
  width: 100%;
}
.accent-red { background: linear-gradient(90deg, #f56c6c, #e6a23c); }
.accent-orange { background: linear-gradient(90deg, #e6a23c, #f7c948); }
.accent-blue { background: linear-gradient(90deg, #409eff, #79bbff); }
.accent-gray { background: linear-gradient(90deg, #c0c4cc, #dcdfe6); }
.card-body {
  padding: 14px 16px 12px;
  position: relative;
}
.rank-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 600;
}
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
.card-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
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
.card-location {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.recommend-score {
  font-size: 11px;
  color: #f56c6c;
  font-weight: 500;
}
</style>
