<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><Compass /></el-icon>
          <span class="logo-text">旅游景点后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          class="header-menu"
          background-color="transparent"
          text-color="rgba(255,255,255,0.85)"
          active-text-color="#fff"
        >
          <el-menu-item index="/admin/manage">
            <el-icon><User /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/city">
            <el-icon><Location /></el-icon>
            <span>城市管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/spot">
            <el-icon><OfficeBuilding /></el-icon>
            <span>景点管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comment">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>可视化大屏</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <!-- 全屏按钮 -->
        <el-tooltip content="全屏" placement="bottom">
          <el-icon class="header-icon" @click="toggleFullscreen"><FullScreen /></el-icon>
        </el-tooltip>
        <!-- 用户头像下拉 -->
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32" class="user-avatar">
              {{ adminInfo?.adminName?.charAt(0).toUpperCase() || 'A' }}
            </el-avatar>
            <span class="admin-name">{{ adminInfo?.adminName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>管理员账号</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 面包屑 -->
    <div class="breadcrumb-bar">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin/manage' }">管理后台</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  User, Location, OfficeBuilding, View, DataAnalysis,
  ChatDotRound, Avatar, Compass, ArrowDown, FullScreen
} from '@element-plus/icons-vue'
import { logout } from '@/api/admin.js'
import { getAdminInfo, removeToken } from '@/utils/auth.js'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const adminInfo = computed(() => getAdminInfo())

// 下拉菜单命令
function handleCommand(command) {
  if (command === 'logout') handleLogout()
}

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

// 全屏切换
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  flex-direction: column;
}

/* ===== 顶部导航: 渐变背景 ===== */
.header {
  background: linear-gradient(90deg, #1e2a47 0%, #2b3a5a 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 21, 41, 0.2);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 30px;
}
.logo-icon {
  font-size: 24px;
  color: #409eff;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

/* 水平菜单 */
.header-menu {
  border-bottom: none;
  flex: 1;
}
.header-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
  border-bottom: none !important;
  margin: 0 4px;
  border-radius: 6px 6px 0 0;
  transition: all 0.2s;
}
.header-menu .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.2) !important;
  color: #fff !important;
}
.header-menu .el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.25) !important;
  color: #fff !important;
  border-bottom: 3px solid #409eff !important;
}

/* 右侧用户区 */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.header-icon {
  font-size: 18px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s;
}
.header-icon:hover {
  color: #fff;
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
  background: linear-gradient(135deg, #409eff 0%, #2d7cf5 100%);
  color: #fff;
  font-weight: 600;
}
.admin-name {
  color: #fff;
  font-size: 14px;
}

/* ===== 面包屑栏 ===== */
.breadcrumb-bar {
  background: #fff;
  padding: 10px 20px;
  border-bottom: 1px solid #ebeef5;
}

/* ===== 主内容区 ===== */
.main-content {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}
</style>
