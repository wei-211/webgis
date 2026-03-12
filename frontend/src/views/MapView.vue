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
        <label>日照时间模拟 ({{ shadowTime }}:00)</label>
        <input type="range" min="0" max="24" step="1" v-model="shadowTime" @input="updateShadowTime">
        <button @click="toggleShadows">{{ isShadowActive ? '关闭日照' : '开启日照' }}</button>
      </div>
      <div class="tool-group">
        <label>淹没分析 (当前水位: {{ waterLevel }}m)</label>
        <button @click="startFlood">{{ isFlooding ? '停止淹没' : '开始淹没' }}</button>
      </div>
    </div>

</template>

<script setup>
import XYZ from 'ol/source/XYZ'
import { onMounted, ref } from 'vue'
import { Map, View, Feature } from 'ol' // 必须引入 Feature
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import OSM from 'ol/source/OSM'
import VectorSource from 'ol/source/Vector'
import GeoJSON from 'ol/format/GeoJSON'
import { Stroke, Style, Fill, Circle as CircleStyle } from 'ol/style'
import { Point, Circle as GeomCircle } from 'ol/geom' // 必须引入几何类型
import { toLonLat } from 'ol/proj'
import OLCesium from 'ol-cesium'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
window.Cesium = Cesium
window.CESIUM_BASE_URL = '/Cesium/';
// 业务 API
import { planRoute } from '@/api/route'
import { fetchAccessiblePois } from '@/api/accessibility'
import LayerPanel from '@/components/LayerPanel.vue'
import { fetchPresetRoute } from '@/api/routing'

// 状态管理
let map
let start = null
let end = null
let ol3d = null
let buildDataSource = null
let currentCesiumTime = Cesium.JulianDate.now()
let floodDataSource = new Cesium.CustomDataSource('flood')
const is3D = ref(false)
const show3DBuildings = ref(false)
const TDT_TK ='24f06bfc85c85fc8114b3b65901416d7'

const functionState = ref({
  accessibility: false,
  'knn-poi': false,
  routing: false,
  'route-accessibility': false
})

const businessLayers = {} // 必须定义，解决之前移除图层的报错

// 1. 样式定义
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

// 2. 图层准备
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

