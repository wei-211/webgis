/**
 * 沈阳市空间数据WebGIS管理系统 - 字段映射与格式化工具
 */

// 1. 字段名映射 (英转中) - 严格参考数据库设计文档
export const FRIENDLY_KEYS = {
  // 基础通用字段
  name: '名称',
  fclass: '要素类别',
  code: '分类代码 (Code)',
  osm_id: '原始编号',
  fid: '系统标识 (FID)',
  id: '标识号 (ID)',
  distance: '距离',
  // 建筑与设施 [cite: 21]
  height: '建筑高度',
  type: '功能类型',

  // 交通网络 (道路/铁路) [cite: 27, 28]
  maxspeed: '限速',
  oneway: '单行道',
  ref: '道路编号',
  bridge: '桥梁',
  tunnel: '隧道',
  layer: '层数',

  // 水系与自然 [cite: 34]
  width: '河道宽度',

  // 行政区划与人口 [cite: 24]
  population: '常住人口',
  students:'容纳学生',

  // 业务核心指标 (根据代码逻辑补充)
  capacity: '可容纳数量',
  beds: '床位数',
  // 空间计算与拓扑字段 (pgRouting)
  source: '起点节点编号',
  target: '终点节点编号',
  length_m: '道路长度',
  calc_area: '占地面积',
  source_table: '数据来源表'
};

// 2. 要素分类值映射 (fclass 细分)
export const FCLASS_MAP = {
  // --- 道路等级 ---
    'motorway': '高速公路', 'trunk': '城市干道', 'primary': '一级道路',
    'secondary': '二级道路', 'tertiary': '三级道路', 'residential': '居住区道路',
    'service': '内部/服务道路', 'track': '机耕道', 'footway': '人行道', 'cycleway': '自行车道',

    // --- 铁路与轨道交通  ---
    'subway': '地铁线路',
    'rail': '铁路',
    'tram': '有轨电车',
    'light_rail': '轻轨',
    'monorail': '单轨铁路',

    // --- 水系河流  ---
    'stream': '溪流/小河',
    'water': '水域',
    'river': '河流',
    'canal': '运河',
    'drain': '排水沟',
    'ditch': '沟渠',

    // --- 建筑分类  ---
    'building': '一般建筑物',
    'apartments': '公寓',
    'house': '住宅/别墅',
    'garage': '车库',
    'dormitory':'宿舍',
    // --- 设施与 POI  ---
    'archaeological': '考古遗址','artwork': '艺术品/雕塑','atm': '自动取款机','attraction': '旅游景点',
    'bakery': '面包店','bank': '银行','bar': '酒吧','beauty_shop': '美容店','bench': '长椅','beverages': '饮品店',
    'bicycle_shop': '自行车店','biergarten': '啤酒花园','bookshop': '书店','cafe': '咖啡馆','camp_site': '露营地',
    'car_dealership': '汽车经销商','car_wash': '洗车房','castle': '城堡','chemist': '药店','cinema': '电影院',
    'clinic': '诊所','clothes': '服装店','college': '学院','comms_tower': '通信塔','community_centre': '社区中心',
    'computer_shop': '电脑/数码店','convenience': '便利店','courthouse': '法院','dentist': '牙科诊所',
    'department_store': '百货商店','doityourself': '建材五金店','drinking_water': '饮用水点','embassy': '大使馆',
    'fast_food': '快餐店','fire_station': '消防站','food_court': '美食广场','fountain': '喷泉','furniture_shop': '家具店',
    'gift_shop': '礼品店','golf_course': '高尔夫球场','graveyard': '墓地','greengrocer': '蔬果店','guesthouse': '家庭旅馆',
    'historic': '历史遗迹','hospital': '医院','hostel': '青年旅舍','hotel': '酒店','ice_rink': '溜冰场','kindergarten': '幼儿园',
    'laundry': '洗衣店','library': '图书馆','mall': '购物中心','market_place': '集市','memorial': '纪念碑','mobile_phone_shop': '手机店',
    'monument': '纪念碑','museum': '博物馆','nightclub': '夜总会','observation_tower': '观光塔','office': '办公机构',
    'optician': '眼镜店','park': '公园','pharmacy': '药店','picnic_site': '野餐点','pitch': '运动场','playground': '游乐场',
    'police': '派出所','post_box': '邮筒','post_office': '邮局','prison': '监狱','pub': '酒吧','public_building': '公共建筑',
    'restaurant': '餐厅','ruins': '废墟','school': '学校','shelter': '避雨亭','shoe_shop': '鞋店','sports_centre': '体育中心',
    'sports_shop': '体育用品店','stadium': '体育场','supermarket': '超市','swimming_pool': '游泳池','theatre': '剧院',
    'theme_park': '主题公园','toilet': '公共厕所','tourist_info': '游客中心','tower': '塔','town_hall': '市政厅','track': '跑道',
    'university': '大学','veterinary': '兽医诊所','viewpoint': '观景台','waste_basket': '垃圾桶','wastewater_plant': '污水处理厂',
    'water_tower': '水塔','zoo': '动物园',

    // --- 地表/自然与土地利用 ---
    'park': '公园', 'stadium': '体育场', 'grass': '草地', 'forest': '森林',
    'commercial': '商业用地', 'industrial': '工业用地'
};

