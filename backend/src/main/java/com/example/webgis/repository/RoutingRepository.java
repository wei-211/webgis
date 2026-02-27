package com.example.webgis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoutingRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

        public String getAutoRoute() {
            // 这里使用你之前测试成功的那两个点对应的坐标，或者直接通过 ID 查询
            String sql = """
    WITH route_details AS (
        SELECT d.seq, r.wkb_geometry
        FROM pgr_dijkstra(
            'SELECT ogc_fid AS id, source, target, length_m AS cost FROM roads_shenyang',
            2, 705, 
            false
        ) AS d
        JOIN roads_shenyang r ON d.edge = r.ogc_fid
        ORDER BY d.seq
    )
    SELECT json_build_object(
        'type', 'FeatureCollection',
        'features', json_build_array(
            json_build_object(
                'type', 'Feature',
                -- 核心修改：明确从 route_details 中获取 wkb_geometry
                'geometry', ST_AsGeoJSON(ST_LineMerge(ST_Collect(route_details.wkb_geometry)))::json,
                'properties', json_build_object('type', 'route')
            )
        )
    )::text 
    FROM route_details; 
""";
            return jdbcTemplate.queryForObject(sql, String.class);
        }

 }


