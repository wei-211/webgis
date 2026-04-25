<template>
  <div class="login-container">
  <header class="top-header">
        <h1>沈阳市空间数据管理系统</h1>
      </header>
    <div class="login-box">

      <!-- 标题 -->
      <div class="sys-header">
        <div style="font-size: 40px; margin-bottom: 10px;">🌍</div>
        <h2 class="sys-title">沈阳市空间数据管理系统</h2>
        <p class="sys-subtitle">Shenyang City Spatial Data  Management System</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" class="login-form">

        <!-- 用户名 -->
        <div class="input-group">
          <label>用户名</label>
          <input
            type="text"
            v-model="form.username"
            placeholder="请输入用户名"
          />
        </div>

        <!-- 密码 -->
        <div class="input-group">
          <label>密码</label>
          <input
            type="password"
            v-model="form.password"
            placeholder="请输入密码"
          />
        </div>

        <!-- 注册时才显示 -->
        <div v-if="isRegister" class="input-group">
          <label>昵称</label>
          <input
            type="text"
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </div>

        <!-- 按钮 -->
        <button type="submit" class="login-btn">
          {{ isLoading
              ? (isRegister ? '注册中...' : '登录中...')
              : (isRegister ? '注册账号' : '系统登录') }}
        </button>

      </form>

      <!-- 切换 -->
      <div class="extra-actions">
        <span @click="toggleMode" class="link">
          {{ isRegister ? '已有账号？去登录' : '没有账号？立即注册' }}
        </span>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 状态控制
const isRegister = ref(false)
const isLoading = ref(false)

// 表单数据：使用 reactive
const form = reactive({
  username: '',
  password: '',
  nickname: '' // 注册时使用的字段
})

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
}

// 提交表单
const handleSubmit = async () => {
  // 1. 基础校验
  if (!form.username || !form.password) {
    alert('请输入用户名和密码')
    return
  }

  if (isRegister.value && !form.nickname) {
    alert('请输入昵称')
    return
  }

  isLoading.value = true

  try {
    const url = isRegister.value
      ? 'http://localhost:8082/user/register'
      : 'http://localhost:8082/user/login'

    // 注意：这里 form 本身就是响应式对象，直接传给 axios
    const res = await axios.post(url, form)

    // 假设后端成功返回 code 为 200
    if (res.data === 'Login successful' || res.data.code === 200 || res.data === 'User registered successfully') {

      if (isRegister.value) {
        alert('注册成功，请登录')
        isRegister.value = false
      } else {
        // --- 核心修改：保存登录信息到浏览器缓存 ---
        // 这样 MapView 页面才能通过 localStorage.getItem('username') 拿到它
        localStorage.setItem('username', form.username);

        // 如果后端返回了完整的用户信息，也可以一起存入
        if (res.data.data) {
          localStorage.setItem('user', JSON.stringify(res.data.data));
        }

        alert('登录成功')
        router.push('/map') // 跳转到地图页面
      }
    } else {
      // 显示后端返回的错误消息
      alert(res.data.msg || res.data || '操作失败')
    }

  } catch (err) {
    console.error('请求异常:', err)
    alert('服务器连接异常，请检查后端服务是否开启')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/styles/DLU.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.sys-header {
  margin-bottom: 30px;
}

.sys-title {
  font-size: 22px;
  color: #333;
  margin: 0 0 5px 0;
  font-weight: bold;
}

.sys-subtitle {
  font-size: 12px;
  color: #666;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  text-align: left;
}

.input-group label {
  font-size: 14px;
  color: #555;
  margin-bottom: 6px;
  display: block;
}

.input-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.input-group input:focus {
  border-color: #2a5298;
}

.login-btn {
  margin-top: 10px;
  padding: 12px;
  background-color: #2a5298;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  font-weight: bold;
}

.login-btn:hover {
  background-color: #1e3c72;
}

.extra-actions {
  margin-top: 15px;
  text-align: right;
}

.link {
  font-size: 13px;
  color: #2a5298;
  cursor: pointer;
}

.link:hover {
  text-decoration: underline;
}
.top-header {
  position: absolute; /* 关键：脱离文档流 */
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  /* 使用与 MapView 一致的渐变色 */
  background: linear-gradient(90deg, #071222 0%, #112a4a 100%);
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.5);
  z-index: 10;
}

.top-header h1 {
  font-size: 18px;
  margin: 0;
  letter-spacing: 1px;
}
</style>