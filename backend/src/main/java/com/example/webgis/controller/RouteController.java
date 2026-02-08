package com.example.webgis.controller;
import com.example.webgis.service.RouteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
@CrossOrigin
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public String route(
            @RequestParam double slon,
            @RequestParam double slat,
            @RequestParam double elon,
            @RequestParam double elat
    ) {
        return routeService.planRoute(slon, slat, elon, elat);
    }
}

