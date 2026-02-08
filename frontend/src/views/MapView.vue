<template>
  <div id="map"></div>
  <LayerPanel
    @add="addLayer"
    @remove="removeLayer"
    @toggle-function="handleAccessibility"
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

// 状态管理
let map
let start = null
let end = null
const accessibilityEnabled = ref(false)
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
  style: new Style({ stroke: new Stroke({ color: '#ffcc33', width: 4 }) })
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

  map.on('click', (evt) => {
    const coord = evt.coordinate

    // --- 逻辑 A: 可达性分析 (优先判断) ---
    if (accessibilityEnabled.value) {
      const lonlat = toLonLat(coord)
      fetchAccessiblePois(lonlat[0], lonlat[1], 2000).then(res => {
        const source = poiLayer.getSource()
        source.clear()

        // 绘制范围圈
        const circleFeat = new Feature({ geometry: new GeomCircle(coord, 2000) })
        circleFeat.setStyle(rangeStyle)

        // 绘制点击中心点
        const centerFeat = new Feature({ geometry: new Point(coord) })
        centerFeat.setStyle(pointStyle)

        // 解析后端返回的 POI 点
        const poiFeats = new GeoJSON().readFeatures(res.data, {
          featureProjection: 'EPSG:3857'
        })

        source.addFeatures([circleFeat, centerFeat, ...poiFeats])
      })
      return // 开启分析时不执行路径规划
    }

    // --- 逻辑 B: 路径规划 ---
    if (!start) {
      start = coord
      console.log('起点已选')
    } else {
      end = coord
      drawRoute()
      start = null
      end = null
    }
  })
})

function addLayer(id, data) {
  const source = new VectorSource({
    features: new GeoJSON().readFeatures(data, { featureProjection: 'EPSG:3857' })
  })
  const layer = new VectorLayer({ source })
  map.addLayer(layer)
  businessLayers[id] = layer
}

function removeLayer(id) {
  if (businessLayers[id]) {
    map.removeLayer(businessLayers[id])
    delete businessLayers[id]
  }
}

function handleAccessibility(layer) {
  if (layer.id === 'accessibility') {
    accessibilityEnabled.value = layer.visible
    if (!layer.visible) {
      poiLayer.getSource().clear()
    }
  }
}

function drawRoute() {
  planRoute(toLonLat(start), toLonLat(end)).then(res => {
    routeLayer.getSource().clear()
    const features = new GeoJSON().readFeatures(res.data, { featureProjection: 'EPSG:3857' })
    routeLayer.getSource().addFeatures(features)
  })
}
</script>

<style scoped>
#map { width: 100vw; height: 100vh; }
</style>