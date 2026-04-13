package com.example.webgis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Repository
public class GenericLayerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Set<String> TABLES = Set.of(
            "build",
            "build_heping",
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

        // 1. 三维模式：只返回和平区建筑
        if ("build_heping".equals(table)) {
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
                (SELECT ST_SetSRID(geom, 4326) FROM shenyang WHERE name = '和平区' LIMIT 1)
              )
            """;
        }
        // 2. 二维模式：返回全沈阳市建筑
        else if ("build".equals(table)) {
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
            """;
        }
        // 3. 其他图层保持原样
        else {
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

    public List<Map<String, Object>> searchInAllLayers(String keyword) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        String baseSql =  """
        SELECT 
            name, 
            ST_AsGeoJSON(geom) as geometry, 
            '%1$s' as source_table,
            -- 如果是面要素则计算面积（平方米），点线要素返回 0
            CASE 
                WHEN ST_GeometryType(geom) LIKE '%%Polygon%%' 
                THEN ROUND(ST_Area(ST_Transform(geom, 3857))::numeric, 2) 
                ELSE 0 
            END as calc_area,
            CAST((to_jsonb(t) - 'geom') AS text) as properties 
        FROM "%1$s" t 
        WHERE name LIKE ?
        """;
        for (String table : TABLES) {
            if ("build_heping".equals(table) || table.contains("vertices")) continue;

            String sql = String.format(baseSql, table);
            try {
                List<Map<String, Object>> tableResults = jdbcTemplate.queryForList(sql, "%" + keyword + "%");
                allResults.addAll(tableResults);
            } catch (Exception e) {
                // 记录日志或忽略
            }
        }
        return allResults;
    }
}




