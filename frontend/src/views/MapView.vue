<template>
  <div class="layout-container">
    <header class="top-header">
      <div class="logo-title">
        <h1>沈阳市空间数据WebGIS管理系统的设计与实现</h1>
      </div>
      <div class="user-actions">
        <button class="toggle-mode-btn" @click="toggle3DMode">
          {{ is3D ? '切换到二维地图' : '切换到三维地球' }}
        </button>
        <div class="user-info">
          <span class="avatar">👤</span>
          <span class="username">{{ currentUsername }}</span>
          <span class="logout-link" @click="handleLogout">退出</span>
        </div>
      </div>
    </header>

    <div class="main-body">
      <aside class="sidebar">
        <div class="menu-container">
          <div class="menu-group">
            <div class="group-title" @click="openData = !openData">
              <span>🗺️ 数据图层</span>
              <span class="arrow" :class="{ open: openData }">▼</span>
            </div>
            <div class="group-content" v-show="openData">
              <label
                v-for="layer in dataLayers"
                :key="layer.id"
                class="layer-item"
                :class="{ active: layer.visible }"
              >
                <input
                  type="checkbox"
                  v-model="layer.visible"
                  @change="handleLayerChange(layer)"
                />
                <span class="layer-name">{{ layer.name }}</span>
              </label>
            </div>
          </div>

          <div class="menu-group">
            <div class="group-title" @click="openFunc = !openFunc">
              <span>⚙️ 空间分析功能</span>
              <span class="arrow" :class="{ open: openFunc }">▼</span>
            </div>
            <div class="group-content" v-show="openFunc">
              <button
                v-for="func in functionLayers"
                :key="func.id"
                class="func-btn"
                :class="{ active: functionState[func.id] }"
                @click="handleFunctionClick(func)"
              >
                {{ func.name }}
              </button>
            </div>
          </div>

          <div class="menu-group" v-if="is3D">
            <div class="group-title" @click="open3D = !open3D">
              <span>🧊 三维立体分析</span>
              <span class="arrow" :class="{ open: open3D }">▼</span>
            </div>
            <div class="group-content" v-show="open3D">
              <button class="func-btn" @click="flyToShenyang">飞往沈阳 (视角复位)</button>
              <button class="func-btn" @click="toggleTerrain">切换地形</button>
              <button class="func-btn" @click="toggle3DBuildings">
                {{ show3DBuildings ? '隐藏3D建筑' : '显示3D建筑' }}
              </button>

              <div class="tool-item">
                <label>日照时间模拟 ({{ shadowTime }}:00)</label>
                <input type="range" min="0" max="24" step="1" v-model="shadowTime" @input="updateShadowTime">
                <button class="sub-btn" @click="toggleShadows">
                  {{ isShadowActive ? '关闭日照' : '开启日照' }}
                </button>
              </div>

              <div class="tool-item">
                <label>淹没分析 (当前水位: {{ waterLevel - 40 }}m)</label>
                <button class="sub-btn" @click="startFlood">
                  {{ isFlooding ? '停止淹没' : '开始淹没' }}
                </button>
              </div>
            </div>
          </div>

          <div class="menu-group">
            <div class="group-title" @click="openTools = !openTools">
              <span>🛠️ 空间量测</span>
              <span class="arrow" :class="{ open: openTools }">▼</span>
            </div>
            <div class="group-content tool-flex" v-show="openTools">
              <button class="func-btn half" @click="startMeasure('LineString')">测距</button>
              <button class="func-btn half" @click="startMeasure('Polygon')">测面积</button>
            </div>
          </div>
          <div class="menu-group">
               <div class="group-title" @click="openSearch = !openSearch">
                <span>🔍 空间查询</span>
                <span class="arrow" :class="{ open: openSearch }">▼</span>
               </div>
                <div class="group-content" v-show="openSearch">
                   <div class="search-box">
                        <input
                          type="text"
                          v-model="searchQuery"
                          @keyup.enter="handleSearch"
                          placeholder="查询道路/建筑/设施..."
                        />
                      <button class="search-btn" @click="handleSearch">查询</button>
                   </div>
               </div>
          </div>
        </div>
      </aside>

      <main class="map-wrapper">
        <div id="map" class="full-map"></div>

        <div class="map-controls">
           <button @click="resetNorth" title="恢复正北">N</button>
           <button @click="zoomIn" title="放大">+</button>
           <button @click="zoomOut" title="缩小">-</button>
           <button @click="locateMe" title="回到沈阳">📍</button>
        </div>

        <div id="mouse-position" class="mouse-coord"></div>

        <div ref="infoTooltip" class="info-tooltip" v-show="tooltipData.show" :style="tooltipStyle">
            <div class="tooltip-header">图层数据信息</div>
               <div class="tooltip-body">
                  <template v-for="(val, key) in tooltipData.properties" :key="key">
                      <div v-if="formatPropertyValue(key, val) !== '-'">
                          <strong>{{ FRIENDLY_KEYS[key] || key }}:</strong> {{ formatPropertyValue(key, val) }}
                      </div>
                  </template>
               </div>
            </div>
      </main>
      <div class="result-panel" v-show="showResultPanel">
      <div class="panel-header">
        <div class="header-left">
          <span class="tab">查询结果</span>
        </div>
        <div class="header-right">
          <span class="total-count">共 {{ searchResults.length }} 条结果</span>
          <button class="close-btn" @click="showResultPanel = false">✖</button>
          </div>
        </div>
        <div class="panel-content">
          <table class="data-table">
            <thead>
              <tr>
                <th>序号</th>
                <th>要素名称</th>
                <th>来源图层</th>
                <th>占地面积</th>
                <th>核心指标</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in paginatedResults" :key="index"
                  :class="{ active: activeRow === index }"
                  @click="locateFeature(item, index)">
                <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                <td>{{ item.name || '未命名要素' }}</td>
                <td>{{ getLayerLabel(item.source_table) }}</td>
                <td>{{ item.calc_area > 0 ? (item.calc_area / 1000000).toFixed(3) + ' km²' : '-' }}</td>

                <td>{{ getDynamicMetric(item) }}</td>

                <td>
                  <button class="locate-btn" @click.stop="locateFeature(item, index)">📍 定位</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
            <div class="panel-content">
                  <table class="data-table"></table></div>
                  <div class="panel-pagination" v-if="totalPages > 1">
                  <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
                  <button :disabled="currentPage === 1" @click="currentPage--">上一页</button>
                  <button :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
             </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import axios from 'axios'
