package com.example.webgis.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccessibilityRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccessibilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String bufferAnalysis(double lon, double lat, double radius) {

        String sql = """
        -- 提前将各自表的属性打包成 JSONB，避开列数不一致的问题
        WITH all_pois AS (
            SELECT 
                geom, 
                fclass, 
                (to_jsonb(pois) - 'geom') AS props 
            FROM pois
            UNION ALL
            SELECT 
                geom, 
                fclass, 
                (to_jsonb("pois-a") - 'geom') AS props 
            FROM "pois-a"
        )
        SELECT jsonb_build_object(
          'type','FeatureCollection',
          'features', jsonb_agg(
            jsonb_build_object(
              'type','Feature',
              'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
              -- 这里直接使用上面打包好的 props
              'properties', p.props || jsonb_build_object(
                  'distance', ST_Distance(
                      CAST(geom AS geography),
                      CAST(ST_SetSRID(ST_MakePoint(?, ?), 4326) AS geography)
                  )
              )
            )
          )
        )
        FROM all_pois p
        WHERE ST_DWithin(
          geom::geography,
          ST_SetSRID(ST_MakePoint(?, ?),4326)::geography,
          ?
        )
        AND p.fclass IN ('school', 'university', 'college', 'kindergarten')
        """;
        return jdbcTemplate.queryForObject(
                sql,
                String.class,
                lon, lat, lon, lat, radius
        );
    }
}