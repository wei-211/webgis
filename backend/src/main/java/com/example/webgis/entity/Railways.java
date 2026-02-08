package com.example.webgis.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "railways")
public class Railways {

    @Id
    @Column(name = "fid")
    private Long fid;

    private String name;
    private String railway;
    private String bridge;
    private String tunnel;

    @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString geom;
}

