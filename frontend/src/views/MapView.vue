<template>
  <div id="map"></div>
  <LayerPanel
    @add="addLayer"
    @remove="removeLayer"
    @toggle-function="handleFunction"
  />

  <div v-if="is3D" class="td-tools">
      <button @click="flyToShenyang">飞往沈阳</button>
      <button @click="toggleTerrain">切换地形</button>
      <button @click="toggle3DBuildings">
            {{ show3DBuildings ? '隐藏3D建筑' : '显示3D建筑' }}
      </button>

      <div class="tool-group">
        <label>空间量测</label>
        <div style="display: flex; gap: 5px;">
          <button @click="startMeasure('LineString')" style="flex: 1;">测距</button>
          <button @click="startMeasure('Polygon')" style="flex: 1;">测面积</button>
        </div>
      </div>

      <div class="tool-group">
        <label>日照时间模拟 ({{ shadowTime }}:00)</label>
        <input type="range" min="0" max="24" step="1" v-model="shadowTime" @input="updateShadowTime">
        <button @click="toggleShadows">{{ isShadowActive ? '关闭日照' : '开启日照' }}</button>
      </div>
      <div class="tool-group">
        <label>淹没分析 (当前水位: {{ waterLevel-60 }}m)</label>
        <button @click="startFlood">{{ isFlooding ? '停止淹没' : '开始淹没' }}</button>
      </div>
  </div>

  <div class="map-controls">
     <button @click="resetNorth" title="恢复正北">N</button>
     <button @click="zoomIn" title="放大">+</button>
     <button @click="zoomOut" title="缩小">-</button>
     <button @click="locateMe" title="回到沈阳">📍</button>
  </div>

  <div id="mouse-position" class="mouse-coord"></div>

  <div ref="infoTooltip" class="info-tooltip" v-show="tooltipData.show" :style="tooltipStyle">
    <div class="tooltip-header">属性信息</div>
    <div class="tooltip-body">
      <div v-for="(val, key) in tooltipData.properties" :key="key">
        <strong>{{ key }}:</strong> {{ val }}
      </div>
    </div>
  </div>
</template>

<script setup>
import XYZ from 'ol/source/XYZ'
import 'ol/ol.css'
import { onMounted, ref, reactive, computed } from 'vue'
import { Map, View, Feature } from 'ol'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import OSM from 'ol/source/OSM'
import VectorSource from 'ol/source/Vector'
import GeoJSON from 'ol/format/GeoJSON'
import { Stroke, Style, Fill, Circle as CircleStyle } from 'ol/style'
import { Point, Circle as GeomCircle, Polygon, LineString } from 'ol/geom'
import { toLonLat } from 'ol/proj'
import OLCesium from 'ol-cesium'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
window.Cesium = Cesium
window.CESIUM_BASE_URL = '/Cesium/';
import { Draw } from 'ol/interaction'
import { getLength, getArea } from 'ol/sphere'
import { ScaleLine, MousePosition, ZoomSlider } from 'ol/control';
import { createStringXY } from 'ol/coordinate';

import { planRoute } from '@/api/route'
import { fetchAccessiblePois } from '@/api/accessibility'
import LayerPanel from '@/components/LayerPanel.vue'
import { fetchPresetRoute } from '@/api/routing'


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
  'route-accessibility': false
})

const businessLayers = {}


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
  style: new Style({
    stroke: new Stroke({
      color: 'red',
      width: 8
    })
  })
})

