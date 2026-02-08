package com.example.webgis.service;

import com.example.webgis.repository.TransportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransportService {

    @Autowired
    private TransportRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}

