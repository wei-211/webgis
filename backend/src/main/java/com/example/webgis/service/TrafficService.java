package com.example.webgis.service;

import com.example.webgis.repository.TrafficRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrafficService {

    @Autowired
    private TrafficRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}