import XYZ from 'ol/source/XYZ'
import 'ol/ol.css'
import { onMounted, ref, reactive, computed } from 'vue'
import { Map, View, Feature } from 'ol'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import GeoJSON from 'ol/format/GeoJSON'
import { Stroke, Style, Fill, Circle as CircleStyle } from 'ol/style'
import { Point, Circle as GeomCircle, Polygon, LineString } from 'ol/geom'
import { toLonLat, fromLonLat } from 'ol/proj'
import OLCesium from 'ol-cesium'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import { Draw } from 'ol/interaction'
import { getLength, getArea } from 'ol/sphere'
import { defaults as defaultControls,ScaleLine, MousePosition, ZoomSlider } from 'ol/control';
import { createStringXY } from 'ol/coordinate';
import { useRouter } from 'vue-router';
import { planRoute } from '@/api/route'
import { fetchAccessiblePois } from '@/api/accessibility'
import { fetchPresetRoute } from '@/api/routing'
import { layers as configLayers } from '@/config/layers'
import { loadLayer } from '@/api/layerApi'
import { FRIENDLY_KEYS, formatPropertyValue, getLayerLabel, safeParseProps, getDynamicMetric } from '@/utils/gisUtils';
import '@/assets/styles/mapView.css';

window.Cesium = Cesium
window.CESIUM_BASE_URL = '/Cesium/';

// --- UI 状态变量 ---
const searchQuery = ref('');
const openData = ref(false);
const openFunc = ref(false);
const open3D = ref(false);
const openTools = ref(false);
const openSearch = ref(true);
// 动态计算图层列表
const layerList = reactive(configLayers.map(l => ({ ...l, visible: l.active || false })));
const dataLayers = computed(() => layerList.filter(l => l.type === 'business' || l.type === 'display'));
const functionLayers = computed(() => layerList.filter(l => l.type === 'function'));

// --- 地图核心变量 ---
let map
let start = null
let end = null
let ol3d = null
let buildDataSource = null
let currentCesiumTime = Cesium.JulianDate.now()
let floodDataSource = new Cesium.CustomDataSource('flood')
let drawInteraction;
let measureHandler = null
let measure3DDataSource = new Cesium.CustomDataSource('measure3D')
let isMeasuring3D = false

