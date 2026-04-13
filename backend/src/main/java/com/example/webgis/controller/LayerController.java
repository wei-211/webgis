package com.example.webgis.controller;
import com.example.webgis.repository.GenericLayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("/search")
    public ResponseEntity<?> searchFeatures(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("搜索关键字不能为空");
        }
        List<Map<String, Object>> results = repository.searchInAllLayers(keyword);
        return ResponseEntity.ok(results);
    }

}


