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
        SELECT jsonb_build_object(
          'type','FeatureCollection',
          'features', jsonb_agg(
            jsonb_build_object(
              'type','Feature',
              'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
              -- 修改这里：使用 || 将 distance 合并进 properties 对象中
              'properties', (to_jsonb(p) - 'geom') || jsonb_build_object(
                  'distance', ST_Distance(
                      CAST(geom AS geography),
                      CAST(ST_SetSRID(ST_MakePoint(?, ?), 4326) AS geography)
                  )
              )
            )
          )
        )
        FROM pois p
        WHERE ST_DWithin(
          geom::geography,
          ST_SetSRID(ST_MakePoint(?, ?),4326)::geography,
          ?
        )
        """;

        return jdbcTemplate.queryForObject(
                sql,
                String.class,
                lon, lat, lon, lat, radius
        );
    }
}