package com.example.webgis.service;

import com.example.webgis.repository.AccessibilityRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessibilityService {

    private final AccessibilityRepository accessibilityRepository;

    public AccessibilityService(AccessibilityRepository accessibilityRepository) {
        this.accessibilityRepository = accessibilityRepository;
    }

    public String findAccessiblePois(double lon, double lat, double radius) {
        return accessibilityRepository.bufferAnalysis(lon, lat, radius);
    }
}

