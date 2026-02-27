import axios from 'axios'
export function getNearestPoi(lon, lat) {
  return request({
    url: '/api/analysis/nearest-poi',
    method: 'get',
    params: { lon, lat }
  });
}