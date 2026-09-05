<template>
  <div class="login-container">
    <!-- 装饰气泡 -->
    <div class="bubble b1"></div>
    <div class="bubble b2"></div>
    <div class="bubble b3"></div>
    <div class="bubble b4"></div>

    <div class="login-box">
      <!-- Logo + 标题 -->
      <div class="login-header">
        <div class="logo-circle">
          <el-icon class="logo-icon"><Compass /></el-icon>
        </div>
        <h2 class="login-title">全国旅游景点信息管理系统</h2>
        <p class="login-subtitle">National Tourist Attractions Management System</p>
      </div>

      <!-- 角色选择标签页 -->
      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <el-tab-pane label="用户端" name="user">
          <template #label>
            <div class="tab-label">
              <el-icon><View /></el-icon>
              <span>用户端</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane label="管理员端" name="admin">
          <template #label>
            <div class="tab-label">
              <el-icon><Setting /></el-icon>
              <span>管理员端</span>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="adminName">
          <el-input
            v-model="loginForm.adminName"
            :placeholder="activeTab === 'admin' ? '请输入管理员账号' : '请输入用户账号'"
            :prefix-icon="User"
            size="large"
            autocomplete="off"
            name="tourism-username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            autocomplete="new-password"
            name="tourism-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="handleLogin" :loading="loading">
            {{ activeTab === 'admin' ? '管理员登录' : '用户登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        <span v-if="activeTab === 'admin'">
          <el-icon><InfoFilled /></el-icon> 请输入管理员账号密码登录
        </span>
        <span v-else>
          <el-icon><InfoFilled /></el-icon>
          用户账号：tourist / 123456　
          <el-link type="primary" :underline="false" @click="router.push('/register')">没有账号？去注册</el-link>
        </span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Setting, Compass, InfoFilled } from '@element-plus/icons-vue'
import { login } from '@/api/admin.js'
import { setToken, setAdminInfo } from '@/utils/auth.js'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const activeTab = ref('user')

const loginForm = reactive({
  adminName: '',
  password: '',
  role: 0
})

const rules = {
  adminName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

watch(activeTab, (newTab) => {
  // 两个端都不预填账号密码
  loginForm.adminName = ''
  loginForm.password = ''
  loginForm.role = newTab === 'admin' ? 1 : 0
  loginFormRef.value?.clearValidate()
})

async function handleLogin() {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const res = await login({
      adminName: loginForm.adminName,
      password: loginForm.password,
      role: loginForm.role
    })
    setToken(res.token)
    setAdminInfo({ adminId: res.adminId, adminName: res.adminName })
    ElMessage.success({ message: '登录成功', duration: 400 })
    if (loginForm.role === 1) {
      router.push('/admin/manage')
    } else {
      router.push('/portal/spot')
    }
  } catch (err) {
    // 错误在 request.js 处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d7cf5 50%, #667eea 100%);
  overflow: hidden;
}

/* 装饰气泡 */
.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: floatBubble 8s ease-in-out infinite;
}
.bubble.b1 { width: 300px; height: 300px; top: -80px; left: -80px; animation-delay: 0s; }
.bubble.b2 { width: 200px; height: 200px; bottom: -50px; right: -50px; animation-delay: 2s; }
.bubble.b3 { width: 120px; height: 120px; top: 40%; right: 10%; animation-delay: 4s; }
.bubble.b4 { width: 80px; height: 80px; bottom: 30%; left: 15%; animation-delay: 6s; }
@keyframes floatBubble {
  0%, 100% { transform: translateY(0) translateX(0); }
  50% { transform: translateY(-20px) translateX(10px); }
}

/* 登录卡片 */
.login-box {
  position: relative;
  width: 420px;
  padding: 48px 40px 32px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

/* 头部 */
.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.logo-circle {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2d7cf5 0%, #667eea 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(45, 124, 245, 0.4);
}
.logo-icon {
  font-size: 32px;
  color: #fff;
}
.login-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
  color: #1e2a47;
}
.login-subtitle {
  margin: 0;
  font-size: 12px;
  color: #909399;
  letter-spacing: 0.5px;
}

/* Tabs */
.login-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}
.login-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #2d7cf5, #667eea);
}
.tab-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 表单输入框美化 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #2d7cf5 inset;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  background: linear-gradient(90deg, #2d7cf5 0%, #667eea 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(45, 124, 245, 0.35);
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(45, 124, 245, 0.45);
}

/* 提示 */
.login-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

/* 底部版权 */
.login-footer {
  position: absolute;
  bottom: 24px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  z-index: 1;
}
</style>
