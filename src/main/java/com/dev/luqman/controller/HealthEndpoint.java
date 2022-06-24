package com.dev.luqman.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthEndpoint {

    @GetMapping
    public Map<String, String> getHealthCheck() {
        return Map.of("msg" , "success", "status" , "ok");
    }
}
