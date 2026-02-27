package com.example.webgis.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.LineString;
@Entity
@Table(name = "roads")
public class Roads {

    @Id
    @Column(name = "fid")
    private Long fid;

    private String name;
    private String fclass;
    private Integer maxspeed;
    private String oneway;
    private String bridge;
    private String tunnel;
    private String ref;
    private Integer source;
    private Integer target;
    private Double length_M;
    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString geom;
}

