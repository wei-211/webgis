'''沈阳市空间数据管理 WebGIS 系统 (Shenyang WebGIS)'''
'''📖 项目简介'''
'''本项目是一套面向沈阳市核心区域（如和平区等）的二三维一体化 WebGIS 空间展示与分析系统 。
系统依托 Vue3 与 Spring Boot 前后端分离架构
旨在探索低成本、高效率的三维城市数字孪生 Web 构建方案
赋能城市体检、防灾减灾应急响应，以及科学的商业选址与供地管理分析 。'''
'''✨ 核心功能'''
'''二三维一体化联动：支持 2D 矢量底座与 3D 虚拟地球的无缝同步切换，实现 2.5D 建筑多边形结合高程数据的 3D 实体动态拉伸渲染 。
高性能空间数据流转：避开传统 ORM 框架的内存损耗，基于 PostGIS 原生 JSONB 特性，实现海量城市模型与 GeoJSON 要素流的零损耗直通下发 。
高级三维物理仿真：洪水淹没推演：利用 Cesium.CallbackProperty 闭包机制，跳过实体销毁重构，实现 60FPS 流畅度的水位动态上涨与灾害模拟动画 。
日照阴影模拟：通过时空参数转换注入引擎时钟系统，实时计算并渲染建筑物随时间推移的投射阴影 。
空间计算与拓扑分析：最近邻 (KNN) 查询：利用数据库 GiST 空间索引与 <-> 操作符，实现毫秒级的兴趣点（POI）精准检索 。
可达性与缓冲区分析：快速测度地块周边指定辐射范围内的公共设施分布 。
最短路径规划：基于 pgRouting 引擎与 Dijkstra 算法，在千万级真实路网拓扑中求解，并在前端绘制平滑缝合的连续导航路线 。'''
'''🛠️ 技术栈'''
'''前端 (Frontend)核心框架:
Vue 3.2 (Composition API), Vite 4.0, Node.js 18 
网络请求: Axios GIS 与渲染引擎: OpenLayers 7.2 (负责 2D 矢量切片)
Cesium 1.102 (负责 3D 渲染), ol-cesium 2.13 (负责引擎桥接与视角同步) 
后端 (Backend)核心框架: Spring Boot 3.0, JDK 17 (LTS), Maven 3.8 
持久层与空间数据库: PostgreSQL 14, PostGIS 3.1 空间扩展模块, pgRouting 3.2 拓扑引擎 '''
'''🚀 快速开始'''
'''1. 空间数据库准备安装 PostgreSQL 并启用 PostGIS 与 pgRouting 插件。
导入沈阳市路网 (roads_shenyang)、建筑物 (Build)、水系 (Waterways) 及兴趣点 (Pois) 等核心基础空间要素表 。
2. 后端服务启动进入后端项目根目录，通过 Maven 构建并运行：Bashmvn clean install
mvn spring-boot:run
后端基础服务默认运行在 http://localhost:8082 。
3. 前端服务启动进入前端项目根目录，安装依赖并启动开发服务器：
   npm install
   npm run dev
前端 Axios 请求已统一拦截并路由至 /api 前缀进行代理转发 。'''
'''🔗 核心 API 接口契约'''
'''系统采用标准化 RESTful 架构，前后端数据交换严格遵循 GeoJSON 规范 ：
GET /api/layer/{table}：泛型图层加载与矢量要素下发
GET /api/analysis/nearest-poi：基于拾取坐标的 KNN 最近邻空间计算
GET /api/accessibility/pois：指定辐射半径的 POI 缓冲区测度
GET /api/route：基于起终点坐标的拓扑路径规划
GET /api/routing/preset：系统预设巡航路线获取 '''
'''版权与维护(JIST wei)'''
