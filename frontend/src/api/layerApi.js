import axios from 'axios'

const BASE_URL = 'http://localhost:8082'

export function loadLayer(layer) {
  if (layer.type === 'business') {
    return axios.get(BASE_URL + layer.url)
  }

  if (layer.type === 'display') {
    return axios.get(`${BASE_URL}/api/layer/${layer.table}`)
  }

  throw new Error('未知图层类型')
}
