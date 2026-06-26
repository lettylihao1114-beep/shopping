package com.gec.shop.qa.service;

import com.gec.shop.qa.engine.QueryEngine;
import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * 知识图谱问答服务 — 整合 GraphBuilder + QueryEngine。
 * 对应 RAG4Pro main.py 的编排逻辑。
 */
@Service
public class KnowledgeGraphService {

    private final Driver driver;
    private final GraphBuilderService graphBuilder;
    private QueryEngine engine;

    public KnowledgeGraphService(Driver driver, GraphBuilderService graphBuilder) {
        this.driver = driver;
        this.graphBuilder = graphBuilder;
    }

    @PreDestroy
    public void close() {
        if (driver != null) driver.close();
    }

    @PostConstruct
    public void init() {
        this.engine = new QueryEngine(driver);
        engine.buildIndex(graphBuilder.getBrands(), graphBuilder.getCategories());
        engine.loadQARules(buildQARules());
        System.out.println("[QA] QueryEngine 初始化完成: " +
            graphBuilder.getBrands().size() + " 品牌, " +
            graphBuilder.getCategories().size() + " 类目, " +
            buildQARules().size() + " QA规则");
    }

    public Map<String, Object> ask(String question) {
        return engine.answer(question);
    }

    public List<Map<String, Object>> findByIntent(String intent) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (q:Question {intent: $i}) RETURN q", Map.of("i", intent)
            ).list(r -> r.get("q").asMap());
        }
    }

    public List<Map<String, Object>> searchProduct(String keyword) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (p:Product) WHERE p.name CONTAINS $kw RETURN p LIMIT 5",
                Map.of("kw", keyword)
            ).list(r -> r.get("p").asMap());
        }
    }

    public List<Map<String, Object>> similarProducts(Long pid) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (p:Product {pid: $pid})-[:BELONGS_TO]->(c:Category)<-[:BELONGS_TO]-(other:Product) " +
                "WHERE other.pid <> $pid RETURN other LIMIT 5",
                Map.of("pid", pid)
            ).list(r -> r.get("other").asMap());
        }
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try (Session session = driver.session()) {
            stats.put("products", session.run("MATCH (p:Product) RETURN COUNT(p) AS c").single().get("c").asLong());
            stats.put("brands",   session.run("MATCH (b:Brand) RETURN COUNT(b) AS c").single().get("c").asLong());
            stats.put("categories", session.run("MATCH (c:Category) RETURN COUNT(c) AS c").single().get("c").asLong());
            stats.put("brands_index", graphBuilder.getBrands().size());
            stats.put("categories_index", graphBuilder.getCategories().size());
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    private List<Map<String, String>> buildQARules() {
        List<Map<String, String>> rules = new ArrayList<>();
        rules.add(Map.of("keyword", "你好", "answer", "您好！欢迎光临悦选商城，有什么可以帮您？", "intent", "greet"));
        rules.add(Map.of("keyword", "在吗", "answer", "在的，请问有什么需要帮助的吗？", "intent", "greet"));
        rules.add(Map.of("keyword", "小米14Ultra多少钱", "answer", "小米14 Ultra 售价 6,999", "intent", "price"));
        rules.add(Map.of("keyword", "华为平板价格",   "answer", "华为 MatePad Pro 售价 4,999", "intent", "price"));
        rules.add(Map.of("keyword", "退货", "answer", "签收后7天内可无理由退货，联系客服即可", "intent", "return"));
        rules.add(Map.of("keyword", "退款", "answer", "退款审核通过后1-3个工作日原路返回", "intent", "return"));
        rules.add(Map.of("keyword", "到货", "answer", "默认快递3-5天，偏远地区5-7天", "intent", "express"));
        rules.add(Map.of("keyword", "包邮", "answer", "满99元包邮，不满99元邮费8元", "intent", "express"));
        rules.add(Map.of("keyword", "快递", "answer", "默认中通/圆通发货，3-5天送达", "intent", "express"));
        rules.add(Map.of("keyword", "小米和华为哪个好",
            "answer", "小米14 Ultra 6,999（旗舰影像），华为 MatePad Pro 4,999（平板办公）", "intent", "compare"));
        rules.add(Map.of("keyword", "有什么好吃的",
            "answer", "坚果礼盒88、MazMaz番茄薯片15.9、MiniLina饼干12.9", "intent", "category"));
        return rules;
    }
}
