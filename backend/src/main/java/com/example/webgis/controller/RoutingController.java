package com.example.webgis.controller;

import com.example.webgis.repository.RoutingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routing")
@CrossOrigin(origins = "http://localhost:5173")
public class RoutingController {

    @Autowired
    private RoutingRepository routingRepository;

    @GetMapping("/preset")
    public String getPresetRoute() {
        return routingRepository.getAutoRoute();
    }
}


