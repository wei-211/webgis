package com.example.webgis.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RouteAccessibilityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 计算指定半径内的道路通达性（道路总长度）
     */
    public Double calcAccessibility(double lon, double lat, double radius) {
        String sql = """
        SELECT COALESCE(SUM(ST_Length(r.geom, true)), 0)
        FROM pgr_drivingDistance(
            'SELECT fid as id, source, target, ST_Length(geom, true) as cost FROM roads',
            (SELECT id FROM roads_vertices_pgr ORDER BY the_geom <-> ST_SetSRID(ST_MakePoint(?, ?), 4326) LIMIT 1),
            ?, 
            FALSE
        ) AS dd
        JOIN roads r ON dd.edge = r.fid
    """;
        return jdbcTemplate.queryForObject(sql, Double.class, lon, lat, radius);
    }
}




