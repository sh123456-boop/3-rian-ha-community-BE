package com.ktb.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/v1/healthz")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok("ok");
    }
}
