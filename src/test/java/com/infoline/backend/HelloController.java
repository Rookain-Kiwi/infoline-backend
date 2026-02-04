package com.infoline.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {
    
    @Value("${app.message.welcome:Hello World}")
    private String welcomeMessage;
    
    @Value("${app.message.description:API Backend}")
    private String description;
    
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", welcomeMessage);
        response.put("description", description);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "running");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "infoline-backend");
        return response;
    }
}