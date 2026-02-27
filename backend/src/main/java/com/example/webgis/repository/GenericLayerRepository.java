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

        String sql = """
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

        return jdbcTemplate.queryForObject(sql, String.class);
    }
}



