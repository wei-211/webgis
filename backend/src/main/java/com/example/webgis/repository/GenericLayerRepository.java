package com.example.webgis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
@Repository
public class GenericLayerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Set<String> TABLES = Set.of(
            "build",
            "landuse1",
            "natural",
            "natural-a",
            "places",
            "places-a",
            "pofw",
            "pofw-a",
            "pois-a",
            "traffic-a",
            "transport-a",
            "water-a",
            "waterways",
            "roads_shenyang",
            "roads_shenyang_vertices_pgr"
    );

    public String load(String table) {

        if (!TABLES.contains(table)) {
            throw new RuntimeException("非法表名: " + table);
        }
        String sql;
        // 如果请求的是建筑图层，执行空间过滤
        if ("build".equals(table)) {
            sql = """
              SELECT jsonb_build_object(
                'type','FeatureCollection',
                'features',jsonb_agg(
                  jsonb_build_object(
                    'type','Feature',
                    'geometry', CAST(ST_AsGeoJSON(b.geom) AS jsonb),
                    'properties',
                    (to_jsonb(b) - 'geom') ||
                              jsonb_build_object(
                                'height',
                                COALESCE(b.height, 15)
                              )
                  )
                )
              )
              FROM build b
              WHERE ST_Intersects(
                b.geom, 
                -- 使用 ST_SetSRID 将 shenyang 表的 geom 强制设为 4326 以匹配 build 表
                (SELECT ST_SetSRID(geom, 4326) FROM shenyang WHERE name = '和平区' LIMIT 1)
              )
            """;
        } else {
            // 其他图层（如道路、水系）保持原样加载
            sql = """
                      SELECT jsonb_build_object(
                        'type','FeatureCollection',
                        'features',jsonb_agg(
                          jsonb_build_object(
                            'type','Feature',
                            'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
                            'properties',to_jsonb(t) - 'geom'
                          )
                        )
                      )
                      FROM %s t
                    """.formatted(table);
        }
            return jdbcTemplate.queryForObject(sql, String.class);
        }

}




