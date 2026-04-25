import axios from 'axios'

export function getNearestPoi(lon, lat) {
  return axios({
    url: 'http://localhost:8082/api/analysis/nearest-poi',
    method: 'get',
    params: { lon, lat }
  });
}