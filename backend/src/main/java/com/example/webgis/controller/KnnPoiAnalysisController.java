package com.example.webgis.controller;

import com.example.webgis.repository.KnnPoisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "http://localhost:5173")
public class KnnPoiAnalysisController {

    @Autowired
    private KnnPoisRepository poisRepository;

    @GetMapping("/nearest-poi")
    public String nearestPoi(
            @RequestParam double lon,
            @RequestParam double lat) {
        return poisRepository.findNearestPoi(lon, lat);
    }
}

