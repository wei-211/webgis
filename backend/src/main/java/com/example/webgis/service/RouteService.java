package com.example.webgis.service;
import com.example.webgis.repository.RouteRepository;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public String planRoute(double slon, double slat, double elon, double elat) {
        return routeRepository.simpleRoute(slon, slat, elon, elat);
    }
}
