package com.example.webgis.repository;

import com.example.webgis.entity.Pois;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PoisRepository extends JpaRepository<Pois, Long> {

    @Query(value = """
      SELECT jsonb_build_object(
        'type','FeatureCollection',
        'features',jsonb_agg(
          jsonb_build_object(
            'type','Feature',
            'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
            'properties',to_jsonb(p) - 'geom'
          )
        )
      )
      FROM pois p
    """, nativeQuery = true)
    String findAllGeoJSON();
}

