package com.formation.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Gestion Formation API");
        info.put("version", "1.0.0");
        info.put("endpoints", Map.of(
            "students", "/api/etudiants",
            "courses", "/api/cours",
            "enrollments", "/api/inscriptions",
            "grades", "/api/grades"
        ));
        return ResponseEntity.ok(info);
    }
}

