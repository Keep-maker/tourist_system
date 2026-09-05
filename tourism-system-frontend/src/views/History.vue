<template>
  <div class="history-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
              <el-icon><Clock /></el-icon>
            </div>
            <div>
              <h3>浏览历史</h3>
              <p>最近浏览过的景点记录</p>
            </div>
          </div>
          <div class="card-actions-header">
            <div class="card-stat" style="background: #f3f0ff;">
              <el-icon><Clock /></el-icon>
              <span>共 <strong style="color:#667eea;">{{ list.length }}</strong> 条记录</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="还没有浏览记录, 快去景点浏览页看看吧~">
        <el-button type="primary" @click="$router.push('/portal/spot')">去浏览景点</el-button>
      </el-empty>

      <!-- 历史列表: 时间轴风格 -->
      <div v-else class="history-list" v-loading="loading">
        <div v-for="(spot, index) in list" :key="spot.id" class="history-item">
          <div class="timeline-dot"></div>
          <div class="timeline-line" v-if="index < list.length - 1"></div>

          <div class="spot-card" @click="$router.push('/portal/spot')">
            <div class="card-left" :style="{ background: getLevelBg(spot.spotType) }">
              <el-icon class="level-icon"><OfficeBuilding /></el-icon>
            </div>
            <div class="card-right">
              <div class="spot-header">
                <h4 class="spot-name">{{ spot.spotName }}</h4>
                <el-tag :type="getSpotTagType(spot.spotType)" effect="light" size="small">{{ spot.spotType }}</el-tag>
              </div>
              <div class="spot-meta">
                <el-icon><LocationFilled /></el-icon>
                <span>{{ spot.province }} · {{ spot.city }}</span>
                <span class="meta-divider">|</span>
                <span class="rating">
                  <el-icon><StarFilled /></el-icon>
                  {{ spot.score?.toFixed(1) || '0.0' }}
                </span>
                <span class="meta-divider">|</span>
                <span class="price" v-if="spot.ticketPrice === 0">免费</span>
                <span class="price" v-else>¥{{ spot.ticketPrice?.toFixed(0) }}</span>
              </div>
              <div class="spot-actions">
                <el-button size="small" type="primary" link @click.stop="$router.push('/portal/spot')">继续查看 →</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Clock, LocationFilled, StarFilled, OfficeBuilding } from '@element-plus/icons-vue'
import { getBrowseHistory } from '@/api/recommend.js'

const loading = ref(false)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    list.value = await getBrowseHistory({ topN: 50 })
  } catch (err) { console.error(err) }
  finally { loading.value = false }
}

function getSpotTagType(type) {
  const map = { '5A景区': 'danger', '4A景区': 'warning', '3A景区': 'primary', '未评级': 'info' }
  return map[type] || 'info'
}
function getLevelBg(type) {
  const map = {
    '5A景区': 'linear-gradient(135deg, #ff6b6b, #ee5a6f)',
    '4A景区': 'linear-gradient(135deg, #f9a825, #f57c00)',
    '3A景区': 'linear-gradient(135deg, #42a5f5, #1e88e5)',
    '未评级': 'linear-gradient(135deg, #bdbdbd, #9e9e9e)'
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
  box-shadow: 0 4px 12px rgba(102,126,234,0.3);
}
.card-title h3 { margin: 0; font-size: 16px; color: #1e2a47; font-weight: 600; }
.card-title p { margin: 2px 0 0; font-size: 12px; color: #909399; }
.card-stat { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 13px; padding: 6px 14px; border-radius: 20px; }

/* 时间轴列表 */
.history-list {
  position: relative;
  padding-left: 20px;
}
.history-item {
  position: relative;
  padding-bottom: 16px;
}
.timeline-dot {
  position: absolute;
  left: -24px;
  top: 20px;
  width: 12px; height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 0 0 4px rgba(102,126,234,0.15);
}
.timeline-line {
  position: absolute;
  left: -19px;
  top: 34px;
  width: 2px;
  bottom: 0;
  background: #e8e8e8;
}

/* 卡片 */
.spot-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  cursor: pointer;
  display: flex;
  transition: all 0.3s;
}
.spot-card:hover {
  transform: translateX(4px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}
.card-left {
  width: 50px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.level-icon { font-size: 22px; color: #fff; }
.card-right { flex: 1; padding: 12px 16px; }
.spot-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 6px;
}
.spot-name {
  margin: 0; font-size: 15px; font-weight: 600; color: #1e2a47;
}
.spot-meta {
  display: flex; align-items: center; gap: 6px;
  color: #909399; font-size: 13px; flex-wrap: wrap;
}
.meta-divider { color: #dcdfe6; }
.rating { color: #f57c00; display: flex; align-items: center; gap: 3px; }
.rating .el-icon { font-size: 13px; }
.price { color: #2d7cf5; font-weight: 600; }
.spot-actions { margin-top: 4px; }
</style>
