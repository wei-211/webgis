package com.example.webgis.controller;

import com.example.webgis.repository.RouteAccessibilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "http://localhost:5173")
public class RouteAccessibilityController {

    @Autowired
    private RouteAccessibilityRepository repository;

    @GetMapping("/route-accessibility")
    public Double routeAccessibility(
            @RequestParam double lon,
            @RequestParam double lat
    ) {
        return repository.calcAccessibility(lon, lat, 2000);
    }
}


