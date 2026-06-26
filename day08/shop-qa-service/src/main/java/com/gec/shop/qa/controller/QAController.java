package com.gec.shop.qa.controller;

import com.gec.shop.qa.service.KnowledgeGraphService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/qa")
public class QAController {

    private final KnowledgeGraphService kgService;

    public QAController(KnowledgeGraphService kgService) {
        this.kgService = kgService;
    }

    /** 智能问答 GET /qa/ask?q=小米多少钱 */
    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", q);

        // 1. 匹配 QA 库
        List<Map<String, Object>> matches = kgService.searchQA(q);
        if (!matches.isEmpty()) {
            Map<String, Object> best = matches.get(0);
            result.put("answer", best.get("answer"));
            result.put("intent", best.get("intent"));
            result.put("matchedQuestion", best.get("question"));
            return result;
        }

        // 2. 匹配商品
        List<Map<String, Object>> products = kgService.searchProduct(q);
        if (!products.isEmpty()) {
            Map<String, Object> p = products.get(0);
            result.put("answer", p.get("name") + " 售价 ¥" + p.get("price") + "，库存充足");
            result.put("intent", "product");
            result.put("product", Map.of("name", p.get("name"), "price", p.get("price"), "image", p.get("image")));
            return result;
        }

        // 3. 兜底
        result.put("answer", "抱歉，我还没学会这个问题。您可以问价格、库存、退换货、物流等相关问题~");
        result.put("intent", "unknown");
        return result;
    }

    /** 按意图查 GET /qa/intent/price */
    @GetMapping("/intent/{intent}")
    public List<Map<String, Object>> intent(@PathVariable String intent) {
        return kgService.findByIntent(intent);
    }

    /** 同类推荐 GET /qa/similar/1 */
    @GetMapping("/similar/{pid}")
    public List<Map<String, Object>> similar(@PathVariable Long pid) {
        return kgService.similarProducts(pid);
    }
}
