package com.example.webgis.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "shenyang")
public class Shenyang {

    @Id
    @Column(name = "id")
    private String id;

    private String name;

    @Column(columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geom;
}
