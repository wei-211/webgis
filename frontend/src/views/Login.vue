<template>
  <div class="login-container">
    <div class="login-box">

      <!-- 标题 -->
      <div class="sys-header">
        <div style="font-size: 40px; margin-bottom: 10px;">🌍</div>
        <h2 class="sys-title">沈阳市空间数据管理WebGIS系统的设计与实现</h2>
        <p class="sys-subtitle">Shenyang 3D WebGIS Analysis System</p>
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

// 状态
const isRegister = ref(false)
const isLoading = ref(false)

// 表单
const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

// 切换登录/注册
const toggleMode = () => {
  isRegister.value = !isRegister.value
}

// 提交
const handleSubmit = async () => {

  // 基础校验
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

    const res = await axios.post(url, form)

    if (res.data.code === 200) {

      // 注册逻辑
      if (isRegister.value) {
        alert('注册成功，请登录')
        isRegister.value = false
      }
      // 登录逻辑
      else {
        localStorage.setItem('user', JSON.stringify(res.data.data))
        alert('登录成功')
        router.push('/map')
      }

    } else {
      alert(res.data.msg)
    }

  } catch (err) {
    console.error(err)
    alert('服务器异常')
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
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.92);
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
</style>