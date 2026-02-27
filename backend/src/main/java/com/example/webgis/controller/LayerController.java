package com.example.webgis.controller;
import com.example.webgis.repository.GenericLayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/layer")
@CrossOrigin(origins = "http://localhost:5173")
public class LayerController {

    @Autowired
    private GenericLayerRepository repository;

    @GetMapping("/{table}")
    public String layer(@PathVariable String table) {
        return repository.load(table);
    }
}


