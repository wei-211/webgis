import axios from 'axios'
// 创建一个 axios 实例（如果没定义的话）或直接使用 axios
const request = axios.create({
  baseURL: 'http://localhost:8082' // 确保指向你的后端地址
})

export function fetchPresetRoute() {
  return request({
    url: '/api/routing/preset', // 必须与 Controller 的 @GetMapping 一致
    method: 'get'
  })
}

