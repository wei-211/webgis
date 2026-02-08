package com.example.webgis.controller;
import com.example.webgis.service.ShenyangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shenyang")
@CrossOrigin
public class ShenyangController {

    @Autowired
    private ShenyangService service;

    @GetMapping("/geojson")
    public String geojson() {
        return service.loadAll();
    }
}