// 用于存储 2D 高亮的状态
let highlightFeature = null;
// 用于存储 3D 高亮的状态
let lastPickedEntity = null;
let lastOriginalMaterial = null;
const router = useRouter();
// 获取登录时存入的用户名，如果没拿到则显示 'Admin'
const currentUsername = ref(localStorage.getItem('username') || 'Admin');
// 退出登录函数
const handleLogout = () => {
  if (confirm('您确定要退出系统吗？')) {
    localStorage.removeItem('username'); // 清除存储的用户名
    router.push('/login'); // 返回登录页面
  }
};
const showResultPanel = ref(false);
const searchResults = ref([]);
const currentPage = ref(1);
const pageSize = ref(5); // 每页显示 5 条
const activeRow = ref(null);

const totalPages = computed(() => Math.ceil(searchResults.value.length / pageSize.value));
const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return searchResults.value.slice(start, start + pageSize.value);
});

const infoTooltip = ref(null)
const tooltipData = reactive({
  show: false,
  properties: {},
  x: 0,
  y: 0
})

const tooltipStyle = computed(() => ({
  left: `${tooltipData.x + 15}px`,
  top: `${tooltipData.y + 15}px`
}))

const is3D = ref(false)
const show3DBuildings = ref(false)
const TDT_TK ='24f06bfc85c85fc8114b3b65901416d7'

const functionState = ref({
  accessibility: false,
  'knn-poi': false,
  routing: false,
  'route-accessibility': false,
  'view-3d': false
})
const businessLayers = {}
const highlightSource = new VectorSource();
const highlightLayer = new VectorLayer({
  source: highlightSource,
  zIndex: 9999,
  style: new Style({
    stroke: new Stroke({ color: '#ffcc00', width: 5 }),
    fill: new Fill({ color: 'rgba(255, 204, 0, 0.4)' }),
    image: new CircleStyle({
      radius: 8,
      fill: new Fill({ color: '#ffcc00' }),
      stroke: new Stroke({ color: '#ffffff', width: 2 })
    })
  })
});

const rangeStyle = new Style({
  stroke: new Stroke({ color: 'rgba(0, 153, 255, 1)', width: 2 }),
  fill: new Fill({ color: 'rgba(0, 153, 255, 0.1)' })
})

const pointStyle = new Style({
  image: new CircleStyle({
    radius: 6,
    fill: new Fill({ color: 'red' }),
    stroke: new Stroke({ color: 'white', width: 2 })
  })
})

const routeLayer = new VectorLayer({
  source: new VectorSource(),
  style: new Style({ stroke: new Stroke({ color: 'red', width: 8 }) })
})

const poiLayer = new VectorLayer({ source: new VectorSource() })

const measureSource = new VectorSource();
const measureLayer = new VectorLayer({
  source: measureSource,
  style: new Style({
    fill: new Fill({ color: 'rgba(255, 255, 255, 0.2)' }),
    stroke: new Stroke({ color: '#ffcc33', width: 2 }),
    image: new CircleStyle({ radius: 7, fill: new Fill({ color: '#ffcc33' }) })
  })
});

