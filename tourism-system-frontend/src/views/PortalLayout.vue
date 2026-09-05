<template>
  <el-container class="portal-layout">
    <!-- 顶部导航栏 -->
    <el-header class="portal-header">
      <div class="header-logo">
        <el-icon class="logo-icon"><Compass /></el-icon>
        <h3>全国旅游景点信息平台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.85)"
        active-text-color="#fff"
        class="header-menu"
      >
        <el-menu-item index="/portal/spot">
          <el-icon><View /></el-icon>
          <span>景点浏览</span>
        </el-menu-item>
        <el-menu-item index="/portal/guess">
          <el-icon><Star /></el-icon>
          <span>猜你喜欢</span>
        </el-menu-item>
        <el-menu-item index="/portal/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>可视化大屏</span>
        </el-menu-item>
        <el-menu-item index="/portal/favorites">
          <el-icon><StarFilled /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/portal/history">
          <el-icon><Clock /></el-icon>
          <span>浏览历史</span>
        </el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              {{ adminInfo?.adminName?.charAt(0).toUpperCase() || 'U' }}
            </el-avatar>
            <span class="user-name">{{ adminInfo?.adminName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <!-- 内容区域 -->
    <el-main class="portal-main">
      <router-view />
    </el-main>

    <!-- AI旅游助手(悬浮对话) -->
    <AiChat />
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { View, DataAnalysis, Star, StarFilled, Clock, User, Compass, ArrowDown } from '@element-plus/icons-vue'
import { logout } from '@/api/admin.js'
import { getAdminInfo, removeToken } from '@/utils/auth.js'
import AiChat from './AiChat.vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const adminInfo = computed(() => getAdminInfo())

// 下拉菜单命令
function handleCommand(command) {
  if (command === 'logout') handleLogout()
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await logout()
    removeToken()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (err) {
    if (err !== 'cancel') {
      removeToken()
      router.push('/login')
    }
  }
}
</script>

<style scoped>
.portal-layout {
  height: 100vh;
  flex-direction: column;
}

/* 顶部导航: 渐变背景 */
.portal-header {
  background: linear-gradient(90deg, #1e2a47 0%, #2d7cf5 100%);
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.2);
  z-index: 10;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 40px;
}
.logo-icon {
  font-size: 24px;
  color: #ffd04b;
}
.header-logo h3 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
  font-weight: 600;
}

/* 水平菜单 */
.header-menu {
  flex: 1;
  border-bottom: none;
}
.header-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
  border-bottom: none !important;
  transition: all 0.2s;
}
.header-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}
/* 选中项底部亮条 */
.header-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
  border-bottom: 3px solid #ffd04b !important;
}

/* 右侧用户区 */
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}
.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
}
.user-name {
  color: #fff;
  font-size: 14px;
}
:deep(.el-icon) {
  color: rgba(255, 255, 255, 0.8);
}

.portal-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>
