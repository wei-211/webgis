package com.example.webgis.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RouteRepository {

    private final JdbcTemplate jdbcTemplate;

    public RouteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public String simpleRoute(double slon, double slat, double elon, double elat) {

        String sql = """
        WITH
        start_pt AS (
          SELECT ST_SetSRID(ST_MakePoint(?, ?),4326) AS geom
        ),
        end_pt AS (
          SELECT ST_SetSRID(ST_MakePoint(?, ?),4326) AS geom
        ),
        nearest_road AS (
          SELECT geom
          FROM transport
          ORDER BY geom <-> (SELECT geom FROM start_pt)
          LIMIT 1
        ),
        route_line AS (
          SELECT ST_MakeLine(
            (SELECT geom FROM start_pt),
            (SELECT geom FROM end_pt)
          ) AS geom
        )
        SELECT jsonb_build_object(
          'type','FeatureCollection',
          'features', jsonb_build_array(
            jsonb_build_object(
              'type','Feature',
              'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
              'properties', jsonb_build_object('type','route')
            )
          )
        )
        FROM route_line
        """;

        return jdbcTemplate.queryForObject(
                sql,
                String.class,
                slon, slat, elon, elat
        );
    }
}