onMounted(async () => {
  map = new Map({
      target: 'map',
      layers: [
        new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=vec_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }) }),
        new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=cva_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }) }),
        routeLayer,
        poiLayer,
        measureLayer,
        highlightLayer
      ],
      controls: defaultControls({
               zoom: false,       // 显式关闭默认的放大缩小按钮 (以及滑块)
               rotate: false,     // 显式关闭默认的旋转指北针
               attribution: false // 显式关闭默认的版权信息
            }).extend([
                new ScaleLine({
                 // className: 'custom-scale-line',
                  units: 'metric',
                  bar: false,
                  text: true,
                }),
                new MousePosition({
                  coordinateFormat: createStringXY(4),
                  projection: 'EPSG:4326',
                  className: 'custom-mouse-position',
                  target: document.getElementById('mouse-position'),
                })
            ]),
            view: new View({
              center: [13741313, 5130280],
              zoom: 11
            })
        })

  map.on('click', async (evt) => {
    const coord = evt.coordinate
    const [lon, lat] = toLonLat(coord)

    // ===== ① 可达性分析 =====
    if (functionState.value.accessibility) {
      const res = await fetchAccessiblePois(lon, lat, 2000)
      const source = poiLayer.getSource()
      source.clear()

      const circleFeat = new Feature({ geometry: new GeomCircle(coord, 2000) })
      circleFeat.setStyle(rangeStyle)

      const centerFeat = new Feature({ geometry: new Point(coord) })
      centerFeat.setStyle(pointStyle)
      centerFeat.set('名称', '查询中心点');
      centerFeat.set('isAnalysis', true);
      const poiFeats = new GeoJSON().readFeatures(res.data, {
        dataProjection: 'EPSG:4326',
        featureProjection: 'EPSG:3857'
      })
      poiFeats.forEach(f => f.set('isAnalysis', true));
      source.addFeatures([circleFeat, centerFeat, ...poiFeats])
      return
    }

      // ===== ② 最近邻 POI =====
      if (functionState.value['knn-poi']) {
      const res = await fetch(
        `/api/analysis/nearest-poi?lon=${lon}&lat=${lat}`
      )
      const geojson = await res.json()
      const source = poiLayer.getSource()
      source.clear()
      const centerFeat = new Feature({ geometry: new Point(coord) })
      centerFeat.set('名称', '查询中心点');
      centerFeat.set('isAnalysis', true);
      centerFeat.setStyle(pointStyle)
      const feats = new GeoJSON().readFeatures(geojson, {
        featureProjection: 'EPSG:3857'
      })
      feats.forEach(f => f.set('isAnalysis', true));
      source.addFeatures([centerFeat, ...feats])
      return
    }
  })

  ol3d = new OLCesium({
      map: map,
      time: () => currentCesiumTime
  })

  // 获取 Cesium 场景对象进行高级配置
  const scene = ol3d.getCesiumScene()
  scene.globe.depthTestAgainstTerrain = true
  Cesium.Ion.defaultAccessToken ='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJmNzQyYzQ1OS1lNzZlLTQ3ZjEtYWZiYS1jMDNmZDkyMmJlZWUiLCJpZCI6MzYyNzcxLCJpYXQiOjE3NjM4MDMxMzd9.BtxXup3tsFUchLvrWqWy752dP8lwLRyfp0sVy-t7NPw';

  try {
     scene.terrainProvider = await Cesium.createWorldTerrainAsync({
       requestVertexNormals: true,
       requestWaterMask: true
     })
     console.log("地形加载成功")
  } catch (err) {
     console.error("地形错误:", err)
     scene.terrainProvider = new Cesium.EllipsoidTerrainProvider()
  }

  // ===== 3D 悬停交互 (Cesium) =====
  const handler = new Cesium.ScreenSpaceEventHandler(scene.canvas);

  handler.setInputAction((movement) => {
    if (!is3D.value) return;

    if (lastPickedEntity) {
      lastPickedEntity.polygon.material = lastOriginalMaterial;
      lastPickedEntity = null;
    }

    const pickedObject = scene.pick(movement.endPosition);

    if (Cesium.defined(pickedObject) && pickedObject.id) {
      const entity = pickedObject.id;

      if (entity.properties) {
        lastPickedEntity = entity;
        lastOriginalMaterial = entity.polygon.material;
        entity.polygon.material = Cesium.Color.YELLOW.withAlpha(0.7);

        tooltipData.show = true;
        tooltipData.x = movement.endPosition.x;
        tooltipData.y = movement.endPosition.y;

        const props = {};
        entity.properties.propertyNames.forEach(name => {
          props[name] = entity.properties[name].getValue();
        });
        tooltipData.properties = props;
        document.body.style.cursor = 'pointer';
      }
    } else {
      tooltipData.show = false;
      document.body.style.cursor = 'default';
    }
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

  // ===== 2D 悬停交互 (OpenLayers) =====
  map.on('pointermove', (evt) => {
    if (is3D.value) return;

    if (highlightFeature) {
    if (!highlightFeature.get('isAnalysis')) {
      highlightFeature.setStyle(undefined);
    }
    highlightFeature = null;
  }

    const feature = map.forEachFeatureAtPixel(evt.pixel, (f) => f);

    if (feature) {
      highlightFeature = feature;
      if (!feature.get('isAnalysis')) {
      feature.setStyle(new Style({
        stroke: new Stroke({ color: '#00ffff', width: 3 }),
        fill: new Fill({ color: 'rgba(0, 255, 255, 0.3)' })
      }));
    }

      tooltipData.show = true;
      tooltipData.x = evt.pixel[0];
      tooltipData.y = evt.pixel[1];
      const props = { ...feature.getProperties() };
      delete props.geometry;
      delete props.isAnalysis; // 隐藏我们的内部标记
      tooltipData.properties = props;

      map.getTargetElement().style.cursor = 'pointer';
    } else {
      tooltipData.show = false;
      map.getTargetElement().style.cursor = '';
    }
  });
})
const handleSearch = async () => {
  if (!searchQuery.value.trim()) return;
  try {
    const response = await axios.get(`http://localhost:8082/api/layer/search`, {
      params: { keyword: searchQuery.value }
    });

    // 关键修复点：声明 results 变量并获取后端数据
    const results = response.data;

    // 检查 results 是否存在并进行排序
    if (results && results.length > 0) {
      results.sort((a, b) => {
        // 使用你之前定义的 safeParseProps 方法
        const propsA = safeParseProps(a.properties);
        const propsB = safeParseProps(b.properties);

        // 判断 A 和 B 是否包含核心指标
        const hasDataA = (propsA.population || propsA.students || propsA.capacity || propsA.beds) ? 1 : 0;
        const hasDataB = (propsB.population || propsB.students || propsB.capacity || propsB.beds) ? 1 : 0;

        // 降序排列：有数据的排在前面
        return hasDataB - hasDataA;
      });
    }

    // 将排序后的结果赋值给响应式变量
    searchResults.value = results;

    if (searchResults.value && searchResults.value.length > 0) {
      showResultPanel.value = true;
      currentPage.value = 1;
      activeRow.value = null;
      highlightSource.clear();
    } else {
      alert('未找到相关要素');
      showResultPanel.value = false;
      highlightSource.clear();
    }
  } catch (error) {
    console.error('搜索异常:', error);
  }
};
const locateFeature = (record, index) => {
  activeRow.value = index; // 高亮当前点击的表格行

  if (!record.geometry) {
    alert(`找到了 [${record.name}]，但它没有空间位置数据！`);
    return;
  }

  // 解析图形系数据
  const geomObj = typeof record.geometry === 'string' ? JSON.parse(record.geometry) : record.geometry;
  const coords = geomObj.coordinates;
  // 兼容点、线、面的中心点提取
  let centerLonLat = Array.isArray(coords[0]) ? (Array.isArray(coords[0][0]) ? coords[0][0][0] : coords[0][0]) : coords;

  // 绘制高亮
  highlightSource.clear();
  const searchFeature = new Feature({
    geometry: new GeoJSON().readGeometry(geomObj, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    })
  });

  // 将属性绑上去以便后续悬浮窗查看
  searchFeature.set('名称', record.name || '搜索结果');
  searchFeature.set('来源数据表', record.source_table);
  const realProps = safeParseProps(record.properties);
  for (let key in realProps) {
     searchFeature.set(key, realProps[key]);
  }

  highlightSource.addFeature(searchFeature);

  // 视角飞往该地
  if (!is3D.value) {
    map.getView().animate({ center: fromLonLat(centerLonLat), zoom: 17, duration: 1000 });
  } else {
    const scene = ol3d.getCesiumScene();
    scene.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(centerLonLat[0], centerLonLat[1], 800),
      duration: 1.5
    });
  }
};
// --- 图层与功能管理 ---
const handleLayerChange = (layer) => {
  if (layer.visible) {
    loadLayer(layer).then(res => addLayer(layer.id, res.data)).catch(e => {
        console.error(e);
        layer.visible = false;
    });
  } else {
    removeLayer(layer.id);
  }
};

