package com.example.webgis.service;

import com.example.webgis.repository.PoisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PoisService {

    @Autowired
    private PoisRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}