export const formatPropertyValue = (key, value) => {
  if (value === null || value === undefined || value === '') return '-';

  // 👉 修改点 1：让 type 也走分类映射字典，这样 hospital 就会变成 医院
  if (key === 'fclass' || key === 'type') return FCLASS_MAP[value] || value;

  const unitMap = {
    'height': ' 米', 'width': ' 米', 'maxspeed': ' km/h',
    'population': ' 人', 'students': ' 人', 'beds': ' 张', 'calc_area': ' ㎡'
  };

  if (unitMap[key]) return `${value}${unitMap[key]}`;

  // 👉 修改点 2：将 distance 和 length_m 统一保留两位小数并加单位
  if (key === 'length_m' || key === 'distance') return `${parseFloat(value).toFixed(2)} 米`;

  if (['bridge', 'tunnel', 'oneway'].includes(key)) {
    return (value === 'T' || value === 'yes' || value === '1') ? '是' : '否';
  }

  return value;
};

/**
 * 2. 获取来源图层的中文标签 (从 MapView.vue 移入)
 */
export const getLayerLabel = (tableName) => {
  if (!tableName) return '地理要素';
  const name = tableName.toLowerCase();

  if (name.includes('build')) return '城市建筑';
  if (name.includes('landuse')) return '土地分类';
  if (name.includes('natural')) return '自然景观';
  if (name.includes('places')) return '行政区域';
  if (name.includes('pofw')) return '标志物';
  if (name.includes('pois')) return '兴趣点';
  if (name.includes('traffic')) return '交通设施';
  if (name.includes('transport')) return '交通枢纽';
  if (name.includes('water')) return '水系河流';
  if (name.includes('road')) return '道路交通';

  return '地理要素';
};

/**
 * 3. 安全解析 Properties JSON (从 MapView.vue 移入)
 */
export const safeParseProps = (props) => {
  if (!props || props === 'undefined') return {};
  if (typeof props === 'object') return props;
  try {
    return JSON.parse(props);
  } catch (e) {
    console.warn('无法解析 properties JSON:', props);
    return {};
  }
};

/**
 * 4. 动态计算核心指标 (从 MapView.vue 移入)
 */
export const getDynamicMetric = (item) => {
  const props = safeParseProps(item.properties);

  if (props.population !== undefined && props.population !== null && props.population !== '') {
    return `常住人口: ${props.population} 人`;
  }

  if (props.capacity !== undefined && props.capacity !== null && props.capacity !== '') {
      if (item.name && (item.name.includes('加油') || item.name.includes('充电') || item.name.includes('停车'))) {
        return `可容纳: ${props.capacity} 辆车`;
      }
      return `可容纳: ${props.capacity} 人`;
  }
  if (props.beds !== undefined && props.beds !== null && props.beds !== '') {
    return `床位: ${props.beds} 张`;
  }
  return '-';
};