const poiLayer = new VectorLayer({
  source: new VectorSource()
})

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
        poiLayer
      ],
      controls: [
          new ScaleLine({
            className: 'custom-scale-line',
             units: 'metric',
             bar: true,        // 显示刻度条
             steps: 4,         // 刻度数量
             text: true,       // 显示文字
             minWidth: 140     // 最小宽度
          }),
          new ZoomSlider(),
          new MousePosition({
            coordinateFormat: createStringXY(4),
            projection: 'EPSG:4326',
            className: 'custom-mouse-position',
            target: document.getElementById('mouse-position'),
          })
        ],
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

      const circleFeat = new Feature({
        geometry: new GeomCircle(coord, 2000)
      })
      circleFeat.setStyle(rangeStyle)

      const centerFeat = new Feature({
        geometry: new Point(coord)
      })
      centerFeat.setStyle(pointStyle)

      const poiFeats = new GeoJSON().readFeatures(res.data, {
        dataProjection: 'EPSG:4326',
        featureProjection: 'EPSG:3857'
      })

      source.addFeatures([circleFeat, centerFeat, ...poiFeats])
      return
    }

    // ===== ② 最近邻 POI =====
    if (functionState.value['knn-poi']) {
      const res = await fetch(
        `http://localhost:8082/api/analysis/nearest-poi?lon=${lon}&lat=${lat}`
      )
      const geojson = await res.json()

      poiLayer.getSource().clear()
      const feats = new GeoJSON().readFeatures(geojson, {
        featureProjection: 'EPSG:3857'
      })
      poiLayer.getSource().addFeatures(feats)
      return
    }

  })
  ol3d = new OLCesium({
      map: map,
      time: () => currentCesiumTime
       })
      // 获取 Cesium 场景对象进行高级配置
      const scene = ol3d.getCesiumScene()
      scene.globe.depthTestAgainstTerrain = true // 开启深度检测，防止要素漂浮
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
          console.log(entity.properties?.getValue())
        }
  // ===== 3D 悬停交互 (Cesium) =====
    const handler = new Cesium.ScreenSpaceEventHandler(scene.canvas);

    handler.setInputAction((movement) => {
      if (!is3D.value) return;

      // 1. 恢复上一个模型的状态
      if (lastPickedEntity) {
        lastPickedEntity.polygon.material = lastOriginalMaterial;
        lastPickedEntity = null;
      }

      // 2. 拾取当前模型
      const pickedObject = scene.pick(movement.endPosition);

      if (Cesium.defined(pickedObject) && pickedObject.id) {
        const entity = pickedObject.id;

        // 只有带属性的实体（如建筑）才触发
        if (entity.properties) {
          lastPickedEntity = entity;
          lastOriginalMaterial = entity.polygon.material;

          // 高亮：改为亮黄色
          entity.polygon.material = Cesium.Color.YELLOW.withAlpha(0.7);

          // 显示 Tooltip
          tooltipData.show = true;
          tooltipData.x = movement.endPosition.x;
          tooltipData.y = movement.endPosition.y;

          // 解析属性
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
      if (is3D.value) return; // 3D 模式下跳过 2D 逻辑

      if (highlightFeature) {
        highlightFeature.setStyle(undefined); // 恢复原有样式
        highlightFeature = null;
      }

      const feature = map.forEachFeatureAtPixel(evt.pixel, (f) => f);

      if (feature) {
        highlightFeature = feature;
        // 设置高亮样式
        feature.setStyle(new Style({
          stroke: new Stroke({ color: '#00ffff', width: 3 }),
          fill: new Fill({ color: 'rgba(0, 255, 255, 0.3)' })
        }));

        tooltipData.show = true;
        tooltipData.x = evt.pixel[0];
        tooltipData.y = evt.pixel[1];
        tooltipData.properties = feature.getProperties();
        // 移除 geometry 属性，不显示在 UI 上
        delete tooltipData.properties.geometry;

        map.getTargetElement().style.cursor = 'pointer';
      } else {
        tooltipData.show = false;
        map.getTargetElement().style.cursor = '';
      }
    });
})

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

      // 【优化核心 1】：把原始的 GeoJSON 数据挂载到图层对象上存起来！
      layer.rawData = typeof data === 'string' ? JSON.parse(data) : data;

      businessLayers[id] = layer
      map.addLayer(layer)

      if (is3D.value) {
          waitForViewer(() => {
            // 【优化核心 2】：不再传 source，直接传最原始的数据！
            preload3DBuildings(layer.rawData)
          })
        }
      return
    }

  const layer = new VectorLayer({
      source,
      style: new Style({
        stroke: new Stroke({ color: id === 'build' ? '#999' : 'blue', width: 1 }),
        fill: new Fill({ color: id === 'build' ? 'rgba(200, 200, 200, 0.8)' : 'rgba(0, 0, 255, 0.1)' }),
        image: new CircleStyle({ // 针对 POI 点图层的显示
          radius: 5,
          fill: new Fill({ color: 'blue' }),
          stroke: new Stroke({ color: 'white', width: 1 })
        })
      })
    });
    // 1. 存入字典以便后续删除
    businessLayers[id] = layer;
    // 2. 真正添加到地图上显示
    map.addLayer(layer);
  console.log("当前source要素数量:", routeLayer.getSource().getFeatures().length)

}