const handleFunctionClick = (layer) => {
  layer.visible = !functionState.value[layer.id];
  handleFunction(layer);
};

const toggle3DMode = () => {
  handleFunction({ id: 'view-3d', visible: !is3D.value });
};

function addLayer(id, data) {
  const source = new VectorSource({
    features: new GeoJSON().readFeatures(data, {
    dataProjection: 'EPSG:4326',
    featureProjection: 'EPSG:3857' })
  })

  if (id === 'build') {
      const layer = new VectorLayer({
        source,
        style: new Style({
          stroke: new Stroke({ color: '#999', width: 1 }),
          fill: new Fill({ color: 'rgba(200,200,200,0.8)' })
        })
      })

      layer.rawData = typeof data === 'string' ? JSON.parse(data) : data;
      businessLayers[id] = layer
      map.addLayer(layer)

      return
    }

  const layer = new VectorLayer({
      source,
      style: new Style({
        stroke: new Stroke({ color: id === 'build' ? '#999' : 'blue', width: 1 }),
        fill: new Fill({ color: id === 'build' ? 'rgba(200, 200, 200, 0.8)' : 'rgba(0, 0, 255, 0.1)' }),
        image: new CircleStyle({
          radius: 5,
          fill: new Fill({ color: 'blue' }),
          stroke: new Stroke({ color: 'white', width: 1 })
        })
      })
    });
    businessLayers[id] = layer;
    map.addLayer(layer);
}

