package com.example.webgis.controller;

import com.example.webgis.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transport")
@CrossOrigin(origins = "http://localhost:5173")
public class TransportController {

    @Autowired
    private TransportService service;

    @GetMapping("/geojson")
    public String geojson() {
        return service.loadAll();
    }
}

