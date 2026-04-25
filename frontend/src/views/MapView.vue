<template>
<div class="layout-container">
    <header class="top-header">
      <div class="logo-title">
        <h1>沈阳市空间数据管理系统</h1>
      </div>
      <div class="user-actions">
        <div class="analysis-dropdown" @mouseenter="showAnalysisMenu = true" @mouseleave="showAnalysisMenu = false">
          <button class="toggle-mode-btn" style="background-color: #67c23a; border-color: #67c23a;">
            ⚙️ 空间分析 ▼
          </button>
          <div class="dropdown-content" v-show="showAnalysisMenu">
            <a v-for="func in functionLayers" :key="func.id"
               :class="{ active: functionState[func.id] }"
               @click="handleFunctionClick(func)">
              {{ func.name }}
            </a>
            <a :class="{ active: showQueryWindow }" @click="openSpatialQuery">
               综合空间查询
            </a>
          </div>
        </div>

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
              <span>🗺️ 资源目录</span>
              <span class="arrow" :class="{ open: openData }">▼</span>
            </div>
            <div class="group-content" v-show="openData" style="padding: 5px 0;">

              <div v-for="group in groupedDataLayers" :key="group.name" class="district-group">
                <div class="district-title" @click="groupOpenState[group.name] = !groupOpenState[group.name]"
                  style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; cursor: pointer;">
                  <span style="color: #a6c0ea; font-size: 14px;">
                    {{ groupOpenState[group.name] ? '📂' : '📁' }} {{ group.name }}
                  </span>
                  <span class="arrow" :class="{ open: groupOpenState[group.name] }">▼</span>
                </div>
                <div class="district-layers" v-show="groupOpenState[group.name]">
                  <label v-for="layer in group.layers" :key="layer.id" class="layer-item" :class="{ active: layer.visible }" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="layer.visible" @change="handleLayerChange(layer)" />
                    <span class="layer-name">{{ layer.name }}</span>
                  </label>
                </div>
              </div>

              <div class="district-group">
                <div class="district-title" @click="groupOpenState['遥感影像数据'] = !groupOpenState['遥感影像数据']" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; cursor: pointer;">
                  <span style="color: #a6c0ea; font-size: 14px;">{{ groupOpenState['遥感影像数据'] ? '📂' : '📁' }} 沈阳市遥感影像数据</span>
                  <span class="arrow" :class="{ open: groupOpenState['遥感影像数据'] }">▼</span>
                </div>
                <div class="district-layers" v-show="groupOpenState['遥感影像数据']">
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="extraLayers.rs2022" @change="toggleExtraLayer('rs2022')" />
                    <span class="layer-name">2022年沈阳影像</span>
                  </label>
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="extraLayers.rs2025" @change="toggleExtraLayer('rs2025')" />
                    <span class="layer-name">2025年沈阳影像</span>
                  </label>
                </div>
              </div>

              <div class="district-group">
                <div class="district-title" @click="groupOpenState['土地利用数据'] = !groupOpenState['土地利用数据']" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; cursor: pointer;">
                  <span style="color: #a6c0ea; font-size: 14px;">{{ groupOpenState['土地利用数据'] ? '📂' : '📁' }} 沈阳市土地利用数据</span>
                  <span class="arrow" :class="{ open: groupOpenState['土地利用数据'] }">▼</span>
                </div>
                <div class="district-layers" v-show="groupOpenState['土地利用数据']">
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="extraLayers.landuse2020" @change="toggleExtraLayer('landuse2020')" />
                    <span class="layer-name">2020年土地利用</span>
                  </label>
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="extraLayers.landuse2023" @change="toggleExtraLayer('landuse2023')" />
                    <span class="layer-name">2023年土地利用</span>
                  </label>
                </div>
              </div>
              <div class="district-group">
                <div class="district-title" @click="groupOpenState['气象数据'] = !groupOpenState['气象数据']" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; cursor: pointer;">
                  <span style="color: #a6c0ea; font-size: 14px;">{{ groupOpenState['气象数据'] ? '📂' : '📁' }} 沈阳市气象数据</span>
                  <span class="arrow" :class="{ open: groupOpenState['气象数据'] }">▼</span>
                </div>
                <div class="district-layers" v-show="groupOpenState['气象数据']">
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" v-model="extraLayers.weather" @change="toggleExtraLayer('weather')" />
                    <span class="layer-name">实时气象监测点</span>
                  </label>
                </div>
              </div>
              <div class="district-group">
                <div class="district-title" @click="groupOpenState['建筑模型'] = !groupOpenState['建筑模型']"
                  style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; cursor: pointer;">
                  <span style="color: #a6c0ea; font-size: 14px;">
                    {{ groupOpenState['建筑模型'] ? '📂' : '📁' }} 沈阳市城市建筑
                  </span>
                  <span class="arrow" :class="{ open: groupOpenState['建筑模型'] }">▼</span>
                </div>
                <div class="district-layers" v-show="groupOpenState['建筑模型']">
                  <label class="layer-item" style="padding-left: 45px; margin-top: 4px;">
                    <input type="checkbox" :checked="show3DBuildings" @change="handleSmart3DBuilding" />
                    <span class="layer-name">和平区 3D 建筑模型</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
      <main class="map-wrapper">
        <div class="map-toolbar">
          <div class="toolbar-group">
            <button class="tool-btn" @click="flyToShenyang">📍沈阳市</button>
            <button class="tool-btn" :class="{ active: enableLayerInfo }" @click="enableLayerInfo = !enableLayerInfo">ℹ️图层属性</button>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <button class="tool-btn" @click="startMeasure('LineString')">📏测线</button>
            <button class="tool-btn" @click="startMeasure('Polygon')">📐测面积</button>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <button class="tool-btn" :class="{ active: isSwipeActive }" @click="toggleSwipeMode">🌗卷帘</button>
            <button class="tool-btn danger" @click="clearAll">🗑️清除</button>
          </div>
        </div>

        <div id="map" class="full-map"></div>
        <div class="map-controls">
          <button @click="resetNorth" title="恢复正北">N</button>
          <button @click="zoomIn" title="放大">+</button>
          <button @click="zoomOut" title="缩小">-</button>
        </div>
        <div id="mouse-position" class="mouse-coord"></div>

        <div ref="infoTooltip" class="info-tooltip" v-show="tooltipData.show && enableLayerInfo" :style="tooltipStyle">
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

      <div class="query-window" v-show="showQueryWindow" style="top: 80px; right: 80px; width: 480px; z-index: 1000;">
        <div class="qw-header">
          <h3>综合空间查询</h3>
          <button class="qw-close-btn" @click="closeQueryWindow">✖</button>
        </div>

        <div class="qw-search-filter" style="padding: 15px 20px;">
          <div class="search-row" style="display: flex; gap: 10px; margin-bottom: 15px;">
            <input type="text" v-model="searchQuery" @keyup.enter="handleSearch" placeholder="输入名称或关键字..." style="flex: 1; padding: 8px 12px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 13px;" />
            <button class="qw-search-btn" @click="handleSearch" style="padding: 8px 20px; background-color: #1890ff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px;">搜索</button>
          </div>
          <div class="filter-row" style="display: flex; align-items: center; font-size: 13px;">
            <label style="margin-right: 10px; color: #606266;">筛选条件:</label>
            <select v-model="filterLayer" style="padding: 6px; border: 1px solid #dcdfe6; border-radius: 4px; width: 150px;">
              <option value="all">所有图层</option>
              <option value="build">城市建筑</option>
              <option value="pois">兴趣点 (POI)</option>
              <option value="roads">道路交通</option>
              </select>
          </div>
        </div>

        <div class="qw-results" style="padding: 0 20px 20px 20px;">
            <div style="font-size: 13px; color: #606266; margin-bottom: 10px;" v-show="searchResults.length > 0">
              合计 <span style="color: #1890ff; font-weight: bold;">{{ searchResults.length }}</span> 个查询的 <span style="color: #1890ff; font-weight: bold;">{{ searchQuery || '要素' }}</span>；
              可容纳的总人数 <span style="color: #67c23a; font-weight: bold;">{{ totalCapacitySum }}</span> 人
            </div>

          <div class="table-container" style="max-height: 300px; overflow-y: auto; border: 1px solid #ebeef5; border-radius: 4px;" v-show="searchResults.length > 0">
            <table class="data-table" style="width: 100%; font-size: 12px; border-collapse: collapse;">
              <thead style="background-color: #f5f7fa; position: sticky; top: 0; z-index: 1;">
                <tr>
                  <th style="padding: 10px 8px; text-align: left; border-bottom: 1px solid #ebeef5;" width="40">序号</th>
                  <th style="padding: 10px 8px; text-align: left; border-bottom: 1px solid #ebeef5;">要素名称</th>
                  <th style="padding: 10px 8px; text-align: left; border-bottom: 1px solid #ebeef5;" width="80">来源图层</th>
                  <th style="padding: 10px 8px; text-align: left; border-bottom: 1px solid #ebeef5;" width="100">核心指标</th>
                  <th style="padding: 10px 8px; text-align: center; border-bottom: 1px solid #ebeef5;" width="60">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in paginatedResults" :key="index"
                    @click="locateFeature(item, index)"
                    style="border-bottom: 1px solid #ebeef5; cursor: pointer; transition: background-color 0.2s;"
                    :style="{ backgroundColor: activeRow === index ? '#ecf5ff' : '' }"
                    onmouseover="this.style.backgroundColor='#f5f7fa'"
                    onmouseout="if(this.style.backgroundColor !== 'rgb(236, 245, 255)') this.style.backgroundColor=''">
                  <td style="padding: 10px 8px;">{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                  <td style="padding: 10px 8px; font-weight: 500; color: #303133;">{{ item.name || '未命名要素' }}</td>
                  <td style="padding: 10px 8px; color: #909399;">{{ getLayerLabel(item.source_table) }}</td>
                  <td style="padding: 10px 8px; color: #67c23a;">{{ getDynamicMetric(item) }}</td>
                  <td style="padding: 10px 8px; text-align: center;">
                    <button class="locate-btn" style="padding: 4px 8px; font-size: 12px; background-color: #1890ff; color: #fff; border: none; border-radius: 3px; cursor: pointer;">定位📍</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div style="display: flex; justify-content: flex-end; align-items: center; margin-top: 15px; font-size: 12px;" v-show="totalPages > 1">
            <span style="margin-right: 15px; color: #606266;">第 {{ currentPage }} / {{ totalPages }} 页</span>
            <button @click="currentPage > 1 && currentPage--" :disabled="currentPage === 1"
                    style="padding: 5px 10px; margin-right: 8px; border: 1px solid #dcdfe6; background: #fff; border-radius: 3px; cursor: pointer;"
                    :style="{ opacity: currentPage === 1 ? 0.5 : 1 }">上一页</button>
            <button @click="currentPage < totalPages && currentPage++" :disabled="currentPage === totalPages"
                    style="padding: 5px 10px; border: 1px solid #dcdfe6; background: #fff; border-radius: 3px; cursor: pointer;"
                    :style="{ opacity: currentPage === totalPages ? 0.5 : 1 }">下一页</button>
          </div>
        </div>
      </div>
      <div class="query-window" v-show="showAnalysisWindow" style="top: 80px;right: 80px; width: 380px; z-index: 1000;">
        <div class="qw-header">
          <h3>⚡ {{ activeAnalysisName || '空间分析配置' }}</h3>
          <button class="qw-close-btn" @click="closeAnalysisWindow">✖</button>
        </div>

        <div class="qw-search-filter" style="padding: 15px 20px;">
          <div v-if="functionState['accessibility']">
            <label style="font-size: 14px; font-weight: bold; color: #333;">分析缓冲半径：</label>
            <input type="number" v-model="accessibilityRadius" step="100" style="width: 100px; padding: 6px; border: 1px solid #dcdfe6; border-radius: 4px;" />
            <span style="font-size: 14px; margin-left: 5px;">米</span>
            <p style="margin-top: 10px; margin-bottom: 0; font-size: 12px; color: #1890ff; background: #e6f7ff; padding: 8px; border-radius: 4px;">
              💡 操作提示：请在左侧地图上点击目标位置作为中心点，系统将提取该范围内的教育设施数据。
            </p>
          </div>
          <div v-if="functionState['knn-poi']">
            <p style="margin: 0; font-size: 12px; color: #1890ff; background: #e6f7ff; padding: 8px; border-radius: 4px; margin-bottom: 15px;">
                💡 操作提示：请在地图上点击，系统将自动计算并高亮离您最近的医疗设施。
              </p>

              <div v-if="nearestResult" class="result-card" style="border: 1px solid #ebeef5; border-radius: 6px; padding: 15px; background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                <div style="display: flex; align-items: center; margin-bottom: 10px; border-bottom: 1px solid #f0f0f0; padding-bottom: 8px;">
                  <span style="font-size: 18px; margin-right: 8px;">🏥</span>
                  <h3 style="margin: 0; font-size: 15px; color: #303133;">{{ nearestResult.properties.name  }}</h3>
                </div>

                <div style="font-size: 13px; line-height: 1.8;">
                  <div>
                    <span style="color: #909399;">设施类型：</span>
                    <span style="color: #606266; font-weight: 500;">{{ getFriendlyType(nearestResult.properties.type) }}</span>
                  </div>
                  <div>
                    <span style="color: #909399;">直线距离：</span>
                    <span style="color: #f56c6c; font-weight: bold; font-size: 14px;">{{ getFormattedDistance(nearestResult.properties.distance) }}</span>
                  </div>
                </div>

                <div style="margin-top: 12px; text-align: right;">
                   <button @click="locateNearestPoi" style="padding: 5px 12px; font-size: 12px; background-color: #1890ff; color: #fff; border: none; border-radius: 3px; cursor: pointer;">定位📍</button>
                </div>
              </div>
          </div>
          <div v-if="functionState['routing']">
             <p style="margin: 0; font-size: 13px; color: #409eff; background: #e6f7ff; padding: 12px; border-radius: 4px; display: flex; align-items: center; border: 1px solid #b3d8ff;">
                <span style="margin-right: 8px;">📍</span> 已为您自动加载预设巡检路线。
             </p>
          </div>
        </div>

        <div v-show="showBufferResultWindow && functionState['accessibility']" style="border-top: 1px solid #ebeef5; padding: 15px 20px; background-color: #fafafa; border-bottom-left-radius: 6px; border-bottom-right-radius: 6px;">
          <div style="font-size: 14px; font-weight: bold; margin-bottom: 10px; color: #333;">📊 分析结果列表</div>
          <div class="results-info" v-if="bufferResults.length > 0" style="font-size: 13px; margin-bottom: 10px;">
            在指定范围内共找到 <span style="color:red; font-weight:bold">{{ bufferResults.length }}</span> 个教育设施
          </div>
          <div class="results-empty" v-else style="font-size: 13px; color: #909399;">范围内暂无数据。</div>

          <div class="table-container" style="max-height: 250px; overflow-y: auto; background: #fff; border: 1px solid #ebeef5;" v-show="bufferResults.length > 0">
            <table class="data-table" style="font-size: 12px; width: 100%; border-collapse: collapse;">
              <thead style="background-color: #f5f7fa;">
                <tr>
                  <th style="padding: 8px; text-align: left; border-bottom: 1px solid #ebeef5;" width="40">序号</th>
                  <th style="padding: 8px; text-align: left; border-bottom: 1px solid #ebeef5;">设施名称</th>
                  <th style="padding: 8px; text-align: center; border-bottom: 1px solid #ebeef5;" width="60">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in bufferResults" :key="index" style="border-bottom: 1px solid #ebeef5;">
                  <td style="padding: 8px;">{{ index + 1 }}</td>
                  <td style="padding: 8px;">{{ item.properties?.name || '未命名' }}</td>
                  <td style="padding: 8px; text-align: center;">
                    <button class="locate-btn" @click="locateBufferFeature(item)" style="padding: 4px 8px; font-size: 12px; background-color: #1890ff; color: #fff; border: none; border-radius: 3px; cursor: pointer;">查看</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="query-window" v-show="show3DWindow" style="top: 80px; right: 80px; width: 380px;">
        <div class="qw-header">
          <h3>🧊 三维立体分析工具箱</h3>
          <button class="qw-close-btn" @click="show3DWindow = false">✖</button>
        </div>
        <div class="qw-search-filter" style="padding: 15px 20px; display: flex; flex-direction: column; gap: 15px;">
          <div style="display: flex; gap: 10px;">
             <button class="qw-search-btn" style="flex: 1; padding: 8px 0;" @click="toggleTerrain">切换地形</button>
             <button class="qw-search-btn" style="flex: 1; padding: 8px 0; background-color: #67c23a;" @click="toggle3DBuildings">
               {{ show3DBuildings ? '隐藏建筑' : '显示建筑' }}
             </button>
          </div>
          <hr style="border: 0; border-top: 1px solid #ebeef5; margin: 5px 0;" />
          <div>
            <label style="font-size: 14px; font-weight: bold; color: #333;">☀️ 日照时间模拟 ({{ shadowTime }}:00)</label>
            <div style="display: flex; align-items: center; gap: 10px; margin-top: 10px;">
              <input type="range" min="0" max="24" step="1" v-model="shadowTime" @input="updateShadowTime" style="flex: 1; cursor: pointer;">
              <button class="qw-search-btn" :style="{ backgroundColor: isShadowActive ? '#f56c6c' : '#1890ff' }" @click="toggleShadows">
                {{ isShadowActive ? '关闭日照' : '开启日照' }}
              </button>
            </div>
          </div>
          <div style="margin-top: 5px;">
            <label style="font-size: 14px; font-weight: bold; color: #333;">🌊 淹没分析 (当前水位: {{ waterLevel - 40 }}m)</label>
            <div style="margin-top: 10px;">
              <button class="qw-search-btn" style="width: 100%; padding: 10px 0;" :style="{ backgroundColor: isFlooding ? '#f56c6c' : '#1890ff' }" @click="startFlood">
                {{ isFlooding ? '停止淹没' : '开始淹没' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="query-window" v-show="showMeasureWindow" style="top: 80px; left: 50%; transform: translateX(-50%); width: 320px;">
        <div class="qw-header">
          <h3>{{ measureWindowTitle }}</h3>
          <button class="qw-close-btn" @click="closeMeasureWindow">✖</button>
        </div>
        <div class="qw-search-filter" style="padding: 20px; text-align: center;">
          <div style="font-size: 16px; font-weight: bold; color: #1890ff; margin-bottom: 15px; padding: 10px; background: #e6f7ff; border-radius: 4px;">
            {{ measureResultText }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import XYZ from 'ol/source/XYZ'
import 'ol/ol.css'
import 'ol-ext/dist/ol-ext.css' // 引入 ol-ext 样式
import Swipe from 'ol-ext/control/Swipe' // 引入卷帘控件
import TileWMS from 'ol/source/TileWMS';
import { onMounted, ref, reactive, computed } from 'vue'
import { Map, View, Feature } from 'ol'
import TileLayer from 'ol/layer/Tile'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import GeoJSON from 'ol/format/GeoJSON'
import { Stroke, Style, Fill, Circle as CircleStyle, Text, Icon } from 'ol/style'
import { Point, Circle as GeomCircle, Polygon, LineString } from 'ol/geom'
import { circular } from 'ol/geom/Polygon';
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
import { getNearestPoi } from '@/api/KnnPoi'
import { fetchPresetRoute } from '@/api/routing'
import { layers as configLayers } from '@/config/layers'
import { loadLayer } from '@/api/layerApi'
import { FRIENDLY_KEYS, formatPropertyValue, getLayerLabel,
safeParseProps, getDynamicMetric,groupLayersByDistrict} from '@/utils/gisUtils';
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
const nearestResult = ref(null);
// 提取友好的类型名称 (利用你已有的字典)
const getFriendlyType = (typeCode) => {
  return FRIENDLY_KEYS[typeCode] || formatPropertyValue('type', typeCode) || typeCode;
};
// 格式化距离 (利用你已有的工具函数)
const getFormattedDistance = (distance) => {
  return formatPropertyValue('distance', distance);
};
const groupOpenState = reactive({
  '全市通用图层': false, // 👈 默认关闭'和平区': false,'沈河区': false,'大东区': false,'皇姑区': false,'铁西区': false,
  '苏家屯区': false,'浑南区': false,'沈北新区': false,'于洪区': false,'辽中区': false,'新民市': false,'康平县': false,'法库县': false
});
let weatherInterval = null;
const WEATHER_STATIONS = [
  { name: '沈阳市', lon: 123.43, lat: 41.78 },
  { name: '辽中区', lon: 122.75, lat: 41.51 },
  { name: '新民市', lon: 122.83, lat: 41.99 },
  { name: '法库县', lon: 123.41, lat: 42.50 },
  { name: '康平县', lon: 123.36, lat: 42.74 }
];
const QWEATHER_KEY = 'd19f6103a74346a7857dee152c1932aa';
// 动态计算图层列表
const layerList = reactive(configLayers.map(l => ({ ...l, visible: l.active || false })));
const dataLayers = computed(() => layerList.filter(l => l.type === 'business' || l.type === 'display'));
const functionLayers = computed(() => layerList.filter(l => l.type === 'function' && l.id !== 'view-3d'));
const groupedDataLayers = computed(() => groupLayersByDistrict(dataLayers.value));
// --- 地图核心变量 ---
let map
let start = null
let end = null
let ol3d = null
let buildDataSource = null
let vectorBaseLayer = null
let vectorAnnoLayer = null
let imgBaseLayer = null
let imgAnnoLayer = null
const extraLayers = reactive({ rs2022: false, rs2025: false, landuse2020: false, landuse2023: false,
weather: false,buildHeping: false });
const showAnalysisMenu = ref(false);     // 控制顶部空间分析下拉菜单
const enableLayerInfo = ref(false);       // 控制是否开启图层属性弹窗
const isSwipeActive = ref(false);        // 控制卷帘状态
const showBufferResultWindow = ref(false); // 控制分析结果面板显示
const bufferResults = ref([]);           // 存储缓冲区/最近邻分析的结果数据
const swipeCtrl = new Swipe();           // 实例化卷帘控件
const measureWindowTitle = ref('🛠️ 空间量测'); // 新增：控制弹窗的标题
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
const searchResults = ref([]);
const currentPage = ref(1);
const pageSize = ref(5);
const activeRow = ref(null);
const showQueryWindow = ref(false);
const filterLayer = ref('all');
const showAnalysisWindow = ref(false); // 控制空间分析窗口
const activeAnalysisName = ref('');    // 当前正在使用的分析功能名称
const accessibilityRadius = ref(2000); // 提取出来的缓冲半径，让用户可调
const show3DWindow = ref(false);       // 控制三维工具箱窗口
const showMeasureWindow = ref(false);
const measureResultText = ref('请在地图上点击开始测量...');

// 关闭量测窗口
const closeMeasureWindow = () => {
  showMeasureWindow.value = false;
  clearMeasure();
};

// 清除所有测量图形和状态
const clearMeasure = () => {
  measureSource.clear();
  if (drawInteraction) {
    map.removeInteraction(drawInteraction);
    drawInteraction = null;
  }
  if (is3D.value && measure3DDataSource) {
    measure3DDataSource.entities.removeAll();
    if (measureHandler) {
      measureHandler.destroy();
      measureHandler = null;
    }
    isMeasuring3D = false;
  }
  measureResultText.value = '请在地图上点击开始测量...';
};
// 统一的关闭空间分析窗口方法
const closeAnalysisWindow = () => {
  showAnalysisWindow.value = false;
  activeAnalysisName.value = '';
  Object.keys(functionState.value).forEach(k => functionState.value[k] = false);
  poiLayer.getSource().clear();
  routeLayer.getSource().clear();
};
const closeQueryWindow = () => {
  showQueryWindow.value = false;
  searchResults.value = [];
  highlightSource.clear();
  activeRow.value = null;
};
const openSpatialQuery = () => {
  showAnalysisMenu.value = false;
  closeAnalysisWindow();
  showQueryWindow.value = true;
};
const handleFunctionClick = (layer) => {
  // 1. 拦截：如果点击的是“3D 视图模式”，只做切换，千万别打开分析窗口！
  if (layer.id === 'view-3d') {
    is3D.value = !is3D.value;
    ol3d.setEnabled(is3D.value);
    if (is3D.value) {
          // 进入3D：隐藏矢量，显示卫星
          vectorBaseLayer.setVisible(false);
          vectorAnnoLayer.setVisible(false);
          imgBaseLayer.setVisible(true);
          imgAnnoLayer.setVisible(true);
        } else {
          // 退出3D：显示矢量，隐藏卫星
          vectorBaseLayer.setVisible(true);
          vectorAnnoLayer.setVisible(true);
          imgBaseLayer.setVisible(false);
          imgAnnoLayer.setVisible(false);
        }
        ol3d.setEnabled(is3D.value);
    if (is3D.value) {
      show3DWindow.value = true;
      waitForViewer(() => {
        console.log("进入 3D 模式，准备加载 3D 建筑...");
        preload3DBuildings();
      });
    } else {
      show3DWindow.value = false; // 切回2D时，顺手关掉3D工具箱
    }
    // 同步按钮的高亮状态
    functionState.value['view-3d'] = is3D.value;
    return; // 👈 关键：在这里直接 return，阻止后续弹窗！
  }

  // 2. 如果点击的是当前已打开的分析工具，则关闭它
  if (activeAnalysisName.value === layer.name && showAnalysisWindow.value) {
    closeAnalysisWindow();
    return;
  }

  // 3. 清理之前的状态（注意保留 view-3d 的状态，不要被误清空）
  const current3DState = functionState.value['view-3d'];
  Object.keys(functionState.value).forEach(k => functionState.value[k] = false);
  functionState.value['view-3d'] = current3DState;

  poiLayer.getSource().clear();
  routeLayer.getSource().clear();

  showBufferResultWindow.value = false;
  bufferResults.value = [];
  nearestResult.value = null;
  highlightSource.clear();
  // 4. 激活新工具并打开分析窗口
  functionState.value[layer.id] = true;
  activeAnalysisName.value = layer.name;
  showAnalysisWindow.value = true;

  if (layer.id === 'routing') {
    showAutoRoute();
  }
};
// 定义统一的 GeoServer WMS 接口地址
const GEOSERVER_WMS = 'http://localhost:8080/geoserver/wms';

const extraLayerInstances = {
  // 遥感影像不需要透明背景，使用 image/jpeg 渲染更快，体积更小
  rs2022: new TileLayer({
    source: new TileWMS({
      url: GEOSERVER_WMS,
      params: { 'LAYERS': 'yxsj:Shenyang_2022_rgb', 'TILED': true, 'FORMAT': 'image/jpeg' },
      serverType: 'geoserver',
      crossOrigin: 'anonymous' // 解决跨域引起的画布污染问题
    }),
    zIndex: 10
  }),

  rs2025: new TileLayer({
    source: new TileWMS({
      url: GEOSERVER_WMS,
      params: { 'LAYERS': 'yxsj:Shenyang_2025_rgb', 'TILED': true, 'FORMAT': 'image/jpeg' },
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    }),
    zIndex: 11
  }),

  // 土地利用分类是矢量栅格，周边有无数据区，必须使用 image/png 才能保持背景透明
  landuse2020: new TileLayer({
    source: new TileWMS({
      url: GEOSERVER_WMS,
      params: { 'LAYERS': 'sytd:sytd2020', 'TILED': true, 'FORMAT': 'image/png' },
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    }),
    zIndex: 15
  }),

  landuse2023: new TileLayer({
    source: new TileWMS({
      url: GEOSERVER_WMS,
      params: { 'LAYERS': 'sytd:ystd2023', 'TILED': true, 'FORMAT': 'image/png' },
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    }),
    zIndex: 16
  }),

  weather: new VectorLayer({ source: new VectorSource(), zIndex: 20 }),

  buildHeping: new VectorLayer({
    source: new VectorSource(),
    zIndex: 18,
    style: new Style({
      stroke: new Stroke({ color: '#888', width: 1 }),
      fill: new Fill({ color: 'rgba(200, 200, 200, 0.6)' })
    })
  })
};
const totalPages = computed(() => Math.ceil(searchResults.value.length / pageSize.value));
const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return searchResults.value.slice(start, start + pageSize.value);
});
const totalCapacitySum = computed(() => {
  if (!searchResults.value || searchResults.value.length === 0) return 0;
  return searchResults.value.reduce((sum, item) => {
    const props = safeParseProps(item.properties);
    const numValue = Number(props.capacity || props.students || props.population || 0);
    return sum + (isNaN(numValue) ? 0 : numValue);
  }, 0);
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
  vectorBaseLayer = new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=vec_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }), visible: true });
  vectorAnnoLayer = new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=cva_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }), visible: true });

  // 2. 初始化卫星影像图 (默认隐藏)
  imgBaseLayer = new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }), visible: false });
  imgAnnoLayer = new TileLayer({ source: new XYZ({ url: `http://t0.tianditu.gov.cn/DataServer?T=cia_w&x={x}&y={y}&l={z}&tk=${TDT_TK}` }), visible: false });
  map = new Map({
      target: 'map',
      layers: [
        // 👇 2. 在这里使用变量
        vectorBaseLayer,
        vectorAnnoLayer,
        imgBaseLayer,
        imgAnnoLayer,

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
    // 可达性或最近邻分析
    if (functionState.value.accessibility || functionState.value['knn-poi']) {
      let resData
    if (functionState.value.accessibility) {
            const res = await fetchAccessiblePois(lon, lat, accessibilityRadius.value)
            resData = res.data

            // 👇 【核心修复部分】：将后端返回的数据赋给结果列表，并强制显示面板
            if (resData && resData.features) {
              bufferResults.value = resData.features;
            } else if (Array.isArray(resData)) {
              bufferResults.value = resData;
            } else {
              bufferResults.value = [];
            }
            showBufferResultWindow.value = true; // 打开右侧结果面板

        } else {
          const res = await getNearestPoi(lon, lat)
          resData = res.data
          if (resData && resData.properties) {
            nearestResult.value = resData;
          } else {
            nearestResult.value = null;
          }
        }
      const source = poiLayer.getSource()
      source.clear()
      const centerFeat = new Feature({ geometry: new Point(coord) })
      centerFeat.set('isAnalysis', true)
      centerFeat.setStyle(pointStyle)

      const poiFeats = new GeoJSON().readFeatures(resData, { featureProjection: 'EPSG:3857' })
      poiFeats.forEach(f => f.set('isAnalysis', true))

      if (functionState.value.accessibility) {
         const circleGeom = circular([lon, lat], accessibilityRadius.value, 64)
                            .transform('EPSG:4326', 'EPSG:3857');
         const circleFeat = new Feature({ geometry: circleGeom });
         circleFeat.setStyle(rangeStyle);
         circleFeat.set('isAnalysis', true);
         source.addFeatures([circleFeat, centerFeat, ...poiFeats]);
      }else if (functionState.value['knn-poi']) {
                source.addFeatures([centerFeat, ...poiFeats]);}
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
      if (!highlightFeature.get('isAnalysis') && !highlightFeature.get('isWeather')) {
        highlightFeature.setStyle(undefined);
      }
      highlightFeature = null;
    }
    const feature = map.forEachFeatureAtPixel(evt.pixel, (f) => f);

    if (feature) {
      highlightFeature = feature;
      if (!feature.get('isAnalysis') && !feature.get('isWeather')) {
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
      delete props.isAnalysis;
      delete props.isWeather;
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

const toggle3DMode = () => {
  handleFunctionClick({ id: 'view-3d', name: '3D 视图模式' });
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

    if (features.length > 0) {
      let allSubLines = [];

      // 1. 拆解逻辑：处理可能存在的 MultiLineString，确保拿到的是独立的线段
      features.forEach(feature => {
        const geom = feature.getGeometry();
        if (geom.getType() === 'MultiLineString') {
          // 如果是多线段，拆解为多个单线段要素
          geom.getLineStrings().forEach(lineString => {
            allSubLines.push(new Feature(lineString));
          });
        } else {
          allSubLines.push(feature);
        }
      });

      // 2. 过滤逻辑：遍历所有线段，计算真实地理长度并找出最长的一条
      let longestFeature = null;
      let maxLen = -1;

      allSubLines.forEach(f => {
        const length = getLength(f.getGeometry()); // 使用 ol/sphere 的 getLength 计算米制长度
        if (length > maxLen) {
          maxLen = length;
          longestFeature = f;
        }
      });

      // 3. 渲染逻辑：只添加最长的那条线
      if (longestFeature) {
        routeLayer.getSource().addFeature(longestFeature);

        // 自动缩放视角到该路径
        map.getView().fit(routeLayer.getSource().getExtent(), {
          padding: [100, 100, 100, 100],
          duration: 1000
        });
      }
    }
  } catch (error) {
    console.error("过滤最长路径失败:", error);
  }
}

const locateNearestPoi = () => {
  if (!nearestResult.value || !nearestResult.value.geometry) return;

  const geomObj = nearestResult.value.geometry;
  const coords = geomObj.coordinates;

  if (is3D.value && ol3d) {
    ol3d.getCesiumScene().camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(coords[0], coords[1], 800),
      duration: 1.5
    });
  } else {
    // 2D 视角平移
    map.getView().animate({ center: fromLonLat(coords), zoom: 17, duration: 1000 });
  }
};

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
  if (is3D.value && ol3d) {
    // 3D 模式定位
    ol3d.getCesiumScene().camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(123.43, 41.80, 15000),
      orientation: { pitch: Cesium.Math.toRadians(-90) }
    })
  } else if (map) {
    // 2D 模式定位（核心：必须进行坐标转换）
    map.getView().animate({
      center: fromLonLat([123.43, 41.80]), // 将经纬度转为 Web 墨卡托投影坐标
      zoom: 14,
      duration: 1000
    });
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

// 智能 3D 建筑加载器
const handleSmart3DBuilding = () => {
  // 1. 如果当前是二维地图，后台静默帮你“点击”切换 3D 的按钮
  if (!is3D.value) {
    toggle3DMode();
  }

  // 2. 触发 3D 建筑的加载和显隐
  toggle3DBuildings();

  // 如果是关闭操作，直接返回
  if (!show3DBuildings.value) return;

  // 3. 施展“立起来”的魔法：等待引擎加载一瞬间后，自动倾斜相机视角
  setTimeout(() => {
    const scene = ol3d.getCesiumScene();
    if (scene) {
      scene.camera.flyTo({
        // 定位到和平区上方大约 1500 米处
        destination: Cesium.Cartesian3.fromDegrees(123.41, 41.79, 1500),
        orientation: {
          heading: Cesium.Math.toRadians(0), // 正北方向
          pitch: Cesium.Math.toRadians(-45), // 👇 核心：向下倾斜 45 度角，完美呈现建筑的立体感
          roll: 0.0
        },
        duration: 1.5 // 1.5 秒的平滑飞行动画
      });
    }
  }, 800); // 稍微等待 800ms 让 3D 地球完成初始化
};
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
  clearMeasure();
  measureWindowTitle.value = '🛠️ 空间量测';
  showMeasureWindow.value = true;
  measureResultText.value = type === 'LineString' ? '👉 请点击地图绘制折线...' : '👉 请点击地图绘制多边形...';

  if (!is3D.value) {
    drawInteraction = new Draw({ source: measureSource, type: type });
    drawInteraction.on('drawend', (evt) => {
      const geom = evt.feature.getGeometry();
      let output = type === 'LineString'
        ? (getLength(geom) / 1000).toFixed(2) + ' km'
        : (getArea(geom) / 1000000).toFixed(2) + ' km²';

      // 替换掉原来的 confirm
      measureResultText.value = `✅ 测量结果: ${output}`;
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
    if (positions.length < 2) {
      measureResultText.value = '⚠️ 点数不足，测量已取消';
      return;
    }

    if (type === 'LineString') {
      let distance = 0;
      for (let i = 0; i < positions.length - 1; i++) {
        distance += Cesium.Cartesian3.distance(positions[i], positions[i+1]);
      }
      measureResultText.value = `✅ 3D 测距结果: ${distance.toFixed(2)} m`;
    } else {
      const coords = positions.map(p => {
        const carto = Cesium.Cartographic.fromCartesian(p);
        return [Cesium.Math.toDegrees(carto.longitude), Cesium.Math.toDegrees(carto.latitude)];
      });
      coords.push(coords[0]);

      const polygonGeom = new Polygon([coords]);
      const area = getArea(polygonGeom, { projection: 'EPSG:4326' });
      measureResultText.value = `✅ 3D 测面积结果: ${(area / 1000000).toFixed(2)} Km²`;
    }
  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

  measureResultText.value = '👉 操作说明：左键添加点，右键结束测量';
}
const toggleExtraLayer = async (type) => {
  const layer = extraLayerInstances[type];
  if (extraLayers[type]) {
    map.addLayer(layer);

    // 如果开启的是气象图层
    if (type === 'weather') {
      refreshWeatherLayer(); // 立刻拉取一次最新数据
        map.getView().animate({
                center: fromLonLat([123.42, 41.85]),
                zoom: 9,
                duration: 1000
              });
      // 开启 15 分钟自动刷新 (15 * 60 * 1000 = 900000 毫秒)
      if (!weatherInterval) {
        weatherInterval = setInterval(() => {
          console.log("正在执行气象数据 15 分钟定时刷新...");
          refreshWeatherLayer();
        }, 900000);
      }
    }
    else if (type === 'buildHeping') {
      const source = layer.getSource();
      // 如果还没有数据，发起请求
      if (source.getFeatures().length === 0) {
        try {
          console.log("正在请求和平区 2D 建筑数据...");
          // 调用后端接口
          const res = await loadLayer({
            id: 'build_heping', name: '和平区建筑', type: 'display', table: 'build_heping'
          });

          const geojsonData = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;

          // 使用 OpenLayers 将数据转为 2D 矢量要素
          const features = new GeoJSON().readFeatures(geojsonData, {
            dataProjection: 'EPSG:4326',
            featureProjection: 'EPSG:3857' // 转换为 Web 墨卡托投影
          });

          source.addFeatures(features);
          console.log("和平区 2D 建筑加载完成！");

          // 视角自动拉近到建筑所在区域
          map.getView().fit(source.getExtent(), { duration: 1000, padding: [50, 50, 50, 50] });
        } catch (e) {
          console.error("加载 2D 建筑失败:", e);
        }
      } else {
        // 如果数据已经在缓存里了，直接飞过去
        map.getView().fit(source.getExtent(), { duration: 500, padding: [50, 50, 50, 50] });
      }
    }
  } else {
    map.removeLayer(layer);

    // 如果关闭的是气象图层，务必销毁定时器，防止后台偷跑流量
    if (type === 'weather' && weatherInterval) {
      clearInterval(weatherInterval);
      weatherInterval = null;
      console.log("气象图层已关闭，自动刷新已停止。");
    }
  }
};
const toggleSwipeMode = () => {
  if (is3D.value) {
    alert("卷帘功能仅在二维地图模式下可用，请先退出三维模式。");
    return;
  }

  isSwipeActive.value = !isSwipeActive.value;

  if (isSwipeActive.value) {
    const isLanduseMode = groupOpenState['土地利用数据'] || extraLayers.landuse2020 || extraLayers.landuse2023;

    if (isLanduseMode) {
      // --- 模式A：土地利用卷帘 ---
      // 1. 强制关闭遥感影像图层，防止底层影像穿透
      extraLayers.rs2022 = false;
      extraLayers.rs2025 = false;
      map.removeLayer(extraLayerInstances.rs2022);
      map.removeLayer(extraLayerInstances.rs2025);

      // 2. 开启土地利用图层
      extraLayers.landuse2020 = true;
      extraLayers.landuse2023 = true;
      map.removeLayer(extraLayerInstances.landuse2020);
      map.removeLayer(extraLayerInstances.landuse2023);
      map.addLayer(extraLayerInstances.landuse2020);
      map.addLayer(extraLayerInstances.landuse2023);

      map.addControl(swipeCtrl);
      swipeCtrl.addLayer(extraLayerInstances.landuse2020);       // 左侧 2020
      swipeCtrl.addLayer(extraLayerInstances.landuse2023, true); // 右侧 2023

      showMeasureWindow.value = true;
      measureWindowTitle.value = '🌗 卷帘模式';
      measureResultText.value = '当前对比：土地利用 (左 2020 vs 右 2023)';

    } else {
      // --- 模式B：遥感影像卷帘 ---
      // 1. 强制关闭土地利用图层，防止画面杂乱
      extraLayers.landuse2020 = false;
      extraLayers.landuse2023 = false;
      map.removeLayer(extraLayerInstances.landuse2020);
      map.removeLayer(extraLayerInstances.landuse2023);

      // 2. 开启遥感影像图层
      extraLayers.rs2022 = true;
      extraLayers.rs2025 = true;
      map.removeLayer(extraLayerInstances.rs2022);
      map.removeLayer(extraLayerInstances.rs2025);
      map.addLayer(extraLayerInstances.rs2022);
      map.addLayer(extraLayerInstances.rs2025);

      map.addControl(swipeCtrl);
      swipeCtrl.addLayer(extraLayerInstances.rs2022);       // 左侧 2022
      swipeCtrl.addLayer(extraLayerInstances.rs2025, true); // 右侧 2025

      showMeasureWindow.value = true;
      measureWindowTitle.value = '🌗 卷帘模式';
      measureResultText.value = '当前对比：遥感影像 (左 2022 vs 右 2025)';
    }
  } else {
    // 关闭卷帘：重置状态并移除所有可能的对比图层
    map.removeControl(swipeCtrl);
    extraLayers.rs2022 = false;
    extraLayers.rs2025 = false;
    extraLayers.landuse2020 = false;
    extraLayers.landuse2023 = false;
    map.removeLayer(extraLayerInstances.rs2022);
    map.removeLayer(extraLayerInstances.rs2025);
    map.removeLayer(extraLayerInstances.landuse2020);
    map.removeLayer(extraLayerInstances.landuse2023);
    showMeasureWindow.value = false;
  }
};
const clearAll = () => {
  // 1. 清除量测图形
  clearMeasure();

  // 2. 清除空间分析绘制点及路径
  poiLayer.getSource().clear();
  routeLayer.getSource().clear();
  highlightSource.clear();

  // 3. 关闭所有弹窗及结果面板
  bufferResults.value = [];
  showBufferResultWindow.value = false;
  showAnalysisWindow.value = false;
  showQueryWindow.value = false;

  // 4. 重置左侧分析功能激活状态
  Object.keys(functionState.value).forEach(k => functionState.value[k] = false);
  activeAnalysisName.value = '';

  // 5. 如果当前处于卷帘模式，强制退出
  if (isSwipeActive.value) {
    toggleSwipeMode();
  }
};
const locateBufferFeature = (item) => {
   if (item.geometry) {
       // 读取几何数据
       const geomObj = typeof item.geometry === 'string' ? JSON.parse(item.geometry) : item.geometry;
       const searchFeature = new Feature({
         geometry: new GeoJSON().readGeometry(geomObj, {
           dataProjection: 'EPSG:4326',
           featureProjection: 'EPSG:3857'
         })
       });

       // 添加到高亮图层
       highlightSource.clear();
       highlightSource.addFeature(searchFeature);

       // 获取中心点并平移放大
       const extent = searchFeature.getGeometry().getExtent();
       const center = [(extent[0] + extent[2]) / 2, (extent[1] + extent[3]) / 2];

       if (is3D.value && ol3d) {
         const lonLat = toLonLat(center);
         ol3d.getCesiumScene().camera.flyTo({
           destination: Cesium.Cartesian3.fromDegrees(lonLat[0], lonLat[1], 800),
           duration: 1.5
         });
       } else {
         map.getView().animate({ center: center, zoom: 17, duration: 1000 });
       }
   } else {
     alert('该设施缺失空间位置数据！');
   }
};

async function refreshWeatherLayer() {
  const source = extraLayerInstances.weather.getSource();
  source.clear(); // 清除旧数据

  console.log("🌦️ 开始同步气象数据...");

  // 封装一个自带重试机制的 fetch 函数
  const fetchWithRetry = async (url, retries = 3, delay = 2000) => {
    for (let i = 0; i < retries; i++) {
      try {
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
      } catch (e) {
        console.warn(`⚠️ 请求失败，第 ${i + 1} 次重试...`);
        if (i < retries - 1) {
          // 指数退避：每次重试多等一会儿
          await new Promise(resolve => setTimeout(resolve, delay * (i + 1)));
        } else {
          throw e; // 重试用光，向外抛出错误
        }
      }
    }
  };

  for (const station of WEATHER_STATIONS) {
    try {
      // 换回 devapi 域名（因为你是免费版 Key）
      const url = `https://m57jpj2u92.re.qweatherapi.com/v7/weather/now?location=${station.lon},${station.lat}&key=${QWEATHER_KEY}`;

      // 使用我们封装好的防刷请求器
      const data = await fetchWithRetry(url);

      if (data.code === '200') {
        const now = data.now;

        const feature = new Feature({
          geometry: new Point(fromLonLat([station.lon, station.lat])),
          name: station.name,
          temp: now.temp + '°C',
          text: now.text,
          windDir: now.windDir,
          windScale: now.windScale + '级',
          humidity: now.humidity + '%',
          pressure: now.pressure + ' hPa',
          isWeather: true
        });

        const iconUrl = `https://cdn.jsdelivr.net/npm/qweather-icons@1.4.0/icons/${now.icon}.svg`;

        feature.setStyle(new Style({
          image: new Icon({
            src: iconUrl,
            scale: 0.8,
            anchor: [0.5, 0.8]
          }),
          text: new Text({
            text: `${station.name}\n${now.temp}°C ${now.text}`,
            font: 'bold 12px sans-serif',
            fill: new Fill({ color: '#333' }),
            stroke: new Stroke({ color: '#fff', width: 3 }),
            offsetY: 20
          })
        }));

        source.addFeature(feature);
        console.log(`✅ ${station.name} 气象加载成功`);
      }
    } catch (e) {
      console.error(`❌ ${station.name} 最终获取失败:`, e);
    } finally {
      // 基础请求间隔依然保持 2000 毫秒
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }

  console.log(" 所有气象数据同步尝试完毕！");
}

function resetNorth() { map.getView().animate({ rotation: 0, duration: 500 }); }
function zoomIn() { map.getView().animate({ zoom: map.getView().getZoom() + 1, duration: 250 }); }
function zoomOut() { map.getView().animate({ zoom: map.getView().getZoom() - 1, duration: 250 }); }
function locateMe() { map.getView().animate({ center: [13741313, 5130280], zoom: 12, duration: 1000 }); }
</script>

<style scoped>
</style>