package com.gec.shop.qa.service;

import org.neo4j.driver.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Service
public class KnowledgeGraphService {

    private final Driver driver;

    public KnowledgeGraphService(Driver driver) {
        this.driver = driver;
    }

    @PreDestroy
    public void close() {
        if (driver != null) driver.close();
    }

    @PostConstruct
    public void init() {
        try (Session session = driver.session()) {
            // 清空旧数据（开发环境）
            session.run("MATCH (n) DETACH DELETE n");

            // 1. 分类
            for (String cat : Arrays.asList("数码", "运动鞋", "T恤", "食品", "家居")) {
                session.run("CREATE (:Category {name: $name})", Map.of("name", cat));
            }

            // 2. 商品 + 关联分类
            String[][] products = {
                {"1", "小米14 Ultra", "6999", "数码", "xiaomi-1.png"},
                {"2", "华为 MatePad Pro", "4999", "数码", "huawei-1.png"},
                {"3", "Air Max 运动跑鞋", "899", "运动鞋", "airmax-1.png"},
                {"4", "纯棉圆领T恤", "129", "T恤", "tshirt-1.png"},
                {"5", "坚果礼盒", "88", "食品", "nuts-1.png"},
                {"6", "Maz Maz 番茄薯片", "15.9", "食品", "mazmaz番茄-1.png"},
                {"7", "Mini Lina 迷你饼干", "12.9", "食品", "minilina-1.png"},
                {"8", "Maz Maz 土豆条", "13.9", "食品", "mazmaz土豆条-1.png"},
                {"9", "北欧风台灯", "159", "家居", "lamp-1.png"},
            };
            for (String[] p : products) {
                session.run(
                    "CREATE (p:Product {pid: $pid, name: $name, price: $price, category: $cat, image: $img}) " +
                    "WITH p MATCH (c:Category {name: $cat}) CREATE (p)-[:BELONGS_TO]->(c)",
                    Map.of("pid", Long.parseLong(p[0]), "name", p[1], "price", Double.parseDouble(p[2]),
                           "cat", p[3], "img", p[4])
                );
            }

            // 3. QA 知识
            String[][] qas = {
                {"小米14Ultra多少钱", "小米14 Ultra 售价 ¥6,999", "price", "小米"},
                {"华为平板价格", "华为 MatePad Pro 售价 ¥4,999", "price", "华为"},
                {"AirMax跑鞋价格", "Air Max 运动跑鞋 ¥899", "price", "跑鞋"},
                {"零食价格", "伊朗零食 ¥12.9 起，坚果礼盒 ¥88", "price", "价格"},
                {"小米有货吗", "小米14 Ultra 库存充足", "stock", "库存"},
                {"台灯有货吗", "北欧风台灯库存充足", "stock", "库存"},
                {"怎么退货", "签收后7天内可无理由退货，联系客服即可", "return", "退货"},
                {"退款多久到账", "退款审核通过后1-3个工作日原路返回", "return", "退款"},
                {"多久到货", "默认快递3-5天，偏远地区5-7天", "express", "快递"},
                {"包邮吗", "满99元包邮，不满99元邮费8元", "express", "运费"},
                {"小米和华为哪个好", "小米14 Ultra ¥6,999（旗舰影像），华为 MatePad Pro ¥4,999（平板办公）", "compare", "对比"},
                {"有什么好吃的", "坚果礼盒¥88、Maz Maz番茄薯片¥15.9、Mini Lina饼干¥12.9", "category", "推荐"},
                {"数码产品有哪些", "小米14 Ultra ¥6,999、华为 MatePad Pro ¥4,999", "category", "数码"},
                {"你好", "您好！欢迎光临悦选商城，有什么可以帮您？", "greet", "你好"},
                {"在吗", "在的，请问有什么需要帮助的吗？", "greet", "在吗"},
            };
            for (String[] qa : qas) {
                session.run(
                    "CREATE (:Question {question: $q, answer: $a, intent: $i, keyword: $k})",
                    Map.of("q", qa[0], "a", qa[1], "i", qa[2], "k", qa[3])
                );
            }
            System.out.println(">>> Neo4j 知识图谱初始化完成：9商品 + 15问答");
        } catch (Exception e) {
            System.err.println(">>> Neo4j 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== 查询接口 ==========

    /** 关键词匹配问答 */
    public List<Map<String, Object>> searchQA(String keyword) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (q:Question) WHERE $kw CONTAINS q.keyword RETURN q LIMIT 5",
                Map.of("kw", keyword)
            ).list(r -> r.get("q").asMap());
        }
    }

    /** 按意图查 */
    public List<Map<String, Object>> findByIntent(String intent) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (q:Question {intent: $i}) RETURN q", Map.of("i", intent)
            ).list(r -> r.get("q").asMap());
        }
    }

    /** 按名称搜索商品 */
    public List<Map<String, Object>> searchProduct(String keyword) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (p:Product) WHERE p.name CONTAINS $kw RETURN p LIMIT 5",
                Map.of("kw", keyword)
            ).list(r -> r.get("p").asMap());
        }
    }

    /** 同类商品推荐 */
    public List<Map<String, Object>> similarProducts(Long pid) {
        try (Session session = driver.session()) {
            return session.run(
                "MATCH (p:Product {pid: $pid})-[:BELONGS_TO]->(c:Category)<-[:BELONGS_TO]-(other:Product) " +
                "WHERE other.pid <> $pid RETURN other",
                Map.of("pid", pid)
            ).list(r -> r.get("other").asMap());
        }
    }
}
