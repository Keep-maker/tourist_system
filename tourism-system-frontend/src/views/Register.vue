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
          <el-icon class="logo-icon"><User /></el-icon>
        </div>
        <h2 class="login-title">用户注册</h2>
        <p class="login-subtitle">快速创建账号, 开启旅程</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0">
        <el-form-item prop="adminName">
          <el-input
            v-model="registerForm.adminName"
            placeholder="请设置账号(3-20字符)"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码(6-20字符)"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="手机号(选填)"
            :prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="handleRegister" :loading="loading">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tip">
        已有账号？
        <el-link type="primary" :underline="false" @click="router.push('/login')">返回登录</el-link>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { register } from '@/api/admin.js'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  adminName: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  adminName: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    await register({
      adminName: registerForm.adminName,
      password: registerForm.password,
      phone: registerForm.phone
    })
    ElMessage.success('注册成功, 请登录')
    router.push('/login')
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

.login-box {
  position: relative;
  width: 420px;
  padding: 40px 40px 28px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.logo-circle {
  width: 60px;
  height: 60px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2d7cf5 0%, #667eea 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(45, 124, 245, 0.4);
}
.logo-icon {
  font-size: 28px;
  color: #fff;
}
.login-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #1e2a47;
}
.login-subtitle {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.login-btn {
  width: 100%;
  height: 42px;
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

.login-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.login-footer {
  position: absolute;
  bottom: 24px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  z-index: 1;
}
</style>
