package com.example.webgis.controller;

import com.example.webgis.service.AccessibilityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accessibility")
@CrossOrigin(origins = "http://localhost:5173")
public class AccessibilityController {

    private final AccessibilityService accessibilityService;

    public AccessibilityController(AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    /**
     * 兴趣点可达性分析
     */
    @GetMapping("/pois")
    public String accessiblePois(
            @RequestParam double lon,
            @RequestParam double lat,
            @RequestParam(defaultValue = "1000") double radius
    ) {
        return accessibilityService.findAccessiblePois(lon, lat, radius);
    }
}

