<template>
  <div id="map"></div>
  <LayerPanel
    @add="addLayer"
    @remove="removeLayer"
    @toggle-function="handleFunction"
  />

</template>

<script setup>
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

// 业务 API
import { planRoute } from '@/api/route'
import { fetchAccessiblePois } from '@/api/accessibility'
import LayerPanel from '@/components/LayerPanel.vue'
import { fetchPresetRoute } from '@/api/routing'

// 状态管理
let map
let start = null
let end = null

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

onMounted(() => {
  map = new Map({
    target: 'map',
    layers: [
      new TileLayer({ source: new OSM() }),
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
})

function addLayer(id, data) {
  const source = new VectorSource({
    features: new GeoJSON().readFeatures(data, {
    dataProjection: 'EPSG:4326',
    featureProjection: 'EPSG:3857' })
  })
  const layer = new VectorLayer({
      source,
      style: new Style({
        stroke: new Stroke({ color: 'blue', width: 3 }),
        fill: new Fill({ color: 'rgba(0, 0, 255, 0.1)' }),
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
  if (businessLayers[id]) {
    map.removeLayer(businessLayers[id])
    delete businessLayers[id]
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

    console.log("OpenLayers 解析出的 Feature 数量:", features.length);

    routeLayer.getSource().addFeatures(features);
    console.log("source features:", routeLayer.getSource().getFeatures());
    console.log("feature geometry:", routeLayer.getSource().getFeatures()[0].getGeometry().getCoordinates());
    console.log("extent:", routeLayer.getSource().getExtent());
    console.log("Raw coordinates:", res.data.features[0].geometry.coordinates);
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

</script>

<style scoped>
#map { width: 100vw; height: 100vh; }
</style>