function removeLayer(id) {
  if (businessLayers[id]) {
    map.removeLayer(businessLayers[id])
    delete businessLayers[id]
  }

  if (id === 'build' && buildDataSource) {
    const scene = ol3d.getCesiumScene()
    if (scene && scene._viewer) {
      scene._viewer.dataSources.remove(buildDataSource)
    }
    buildDataSource = null
  }
}

function handleFunction(layer) {
  Object.keys(functionState.value).forEach(k => {
    functionState.value[k] = false
  })
  functionState.value[layer.id] = layer.visible
  poiLayer.getSource().clear()
  routeLayer.getSource().clear()

  if (layer.id === 'routing' && layer.visible) {
    showAutoRoute();
  }

  if (layer.id === 'view-3d') {
    is3D.value = layer.visible
    ol3d.setEnabled(is3D.value)
    if (is3D.value) {
        waitForViewer(() => {
           console.log("进入 3D 模式，准备加载和平区 3D 建筑...");
           preload3DBuildings();
        });
    }
  }
}

async function showAutoRoute() {
  try {
    const res = await fetchPresetRoute();
    const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;

    const geojsonFormat = new GeoJSON();
    const features = geojsonFormat.readFeatures(data, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    });

    routeLayer.getSource().clear();
    routeLayer.getSource().addFeatures(features);

    if (features.length > 0) {
      const coords = features[0].getGeometry().getCoordinates();
      map.getView().fit(routeLayer.getSource().getExtent(), {
        padding: [100, 100, 100, 100],
        duration: 1000
      });
    }
  } catch (error) {
    console.error("加载预设路径失败:", error);
  }
}

function drawRoute() {
  const startLL = toLonLat(start);
  const endLL = toLonLat(end);

  planRoute(startLL, endLL).then(res => {
    routeLayer.getSource().clear();
    const geojsonFormat = new GeoJSON();
    const features = geojsonFormat.readFeatures(res.data, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    });
    if (features.length > 0) {
      map.getView().fit(routeLayer.getSource().getExtent(), {
        padding: [50, 50, 50, 50],
        duration: 1000
      });
    }
  })
}

function flyToShenyang() {
  if (ol3d && is3D.value) {
    ol3d.getCesiumScene().camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(123.43, 41.80, 15000),
      orientation: { pitch: Cesium.Math.toRadians(-90) }
    })
  }
}

let terrainActive = true
async function toggleTerrain() {
  if (!ol3d) return
  const scene = ol3d.getCesiumScene()
  terrainActive = !terrainActive
  try {
    if (terrainActive) {
      scene.terrainProvider = await Cesium.createWorldTerrainAsync({
        requestVertexNormals: true,
        requestWaterMask: true
      })
    } else {
      scene.terrainProvider = new Cesium.EllipsoidTerrainProvider()
    }
  } catch (e) {
    console.error("地形切换失败:", e)
  }
}

