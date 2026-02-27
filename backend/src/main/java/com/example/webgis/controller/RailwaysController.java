package com.example.webgis.controller;
import com.example.webgis.service.RailwaysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/railways")
@CrossOrigin(origins = "http://localhost:5173")
public class RailwaysController {

    @Autowired
    private RailwaysService service;

    @GetMapping("/geojson")
    public String geojson() {
        return service.loadAll();
    }
}

