package com.example.webgis.service;

import com.example.webgis.repository.RoadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoadsService {

    @Autowired
    private RoadsRepository repository;

    public String loadAll() {
        return repository.findAllGeoJSON();
    }
}