async function preload3DBuildings() {
  if (buildDataSource) return;
  const scene = ol3d.getCesiumScene();
  if (!scene) return;

  const startTime = performance.now();
  console.log("正在请求和平区 3D 建筑数据...");

  try {
    // 构造一个虚拟图层对象，请求后端的 build_heping
    const res = await loadLayer({
     id: 'build_heping',
         name: '和平区',
         type: 'display',
         table: 'build_heping',
         isBuilding: true
     });
    const geojsonData = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;

    Cesium.GeoJsonDataSource.load(geojsonData).then(dataSource => {
      ol3d.getDataSources().add(dataSource);
      buildDataSource = dataSource;
      buildDataSource.show = show3DBuildings.value; // 与面板按钮状态同步

      dataSource.entities.suspendEvents();
      dataSource.entities.values.forEach(entity => {
        if (entity.polygon) {
          const baseHeight = 45;
          const buildingHeight = entity.properties.height ? parseFloat(entity.properties.height.getValue()) : 20;
          entity.polygon.height = baseHeight;
          entity.polygon.extrudedHeight = baseHeight + buildingHeight;
          entity.polygon.material = Cesium.Color.WHITE.withAlpha(0.8);
        }
      });
      dataSource.entities.resumeEvents();

      const endTime = performance.now();
      console.log(`✅ 和平区 3D 建筑加载完毕！耗时: ${((endTime - startTime) / 1000).toFixed(2)} 秒`);
    });
  } catch (error) {
    console.error("加载和平区 3D 建筑失败: ", error);
  }
}

function toggle3DBuildings() {
  show3DBuildings.value = !show3DBuildings.value;
  if (buildDataSource) {
    buildDataSource.show = show3DBuildings.value;
  } else {
    alert("3D建筑(和平区)数据正在后台拼命加载中，请稍等几秒后再试~");
    show3DBuildings.value = false;
    // 如果由于某种原因还没开始加载，手动触发一次
    preload3DBuildings();
  }
}

function waitForViewer(callback) {
  const check = () => {
    const scene = ol3d ? ol3d.getCesiumScene() : null;
    if (scene) {
      callback();
    } else {
      requestAnimationFrame(check);
    }
  };
  check();
}

const isShadowActive = ref(false)
const shadowTime = ref(12)

function toggleShadows() {
  const scene = ol3d.getCesiumScene()
  if (!scene) return
  isShadowActive.value = !isShadowActive.value
  scene.globe.enableLighting = isShadowActive.value
  scene.shadowMap.enabled = isShadowActive.value
  if (isShadowActive.value) {
    updateShadowTime()
  } else {
    currentCesiumTime = Cesium.JulianDate.now()
  }
}

function updateShadowTime() {
  if (!isShadowActive.value) return
  const bjHour = parseFloat(shadowTime.value)
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  const timeDifferenceMs = (bjHour + 2 ) * 3600000 - (14.8 * 60000)
  const finalUtcMs = date.getTime() + timeDifferenceMs
  currentCesiumTime = Cesium.JulianDate.fromDate(new Date(finalUtcMs))

  if (ol3d && ol3d.getCesiumScene()) {
    const scene = ol3d.getCesiumScene()
    scene.globe.enableLighting = true
    scene.shadowMap.darkness = 0.5
  }
}

const isFlooding = ref(false)
const waterLevel = ref(40)
let floodEntity = null
let floodTimer = null

function startFlood() {
  isFlooding.value = !isFlooding.value

  if (isFlooding.value) {
    if (!ol3d.getDataSources().contains(floodDataSource)) {
      ol3d.getDataSources().add(floodDataSource)
    }
    const scene = ol3d.getCesiumScene();
    scene.globe.depthTestAgainstTerrain = true;

    if (!floodEntity) {
      floodEntity = floodDataSource.entities.add({
        polygon: {
          hierarchy: Cesium.Cartesian3.fromDegreesArray([
            123.2404,41.8736,
            123.2269,41.6550,
            123.6232,41.6449,
            123.6265,41.8741
          ]),
          height: 40,
          extrudedHeight: new Cesium.CallbackProperty(() => waterLevel.value, false),
          material: Cesium.Color.fromCssColorString('#0288D1').withAlpha(0.6),
          outline: false
        }
      })
    }

    waterLevel.value = 40;
    if (floodTimer) clearInterval(floodTimer);

    floodTimer = setInterval(() => {
      if (waterLevel.value >= 200) {
        clearInterval(floodTimer)
        return
      }
      waterLevel.value += 0.5;
    }, 200)

  } else {
    if (floodTimer) {
      clearInterval(floodTimer)
      floodTimer = null
    }
  }
}

