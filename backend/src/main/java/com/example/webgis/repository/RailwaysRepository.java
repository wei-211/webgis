package com.example.webgis.repository;

import com.example.webgis.entity.Railways;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RailwaysRepository extends JpaRepository<Railways, Long> {

    @Query(value = """
      SELECT jsonb_build_object(
        'type','FeatureCollection',
        'features',jsonb_agg(
          jsonb_build_object(
            'type','Feature',
            'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
            'properties',to_jsonb(r) - 'geom'
          )
        )
      )
      FROM railways r
    """, nativeQuery = true)
    String findAllGeoJSON();
}

