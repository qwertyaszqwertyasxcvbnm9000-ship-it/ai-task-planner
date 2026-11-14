package com.taskplanner.controller;

import com.taskplanner.service.OpenSourceAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final OpenSourceAiService aiService;

    public AiController(OpenSourceAiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAiStatus() {
        boolean available = aiService.isOllamaAvailable();
        Map<String, Object> status = new HashMap<>();
        status.put("ollamaAvailable", available);
        status.put("activeModel", aiService.getOllamaModel());
        status.put("ollamaUrl", aiService.getOllamaUrl());
        status.put("engineFallback", "JAVA_BEHAVIORAL_HEURISTICS");
        status.put("modeDescription", available
                ? "Connected to local Ollama (" + aiService.getOllamaModel() + "). Full neural scheduling active."
                : "Ollama not running. Pure Java Circadian Behavioral heuristics engine active seamlessly.");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, String> config) {
        if (config.containsKey("model")) {
            aiService.setOllamaModel(config.get("model"));
        }
        if (config.containsKey("url")) {
            aiService.setOllamaUrl(config.get("url"));
        }
        return getAiStatus();
    }
}
