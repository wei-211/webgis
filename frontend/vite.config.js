import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
      proxy: {
        '/api': {
          target: 'http://localhost:8082',
          changeOrigin: true,
          // 如果后端接口里没有 /api 前缀，则打开下面的注释
          // rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
  resolve: {
    alias: {
      // 这里的 path.resolve 会把相对路径转为绝对路径
      '@': path.resolve(__dirname, './src')
    }
  }
})