onMounted(async () => {
  map = new Map({
    target: 'map',
        layers: [
          new TileLayer({
            source: new XYZ({
              url: `http://t0.tianditu.gov.cn/DataServer?T=vec_w&x={x}&y={y}&l={z}&tk=${TDT_TK}`
            })
          }),
          new TileLayer({
            source: new XYZ({
              url: `http://t0.tianditu.gov.cn/DataServer?T=cva_w&x={x}&y={y}&l={z}&tk=${TDT_TK}`
            })
          }),
      routeLayer,
      poiLayer
    ],
    view: new View({
      center: [13750000, 5130000],
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

})

function addLayer(id, data) {
  const source = new VectorSource({
    features: new GeoJSON().readFeatures(data, {
    dataProjection: 'EPSG:4326',
    featureProjection: 'EPSG:3857' })
  })

  if (id === 'build') {

    // ===== 1. 创建 2D 图层 =====
    const layer = new VectorLayer({
      source,
      style: new Style({
        stroke: new Stroke({ color: '#999', width: 1 }),
        fill: new Fill({ color: 'rgba(200,200,200,0.8)' })
      })
    })

    businessLayers[id] = layer
    map.addLayer(layer)
    if (is3D.value) {

        waitForViewer(() => {
          load3DBuildings(source)
        })
      }
      console.log("addLayer id =", id)
      console.log("is3D.value =", is3D.value)
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

      if (is3D.value) {
            waitForViewer(() => {
              // 核心修复：开启3D时，检查建筑图层是否已存在，若存在则手动补跑拉伸
              const buildLayer = businessLayers['build'];
              if (buildLayer) {
                console.log("检测到已有建筑数据，正在补跑3D拉伸...");
                load3DBuildings(buildLayer.getSource());
              }
            });
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
    console.log("收到路段总数:", res.data.features.length); // 如果这里是 1，说明后端 SQL 依然没 JOIN 到中间数据
    console.log(JSON.stringify(res.data).length)
    console.log(res.data)
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

function load3DBuildings(source) {
  const features = source.getFeatures();
  console.log("--- 函数已运行，待拉伸要素数量:", features.length);

  const scene = ol3d.getCesiumScene();
  if (!scene) return; // 移除对 _viewer 的检查

  // 1. 获取 ol-cesium 管理的数据源集合
  const dataSources = ol3d.getDataSources();

  // 2. 将 OpenLayers 要素转为 GeoJSON
  const geojson = new GeoJSON().writeFeaturesObject(
    features,
    {
      featureProjection: 'EPSG:3857',
      dataProjection: 'EPSG:4326'
    }
  );

  // 3. 加载到 Cesium
  Cesium.GeoJsonDataSource.load(geojson).then(dataSource => {
    // 如果之前已经存在旧的建筑数据源，先移除
    if (buildDataSource) {
      dataSources.remove(buildDataSource);
    }

    dataSources.add(dataSource);
    buildDataSource = dataSource;

    // 4. 遍历实体并设置拉伸高度
    dataSource.entities.values.forEach(entity => {
      if (entity.polygon) {
        // 1. 设置高度参考系为相对于地面
        entity.polygon.heightReference = Cesium.HeightReference.RELATIVE_TO_GROUND;
        entity.polygon.extrudedHeightReference = Cesium.HeightReference.RELATIVE_TO_GROUND;
        entity.polygon.height = 0;
        // 2. height = 0 表示底部紧贴地面
        let rawHeight = entity.properties.height ? entity.properties.height.getValue() : 20;
        entity.polygon.extrudedHeight = rawHeight;
        // 3. 设置一个合理的拉伸高度（例如 30 到 50 米）
        if (rawHeight > 50) {
                entity.polygon.material = Cesium.Color.fromCssColorString('#4a90e2').withAlpha(0.8);
            } else {
                entity.polygon.material = Cesium.Color.BLUE.withAlpha(0.8);
            }

            entity.polygon.outline = true;
            entity.polygon.outlineColor = Cesium.Color.BLUE.withAlpha(0.3);
            entity.polygon.shadows = Cesium.ShadowMode.CAST_AND_RECEIVE;
          }
    });

    console.log("--- 3D建筑拉伸逻辑执行完毕 ---");
  }).catch(err => {
    console.error("Cesium 加载 GeoJSON 失败:", err);
  });
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

   const today = new Date()
   const utcHour = (shadowTime.value - 8 + 24) % 24
   const dateString = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}T${String(utcHour).padStart(2, '0')}:00:00Z`

   // 更新全局时间，ol3d的内部渲染循环会自动读取它
   currentCesiumTime = Cesium.JulianDate.fromIso8601(dateString)
 }

 const isFlooding = ref(false)
 const waterLevel = ref(0)
 let floodEntity = null
 let floodTimer = null

 function startFlood() {
   const scene = ol3d.getCesiumScene()
   if (!scene) return

   isFlooding.value = !isFlooding.value

   if (isFlooding.value) {
     waterLevel.value = 100 // 重置水位

     // 确保我们将自定义的 dataSource 加入到了场景的数据源集合中
     if (!ol3d.getDataSources().contains(floodDataSource)) {
       ol3d.getDataSources().add(floodDataSource)
     }

     if (!floodEntity) {
       // 在自定义数据源上挂载实体，而不是 viewer
       floodEntity = floodDataSource.entities.add({
         polygon: {
           // 你可以稍微扩大包围盒以覆盖大片区域
           hierarchy: Cesium.Cartesian3.fromDegreesArray([
             123.0, 41.5,
             124.0, 41.5,
             124.0, 42.0,
             123.0, 42.0
           ]),
           extrudedHeight: new Cesium.CallbackProperty(() => waterLevel.value, false),
           height: 0,
           material: Cesium.Color.fromCssColorString('#00BFFF').withAlpha(0.6),
           perPositionHeight: false
         }
       })
     }

     floodTimer = setInterval(() => {
       if (waterLevel.value >= 1000) {
         clearInterval(floodTimer)
         return
       }
       waterLevel.value += 0.5
     }, 100)

   } else {
         if (floodTimer) {
           clearInterval(floodTimer)
           floodTimer = null
         }
         }
 }
</script>

<style scoped>
#map { width: 100vw; height: 100vh; }
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
</style>