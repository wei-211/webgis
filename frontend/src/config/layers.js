export const layers = [
  {
    id: 'pois',
    name: '兴趣点（POI）',
    type: 'business',
    url: '/api/pois/geojson'
  },
  {
    id: 'roads',
    name: '道路',
    type: 'business',
    url: '/api/roads/geojson'
  },
  {
    id: 'railways',
    name: '铁路',
    type: 'business',
    url: '/api/railways/geojson'
  },
  {
    id: 'shenyang',
    name: '行政区',
    type: 'business',
    url: '/api/shenyang/geojson'
  },
  {
    id: 'waterways',
    name: '河流',
    type: 'display',
    table: 'waterways'
  },
  {
    id: 'build',
    name: '建筑',
    type: 'display',
    table: 'build'
  },
  {
      id: 'accessibility',
      name: '可达性分析',
      type: 'function', // 标记这是一个功能按钮
      active: false
    }
]
