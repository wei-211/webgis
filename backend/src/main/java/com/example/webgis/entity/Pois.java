package com.example.webgis.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;
@Entity
@Table(name = "pois")
public class Pois {

    @Id
    @Column(name = "fid")
    private Long fid;

    private String name;
    private String fclass;
    private String osm_id;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;
}

