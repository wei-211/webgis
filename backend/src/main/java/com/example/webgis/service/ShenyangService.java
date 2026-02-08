package com.example.webgis.service;

import com.example.webgis.repository.ShenyangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShenyangService {

    @Autowired
    private ShenyangRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}

