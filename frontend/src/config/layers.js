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
    },
   {
       id: 'knn-poi',
       name: '最近邻 POI 查询',
       type: 'function',
       active: false
     },
     {
       id: 'routing',
       name: '路径规划',
       type: 'function',
       active: false
     },
     {
       id: 'route-accessibility',
       name: '道路通达性分析',
       type: 'function',
       active: false
     }

]
