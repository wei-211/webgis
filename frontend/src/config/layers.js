export const layers = [
  {
    id: 'pois',
    name: '沈阳市兴趣点分布图（POI）',
    type: 'business',
    url: '/api/pois/geojson',
    district: '沈阳市通用图层'
  },
  {
    id: 'roads',
    name: '沈阳市道路分布图',
    type: 'business',
    url: '/api/roads/geojson',
    district: '沈阳市通用图层'
  },
  {
    id: 'railways',
    name: '沈阳市铁路分布图',
    type: 'business',
    url: '/api/railways/geojson',
    district: '沈阳市通用图层'
  },
  {
    id: 'shenyang',
    name: '沈阳市行政区划图',
    type: 'business',
    url: '/api/shenyang/geojson',
    district: '沈阳市通用图层'
  },
  {
    id: 'waterways',
    name: '沈阳市水系分布图',
    type: 'display',
    table: 'waterways',
    district: '沈阳市通用图层'
  },
  {
    id: 'build',
    name: '沈阳市建筑物轮廓图',
    type: 'display',
    table: 'build',
    isBuilding: true,
    district: '沈阳市通用图层'
  },
  // {
    //   id: 'build_heping',
    //   name: '和平区建筑数据',
    //   type: 'display',
    //   table: 'build_heping',
    //   isBuilding: true,
    //   district: '和平区' // 只要写了和平区，前端会自动生成和平区的下拉文件夹
    // },
  {
      id: 'accessibility',
      name: '教育设施服务范围',
      type: 'function', // 标记这是一个功能按钮
      active: false
    },
   {
       id: 'knn-poi',
       name: '最近医疗资源',
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
         id: 'view-3d',
         name: '3D 视图模式',
         type: 'function',
         active: false
       }

]
