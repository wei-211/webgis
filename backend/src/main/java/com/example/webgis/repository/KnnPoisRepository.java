package com.example.webgis.repository;

import com.example.webgis.entity.Pois;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KnnPoisRepository extends JpaRepository<Pois, Long> {

    @Query(value = """
        SELECT jsonb_build_object(
          'type','Feature',
         'geometry', CAST(ST_AsGeoJSON(geom) AS jsonb),
          'properties',
            jsonb_build_object(
              'id', fid,
              'name', name,
              'type', fclass,
              'distance',
              ST_Distance(
                CAST(geom AS geography),
                CAST(ST_SetSRID(ST_MakePoint(?1, ?2),4326) AS geography)
              )
            )
        )
        FROM pois
        WHERE fclass IN ('hospital', 'clinic','pharmacy', 'chemist')
        ORDER BY geom <-> ST_SetSRID(ST_MakePoint(?1, ?2),4326)
        LIMIT 1
        """, nativeQuery = true)
    String findNearestPoi(double lon, double lat);
}