function removeLayer(id) {

  // ===== 1. 删除 2D 图层 =====
  if (businessLayers[id]) {
    map.removeLayer(businessLayers[id])
    delete businessLayers[id]
  }

  // ===== 2. 删除 3D 图层（如果存在） =====
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
  //路径规划
    if (layer.id === 'routing' && layer.visible) {
      showAutoRoute();
    }

    if (layer.id === 'view-3d') {

      is3D.value = layer.visible
      ol3d.setEnabled(is3D.value)

      if (layer.id === 'view-3d') {
            is3D.value = layer.visible
            ol3d.setEnabled(is3D.value)

            if (is3D.value) {
                  waitForViewer(() => {
                    const buildLayer = businessLayers['build'];
                    // 【优化核心 3】：如果图层存在，且有我们刚才存的 rawData，直接传入
                    if (buildLayer && buildLayer.rawData) {
                      console.log("检测到已有建筑数据，正在后台预加载3D模型...");
                      preload3DBuildings(buildLayer.rawData);
                    }
                  });
                }
          }
    }
}

async function showAutoRoute() {
  try {
    const res = await fetchPresetRoute();
    // 核心：直接处理 res.data，因为它已经是 GeoJSON 字符串了
    // 如果后端返回的是 String，有时需要 JSON.parse(res.data)
    const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;

    const geojsonFormat = new GeoJSON();
    const features = geojsonFormat.readFeatures(data, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    });

    routeLayer.getSource().clear();
    routeLayer.getSource().addFeatures(features);

    // 此时控制台打印的“路径点总数”应该是这 6 行数据的所有坐标点
    if (features.length > 0) {
      const coords = features[0].getGeometry().getCoordinates();
      console.log("路径点总数:", coords.length);

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
    // 强制刷新渲染
    const geojsonFormat = new GeoJSON();
    const features = geojsonFormat.readFeatures(res.data, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857'
    });
    // 自动缩放到路径范围，方便观察是不是飞到了别处
    if (features.length > 0) {
      map.getView().fit(routeLayer.getSource().getExtent(), {
        padding: [50, 50, 50, 50],
        duration: 1000
      });
    }
    if (features.length > 0) {
          const coords = features[0].getGeometry().getCoordinates();
          console.log("路径点总数:", coords.length);
        }
  })
}
function flyToShenyang() {
  if (ol3d && is3D.value) {
    ol3d.getCesiumScene().camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(123.43, 41.80, 15000), // 坐标与高度
      orientation: {
        pitch: Cesium.Math.toRadians(-90) // 俯视
      }
    })
  }
}