function startMeasure(type) {
  if (!is3D.value) {
    measureSource.clear();
    if (drawInteraction) map.removeInteraction(drawInteraction);
    drawInteraction = new Draw({ source: measureSource, type: type });
    drawInteraction.on('drawend', (evt) => {
      const geom = evt.feature.getGeometry();
      let output = type === 'LineString'
        ? (getLength(geom) / 1000).toFixed(2) + ' km'
        : (getArea(geom) / 1000000).toFixed(2) + ' km²';
      setTimeout(() => {
          if (confirm('测量结果: ' + output + '，是否清除？')) {
            measureSource.clear();
          }
        }, 100);
      map.removeInteraction(drawInteraction);
      drawInteraction = null;
    });
    map.addInteraction(drawInteraction);
    return;
  }

  const scene = ol3d.getCesiumScene();
  if (!scene) return;

  if (!ol3d.getDataSources().contains(measure3DDataSource)) {
    ol3d.getDataSources().add(measure3DDataSource);
  }

  measure3DDataSource.entities.removeAll();
  if (measureHandler) measureHandler.destroy();
  isMeasuring3D = true;

  measureHandler = new Cesium.ScreenSpaceEventHandler(scene.canvas);
  let positions = [];
  let polyObj = null;

  measureHandler.setInputAction((movement) => {
    const ray = scene.camera.getPickRay(movement.position);
    const cartesian = scene.globe.pick(ray, scene);

    if (cartesian) {
      if (positions.length === 0) {
        positions.push(cartesian.clone());
        measure3DDataSource.entities.add({
          position: cartesian,
          point: { color: Cesium.Color.RED, pixelSize: 6, outlineColor: Cesium.Color.WHITE, outlineWidth: 2 }
        });
      }
      positions.push(cartesian.clone());

      if (!polyObj) {
        if (type === 'LineString') {
          polyObj = measure3DDataSource.entities.add({
            polyline: {
              positions: new Cesium.CallbackProperty(() => positions, false),
              width: 4,
              material: Cesium.Color.YELLOW,
              clampToGround: true
            }
          });
        } else {
          polyObj = measure3DDataSource.entities.add({
            polygon: {
              hierarchy: new Cesium.CallbackProperty(() => new Cesium.PolygonHierarchy(positions), false),
              material: Cesium.Color.YELLOW.withAlpha(0.4),
              classificationType: Cesium.ClassificationType.TERRAIN
            },
            polyline: {
              positions: new Cesium.CallbackProperty(() => [...positions, positions[0]], false),
              width: 3,
              material: Cesium.Color.YELLOW,
              clampToGround: true
            }
          });
        }
      }
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

  measureHandler.setInputAction((movement) => {
    if (positions.length > 0) {
      const ray = scene.camera.getPickRay(movement.endPosition);
      const cartesian = scene.globe.pick(ray, scene);
      if (cartesian) {
        positions[positions.length - 1] = cartesian.clone();
      }
    }
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

  measureHandler.setInputAction(() => {
    isMeasuring3D = false;
    measureHandler.destroy();
    measureHandler = null;

    positions.pop();
    if (positions.length < 2) return;

    if (type === 'LineString') {
      let distance = 0;
      for (let i = 0; i < positions.length - 1; i++) {
        distance += Cesium.Cartesian3.distance(positions[i], positions[i+1]);
      }
      alert(`3D 测距结果: ${distance.toFixed(2)} m`);
    } else {
      const coords = positions.map(p => {
        const carto = Cesium.Cartographic.fromCartesian(p);
        return [Cesium.Math.toDegrees(carto.longitude), Cesium.Math.toDegrees(carto.latitude)];
      });
      coords.push(coords[0]);

      const polygonGeom = new Polygon([coords]);
      const area = getArea(polygonGeom, { projection: 'EPSG:4326' });
      alert(`3D 测面积结果: ${(area / 1000000).toFixed(2)} Km²`);
    }
  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

  alert(`已开启 3D 测量\n操作说明：\n1. 鼠标【左键】点击地图添加点\n2. 鼠标【右键】点击结束并获取结果`);
}

function resetNorth() { map.getView().animate({ rotation: 0, duration: 500 }); }
function zoomIn() { map.getView().animate({ zoom: map.getView().getZoom() + 1, duration: 250 }); }
function zoomOut() { map.getView().animate({ zoom: map.getView().getZoom() - 1, duration: 250 }); }
function locateMe() { map.getView().animate({ center: [13741313, 5130280], zoom: 12, duration: 1000 }); }
</script>

<style scoped>
</style>