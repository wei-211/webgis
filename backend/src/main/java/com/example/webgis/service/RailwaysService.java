package com.example.webgis.service;

import com.example.webgis.repository.RailwaysRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RailwaysService {

    @Autowired
    private RailwaysRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}