// 切换地形可见性
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
      console.log("地形开启")
    } else {
      scene.terrainProvider = new Cesium.EllipsoidTerrainProvider()
      console.log("地形关闭")
    }
  } catch (e) {
    console.error("地形切换失败:", e)
  }
}
function preload3DBuildings(geojsonData) {
  if (buildDataSource) return;
  const scene = ol3d.getCesiumScene();
  if (!scene) return;

  console.log("🚀 开始极速解析 3D 建筑...");
  const startTime = performance.now();

  // 直接使用原始的、已经是 EPSG:4326 经纬度的 GeoJSON 数据！
  Cesium.GeoJsonDataSource.load(geojsonData).then(dataSource => {
    ol3d.getDataSources().add(dataSource);
    buildDataSource = dataSource;

    buildDataSource.show = false;

    dataSource.entities.suspendEvents();
    dataSource.entities.values.forEach(entity => {
      if (entity.polygon) {
        if (entity.polygon) {
            const baseHeight = 45; // 沈阳地表海拔
            const buildingHeight = entity.properties.height ? parseFloat(entity.properties.height.getValue()) : 20;

            entity.polygon.height = baseHeight;
            entity.polygon.extrudedHeight = baseHeight + buildingHeight; // 建筑顶端 = 地表 + 楼高

            // 颜色改为灰白色，与蓝水区分
            entity.polygon.material = Cesium.Color.WHITE.withAlpha(0.8);
          }
        }
      });
    dataSource.entities.resumeEvents();

    const endTime = performance.now();
    console.log(`✅ 3D建筑加载完毕！耗时: ${((endTime - startTime) / 1000).toFixed(2)} 秒，随时可以秒切！`);
  });
}

// 2. 按钮点击触发函数 (0秒延迟，瞬间切换！)
function toggle3DBuildings() {
  show3DBuildings.value = !show3DBuildings.value;

  if (buildDataSource) {
    // 直接控制数据源的显隐属性，不需要重新渲染
    buildDataSource.show = show3DBuildings.value;
  } else {
    // 如果用户手速太快，在后台还没加载完就点了按钮
    alert("3D建筑数据正在后台拼命加载中，请稍等几秒后再试~");
    show3DBuildings.value = false; // 状态回退
  }
}

