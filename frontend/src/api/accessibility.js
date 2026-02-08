import axios from 'axios'

export function fetchAccessiblePois(lon, lat, radius = 1000) {
  // 移除 http://localhost:8080，改用 /api 开头
  return axios.get('/api/accessibility/pois', {
    params: { lon, lat, radius }
  })
}
