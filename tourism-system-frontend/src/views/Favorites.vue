<template>
  <div class="favorites-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon" style="background: linear-gradient(135deg, #ff6b6b, #ee5a6f);">
              <el-icon><StarFilled /></el-icon>
            </div>
            <div>
              <h3>我的收藏</h3>
              <p>收藏夹中的景点, 点击卡片查看详情</p>
            </div>
          </div>
          <div class="card-stat" style="background: #fff0f0;">
            <el-icon><StarFilled /></el-icon>
            <span>共收藏 <strong style="color:#ee5a6f;">{{ list.length }}</strong> 个景点</span>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何景点, 快去景点浏览页收藏吧~">
        <el-button type="primary" @click="$router.push('/portal/spot')">去浏览景点</el-button>
      </el-empty>

      <!-- 收藏卡片网格 -->
      <div v-else class="spot-grid" v-loading="loading">
        <div v-for="spot in list" :key="spot.id" class="spot-card" @click="$router.push('/portal/spot')">
          <div class="card-accent" :style="{ background: getLevelColor(spot.spotType) }"></div>
          <div class="card-body">
            <div class="spot-header">
              <h4 class="spot-name">{{ spot.spotName }}</h4>
              <el-tag :type="getSpotTagType(spot.spotType)" effect="light" round size="small">{{ spot.spotType }}</el-tag>
            </div>
            <div class="spot-meta">
              <el-icon><LocationFilled /></el-icon>
              <span>{{ spot.province }} · {{ spot.city }}</span>
            </div>
            <div class="spot-info">
              <span class="rating">
                <el-icon><StarFilled /></el-icon>
                {{ spot.score?.toFixed(1) || '0.0' }}
              </span>
              <span class="price" v-if="spot.ticketPrice === 0">免费</span>
              <span class="price" v-else>¥{{ spot.ticketPrice?.toFixed(0) }}</span>
            </div>
          </div>
          <div class="card-actions">
            <el-button size="small" type="primary" link @click.stop="$router.push('/portal/spot')">
              前往浏览
            </el-button>
            <el-popconfirm title="确定从收藏夹移除吗?" @confirm="handleRemove(spot)">
              <template #reference>
                <el-button size="small" type="danger" link @click.stop>移除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, LocationFilled } from '@element-plus/icons-vue'
import { getMyFavorites, removeFavorite } from '@/api/favorite.js'

const loading = ref(false)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    list.value = await getMyFavorites()
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function handleRemove(spot) {
  try {
    await removeFavorite(spot.id)
    ElMessage.success('已从收藏夹移除')
    loadData()
  } catch (err) { console.error(err) }
}

function getSpotTagType(type) {
  const map = { '5A景区': 'danger', '4A景区': 'warning', '3A景区': 'primary', '未评级': 'info' }
  return map[type] || 'info'
}
function getLevelColor(type) {
  const map = {
    '5A景区': 'linear-gradient(180deg, #ff6b6b, #ee5a6f)',
    '4A景区': 'linear-gradient(180deg, #f9a825, #f57c00)',
    '3A景区': 'linear-gradient(180deg, #42a5f5, #1e88e5)',
    '未评级': 'linear-gradient(180deg, #bdbdbd, #9e9e9e)'
  }
  return map[type] || map['未评级']
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
  box-shadow: 0 4px 12px rgba(238,90,111,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 13px; padding: 6px 14px; border-radius: 20px; }

/* 卡片网格 */
.spot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.spot-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}
.spot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}
.card-accent {
  height: 4px;
  width: 100%;
}
.card-body {
  padding: 16px;
  flex: 1;
}
.spot-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 8px;
}
.spot-name {
  margin: 0; font-size: 15px; font-weight: 600; color: #1e2a47;
  flex: 1; margin-right: 8px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.spot-meta {
  display: flex; align-items: center; gap: 4px;
  color: #909399; font-size: 13px; margin-bottom: 10px;
}
.spot-info {
  display: flex; justify-content: space-between; align-items: center;
}
.rating {
  display: flex; align-items: center; gap: 3px;
  color: #f57c00; font-weight: 600; font-size: 14px;
}
.rating .el-icon { font-size: 14px; }
.price {
  font-size: 14px; font-weight: 600; color: #2d7cf5;
}
.card-actions {
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex; justify-content: space-between;
}
</style>