function waitForViewer(callback) {
  const check = () => {
    // 只要 ol3d 实例和 scene 准备好了就可以执行
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
 const shadowTime = ref(12) // 默认中午 12 点

 // 开启/关闭日照与阴影
 function toggleShadows() {
   const scene = ol3d.getCesiumScene()
   if (!scene) return

   isShadowActive.value = !isShadowActive.value

   // 直接操作 globe 和 shadowMap
   scene.globe.enableLighting = isShadowActive.value
   scene.shadowMap.enabled = isShadowActive.value

   if (isShadowActive.value) {
     updateShadowTime()
   } else {
     // 关闭时重置时间为当前实际时间
     currentCesiumTime = Cesium.JulianDate.now()
   }
 }

 function updateShadowTime() {
   if (!isShadowActive.value) return
    const bjHour = parseFloat(shadowTime.value)

      // 获取当前日期，并将时间归零到当天的 00:00:00
      const date = new Date()
      date.setHours(0, 0, 0, 0)

      // 核心修正算法：
      // 1. 北京时间 -> UTC 时间 (减去 8 小时)
      // 2. 沈阳(123°43'E)地方太阳时修正：比120°E早约 14.8 分钟。
      // 为了让输入框的12:00正好对应沈阳的正午，UTC时间需要额外减去这14.8分钟的偏差。
      const timeDifferenceMs = (bjHour + 2 ) * 3600000 - (14.8 * 60000)

      // 生成最终的 UTC 时间戳
      const finalUtcMs = date.getTime() + timeDifferenceMs

      // 赋值给 Cesium
      currentCesiumTime = Cesium.JulianDate.fromDate(new Date(finalUtcMs))

      // 确保场景开启了日照和阴影
      if (ol3d && ol3d.getCesiumScene()) {
        const scene = ol3d.getCesiumScene()
        scene.globe.enableLighting = true
        scene.shadowMap.darkness = 0.5 // 调整阴影浓度
      }
}
 const isFlooding = ref(false)
 const waterLevel = ref(0)
 let floodEntity = null
 let floodTimer = null

function startFlood() {
  isFlooding.value = !isFlooding.value

  if (isFlooding.value) {
    // 【修复1】：正确判断并添加数据源到场景中
    if (!ol3d.getDataSources().contains(floodDataSource)) {
      ol3d.getDataSources().add(floodDataSource)
    }

    const scene = ol3d.getCesiumScene();
    // 【修复2】：必须开启深度检测，才能让水面被高地势遮挡，形成淹没效果
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
          // 2. 动态改变拉伸高度（水面高度），形成水体体积
          extrudedHeight: new Cesium.CallbackProperty(() => waterLevel.value, false),
          material: Cesium.Color.fromCssColorString('#0288D1').withAlpha(0.6),
          // 3. 必须关闭轮廓线，否则你的眼睛还是会盯着框看
          outline: false
        }
      })
    }

    waterLevel.value = 40;
    if (floodTimer) clearInterval(floodTimer);

    floodTimer = setInterval(() => {
      if (waterLevel.value >= 120) {
        clearInterval(floodTimer)
        return
      }
      waterLevel.value += 0.5;
    }, 30)

  } else {
    // 停止淹没：仅清除定时器，保留水面高度和视觉状态
    if (floodTimer) {
      clearInterval(floodTimer)
      floodTimer = null
    }
  }
}
 function startMeasure(type) {
   if (!is3D.value) {
       if (drawInteraction) map.removeInteraction(drawInteraction);
       drawInteraction = new Draw({ source: measureSource, type: type });
       drawInteraction.on('drawend', (evt) => {
         const geom = evt.feature.getGeometry();
         let output = type === 'LineString'
           ? (getLength(geom) / 1000).toFixed(2) + ' km'
           : (getArea(geom) / 1000000).toFixed(2) + ' km²';
         alert('2D 测量结果: ' + output);
         map.removeInteraction(drawInteraction);
       });
       map.addInteraction(drawInteraction);
       return;
     }

     const scene = ol3d.getCesiumScene();
     if (!scene) return;

     // 将三维量测数据源加入场景
     if (!ol3d.getDataSources().contains(measure3DDataSource)) {
       ol3d.getDataSources().add(measure3DDataSource);
     }

     // 初始化/清理上一次绘制
     measure3DDataSource.entities.removeAll();
     if (measureHandler) measureHandler.destroy();
     isMeasuring3D = true;

     measureHandler = new Cesium.ScreenSpaceEventHandler(scene.canvas);
     let positions = []; // 存储三维坐标点
     let polyObj = null; // 绘制的图形实体

     // A. 鼠标左键点击：打点
     measureHandler.setInputAction((movement) => {
       // 获取带地形的准确三维坐标
       const ray = scene.camera.getPickRay(movement.position);
       const cartesian = scene.globe.pick(ray, scene);

       if (cartesian) {
         if (positions.length === 0) {
           positions.push(cartesian.clone()); // 起点
           // 画出起点小红球
           measure3DDataSource.entities.add({
             position: cartesian,
             point: { color: Cesium.Color.RED, pixelSize: 6, outlineColor: Cesium.Color.WHITE, outlineWidth: 2 }
           });
         }
         positions.push(cartesian.clone()); // 加入下一个点（作为鼠标移动的跟随点）

         // 初始化线或面实体
         if (!polyObj) {
           if (type === 'LineString') {
             polyObj = measure3DDataSource.entities.add({
               polyline: {
                 positions: new Cesium.CallbackProperty(() => positions, false),
                 width: 4,
                 material: Cesium.Color.YELLOW,
                 clampToGround: true // 贴地线
               }
             });
           } else {
             polyObj = measure3DDataSource.entities.add({
               polygon: {
                 hierarchy: new Cesium.CallbackProperty(() => new Cesium.PolygonHierarchy(positions), false),
                 material: Cesium.Color.YELLOW.withAlpha(0.4),
                 classificationType: Cesium.ClassificationType.TERRAIN // 贴地面
               },
               polyline: {
                 positions: new Cesium.CallbackProperty(() => [...positions, positions[0]], false), // 闭合边界
                 width: 3,
                 material: Cesium.Color.YELLOW,
                 clampToGround: true
               }
             });
           }
         }
       }
     }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

     // B. 鼠标移动：实现“橡皮筋”跟随效果
     measureHandler.setInputAction((movement) => {
       if (positions.length > 0) {
         const ray = scene.camera.getPickRay(movement.endPosition);
         const cartesian = scene.globe.pick(ray, scene);
         if (cartesian) {
           // 更新数组里的最后一个点为当前鼠标位置
           positions[positions.length - 1] = cartesian.clone();
         }
       }
     }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

     // C. 鼠标右键：结束绘制并计算结果
     measureHandler.setInputAction(() => {
       isMeasuring3D = false;
       measureHandler.destroy();
       measureHandler = null;

       positions.pop(); // 移除最后那个多余的鼠标跟随点
       if (positions.length < 2) return;

       // ----- 计算结果 -----
       if (type === 'LineString') {
         let distance = 0;
         for (let i = 0; i < positions.length - 1; i++) {
           distance += Cesium.Cartesian3.distance(positions[i], positions[i+1]);
         }
         alert(`3D 测距结果: ${distance.toFixed(2)} m`);

       } else {
         // 测面积：将 Cesium 坐标转换回经纬度，调用 OpenLayers 高精度算法计算
               const coords = positions.map(p => {
                 const carto = Cesium.Cartographic.fromCartesian(p);
                 return [Cesium.Math.toDegrees(carto.longitude), Cesium.Math.toDegrees(carto.latitude)];
               });
               coords.push(coords[0]); // 闭合多边形

               const polygonGeom = new Polygon([coords]);
               // 【修复核心】：明确指定投影为 EPSG:4326
               const area = getArea(polygonGeom, { projection: 'EPSG:4326' });
               alert(`3D 测面积结果: ${(area / 1000000).toFixed(2)} Km²`);
       }
     }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

     // 给用户的操作提示
     alert(`已开启 3D 测量\n操作说明：\n1. 鼠标【左键】点击地图添加点\n2. 鼠标【右键】点击结束并获取结果`);

 }
 function resetNorth() {
   map.getView().animate({ rotation: 0, duration: 500 });
  }
 function zoomIn() {
   map.getView().animate({ zoom: map.getView().getZoom() + 1, duration: 250 });
  }
 function zoomOut() {
    map.getView().animate({ zoom: map.getView().getZoom() - 1, duration: 250 });
   }
 function locateMe() {
   // 快速定位回沈阳市中心
   map.getView().animate({ center: [13741313, 5130280], zoom: 12, duration: 1000 });
  }
</script>

<style scoped>
#map { width: 100vw; height: 98vh; }
.td-tools {
  position: absolute;
  right: 20px;
  top: 20px;
  background: white;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
  gap: 8px;
}
button { cursor: pointer; padding: 4px 8px; }
.tool-group {
  border-top: 1px solid #eee;
  padding-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.tool-group label {
  font-size: 12px;
  color: #333;
}

.map-controls {
  position: absolute;
  bottom: 80px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 1000; /* 确保在地图上方 */
}

.map-controls button {
  width: 40px;
  height: 40px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-controls button:hover {
  background-color: #f0f0f0;
}

.mouse-coord {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.8);
  padding: 5px 10px;
  border-radius: 4px;
  font-family: monospace;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
.info-tooltip {
  position: absolute;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 10px;
  border-radius: 4px;
  pointer-events: none; /* 确保鼠标不会挡住拾取 */
  max-width: 300px;
  font-size: 13px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
  border: 1px solid #555;
}

.tooltip-header {
  border-bottom: 1px solid #666;
  padding-bottom: 5px;
  margin-bottom: 5px;
  font-weight: bold;
  color: #00d2ff;
}

.tooltip-body div {
  margin: 3px 0;
  word-break: break-all;
}
</style>