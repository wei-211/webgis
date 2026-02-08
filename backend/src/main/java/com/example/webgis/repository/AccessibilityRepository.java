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
              'properties', to_jsonb(p) - 'geom'
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
                sql, String.class, lon, lat, radius
        );
    }
}

