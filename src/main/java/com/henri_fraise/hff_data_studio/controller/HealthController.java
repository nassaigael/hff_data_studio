package com.henri_fraise.hff_data_studio.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "HFF DATA STUDIO - HFF Data Intelligence Platform");
    response.put("version", "1.0.0");
    response.put("timestamp", System.currentTimeMillis());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/info")
  public ResponseEntity<Map<String, String>> info() {
    Map<String, String> response = new HashMap<>();
    response.put("name", "HFF DATA STUDIO - HFF Data Intelligence Platform");
    response.put("version", "1.0.0");
    response.put("description", "From raw data to decision, without detour");
    return ResponseEntity.ok(response);
  }
}
