# 沈阳市空间数据管理 WebGIS 系统 (Shenyang WebGIS)

![Vue3](https://img.shields.io/badge/Vue-3.2-4FC08D?style=flat-square&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.0-6DB33F?style=flat-square&logo=springboot)
![PostGIS](https://img.shields.io/badge/PostGIS-3.1-336791?style=flat-square&logo=postgresql)
![Cesium](https://img.shields.io/badge/Cesium-1.102-61AEE8?style=flat-square)

## 📖 项目简介

本项目是一套面向沈阳市核心区域（如和平区等）的二三维一体化 WebGIS 空间展示与分析系统。系统依托 Vue3 与 Spring Boot 前后端分离架构，探索低成本、高效率的三维城市数字孪生 Web 构建方案，赋能城市体检、防灾减灾应急响应，以及科学的商业选址与供地管理分析。

## ✨ 核心功能

- **二三维一体化联动**：支持 2D 矢量底座与 3D 虚拟地球的平滑同步切换，实现基于高程数据的 2.5D 建筑多边形向 3D 实体的动态拉伸渲染。
- **高性能空间数据流转**：利用 PostgreSQL/PostGIS 的原生 `JSONB` 特性，在数据库层完成 GeoJSON 序列化，实现海量城市要素流的低损耗直通下发。
- **三维物理仿真推演**：
  - **洪水淹没模拟**：基于 `Cesium.CallbackProperty` 闭包机制，跳过实体频繁销毁重构，流畅推演水位动态上涨与漫延过程。
  - **日照阴影分析**：通过时空参数转换注入引擎内部时钟系统（`JulianDate`），动态计算并渲染建筑物随时间推移的投射阴影。
- **空间拓扑与测度分析**：
  - **最近邻 (KNN) 检索**：利用 GiST 空间索引与 `<->` 操作符，实现毫秒级兴趣点（POI）精准定位。
  - **可达性/缓冲区评估**：快速计算并提取指定坐标辐射范围内的公共设施分布。
  - **最优路径规划**：基于 pgRouting 引擎在真实路网中求解两点间的最短拓扑路径，并于前端绘制平滑导航轨迹。

## 🛠️ 技术栈选型

### 🎨 前端 (Frontend)
- **框架与构建**: Vue 3 (Composition API), Vite, Node.js
- **网络通信**: Axios
- **GIS 渲染引擎**: 
  - OpenLayers 7 (2D 地图底座与矢量要素绘制)
  - Cesium 1.102 (3D 球体渲染与物理仿真)
  - ol-cesium (二三维视图桥接与相机同步)

### ⚙️ 后端 (Backend)
- **核心框架**: Spring Boot 3.0, JDK 17, Maven
- **空间数据库**: PostgreSQL 14 + PostGIS 3.1 + pgRouting

## 🚀 快速启动

### 1. 数据库环境准备
安装 PostgreSQL 并启用 PostGIS 与 pgRouting 扩展。将沈阳市的基础空间数据（如道路 `roads`、建筑 `build`、水系及兴趣点等）导入对应的空间数据表中。

### 2. 后端服务运行
进入后端工程目录，编译并启动服务：
```bash
mvn clean install
mvn spring-boot:run
```
后端基础服务默认运行在 http://localhost:8082

### 3. 前端服务启动
进入前端项目根目录，安装依赖并启动开发服务器：
```Bash
npm install
npm run dev
```
前端 Axios 请求已统一拦截并路由至 /api 前缀进行代理转发

## 🔗 核心 API 接口契约
系统采用标准化 RESTful 架构，前后端数据交换严格遵循 GeoJSON 规范 ：
```
GET /api/layer/{table}：泛型图层加载与矢量要素下发 
GET /api/analysis/nearest-poi：基于拾取坐标的 KNN 最近邻空间计算 
GET /api/accessibility/pois：指定辐射半径的 POI 缓冲区测度 
GET /api/route：基于起终点坐标的拓扑路径规划 
GET /api/routing/preset：系统预设巡航路线获取 
```
## 本项目作为本科毕业设计（论文）的配套工程代码，未经许可请勿用于商业用途。

© 2026 魏光义 (大连理工大学城市学院)
