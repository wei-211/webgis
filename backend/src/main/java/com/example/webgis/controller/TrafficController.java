package com.example.webgis.controller;
import com.example.webgis.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin(origins = "http://localhost:5173")
public class TrafficController {

    @Autowired
    private TrafficService service;

    @GetMapping("/geojson")
    public String geojson() {
        return service.loadAll();
    }
}

