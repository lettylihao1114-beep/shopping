package com.gec.shop.qa.controller;

import com.gec.shop.qa.service.KnowledgeGraphService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 智能客服 Controller — 集成 RAG4Pro 方法论。
 * 意图识别 → 实体链接 → Cypher 模板 → Neo4j 执行 → 答案
 */
@RestController
@RequestMapping("/qa")
public class QAController {

    private final KnowledgeGraphService kgService;

    public QAController(KnowledgeGraphService kgService) {
        this.kgService = kgService;
    }

    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        return kgService.ask(q);
    }

    @GetMapping("/intent/{intent}")
    public List<Map<String, Object>> intent(@PathVariable String intent) {
        return kgService.findByIntent(intent);
    }

    @GetMapping("/similar/{pid}")
    public List<Map<String, Object>> similar(@PathVariable Long pid) {
        return kgService.similarProducts(pid);
    }

    @GetMapping("/products")
    public List<Map<String, Object>> searchProduct(@RequestParam(defaultValue = "") String kw) {
        return kgService.searchProduct(kw);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return kgService.getStats();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("service", "qa-service", "status", "UP");
    }
}
