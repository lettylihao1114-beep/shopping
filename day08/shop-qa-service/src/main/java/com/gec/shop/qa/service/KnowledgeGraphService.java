package com.gec.shop.qa.service;

import com.gec.shop.qa.engine.QueryEngine;
import org.neo4j.driver.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 知识图谱问答服务 — 整合 GraphBuilder + QueryEngine + JSONL 知识库。
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

        // 加载规则：JSONL 外部知识库 + 内置硬编码规则
        List<Map<String, String>> rules = new ArrayList<>();
        rules.addAll(loadJsonlRules());
        rules.addAll(buildHardcodedRules());
        engine.loadQARules(rules);

        System.out.println("[QA] QueryEngine 初始化完成: " +
            graphBuilder.getBrands().size() + " 品牌, " +
            graphBuilder.getCategories().size() + " 类目, " +
            rules.size() + " QA规则");
    }

    // ═══════════════════════════════════════
    // 问答接口
    // ═══════════════════════════════════════

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

    // ═══════════════════════════════════════
    // JSONL 知识库加载（1800 条电商 QA）
    // ═══════════════════════════════════════

    private List<Map<String, String>> loadJsonlRules() {
        List<Map<String, String>> rules = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("data/ChineseEcomQA.jsonl");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    // JSONL 格式: {"question":"...", "answer":"..."}  或 {"q":"...", "a":"..."}
                    Map<String, String> rule = parseJsonlLine(line);
                    if (rule != null) rules.add(rule);
                }
            }
            System.out.println("[QA] 从 JSONL 加载 " + rules.size() + " 条知识库规则");
        } catch (Exception e) {
            System.out.println("[QA] JSONL 知识库加载失败: " + e.getMessage() + "，仅使用内置规则");
        }
        return rules;
    }

    /** 简易 JSONL 行解析（不依赖第三方 JSON 库） */
    private Map<String, String> parseJsonlLine(String line) {
        try {
            String q = extractJsonValue(line, "question");
            if (q == null) q = extractJsonValue(line, "q");
            String a = extractJsonValue(line, "answer");
            if (a == null) a = extractJsonValue(line, "a");
            String intent = extractJsonValue(line, "intent");
            if (q != null && a != null) {
                Map<String, String> rule = new LinkedHashMap<>();
                // 取问题中最长的连续中文字段作为关键词
                rule.put("keyword", extractKeyword(q));
                rule.put("answer", a);
                rule.put("intent", intent != null ? intent : "qa");
                return rule;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx + searchKey.length());
        if (colonIdx < 0) return null;
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote)
            .replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    /** 从问题文本中提取最长的关键词（取最长连续中文字段） */
    private String extractKeyword(String question) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5\\w]{2,}")
            .matcher(question);
        String best = question; // fallback: 整句
        int maxLen = 0;
        while (m.find()) {
            String seg = m.group();
            if (seg.length() > maxLen) {
                maxLen = seg.length();
                best = seg;
            }
            // 优先匹配含产品名词的片段
            if (seg.length() >= 3 && (seg.contains("退货") || seg.contains("快递") ||
                seg.contains("退款") || seg.contains("支付") || seg.contains("推荐") ||
                seg.contains("优惠") || seg.contains("保修"))) {
                return seg;
            }
        }
        return best;
    }

    // ═══════════════════════════════════════
    // 内置硬编码规则（兜底 + 增强）
    // ═══════════════════════════════════════

    private List<Map<String, String>> buildHardcodedRules() {
        List<Map<String, String>> rules = new ArrayList<>();

        // ── 问候 ──
        rules.add(rule("你好", "您好！欢迎光临悦选商城 🛒 有什么可以帮您？", "greeting"));
        rules.add(rule("在吗", "在的！请问有什么需要帮助的吗？", "greeting"));
        rules.add(rule("hi", "Hi! Welcome to YueXuan Shopping. How can I help you?", "greeting"));
        rules.add(rule("hello", "Hello! 欢迎来到悦选，您可以问我商品价格、库存、推荐等问题~", "greeting"));
        rules.add(rule("早上好", "早上好！新的一天，来看看有什么好物吧~", "greeting"));
        rules.add(rule("你是谁", "我是悦选商城的智能客服 🤖，基于 RAG4Pro 知识图谱引擎，可以帮您查商品、比价格、推荐好物、解答售后问题！", "greeting"));

        // ── 价格 ──
        rules.add(rule("小米14Ultra多少钱", "小米14 Ultra 售价 6,999 元，旗舰影像手机", "price"));
        rules.add(rule("华为平板价格", "华为 MatePad Pro 售价 4,999 元，办公学习两不误", "price"));
        rules.add(rule("坚果礼盒", "三只松鼠坚果礼盒 88 元，节日送礼首选", "price"));
        rules.add(rule("最便宜", "目前最便宜的是 MiniLina 饼干 12.9 元、MazMaz 土豆条 13.9 元、MazMaz 番茄薯片 15.9 元", "price"));
        rules.add(rule("最贵", "目前最贵的是小米14 Ultra 6,999 元、华为 MatePad Pro 4,999 元、Air Max 跑鞋 899 元", "price"));

        // ── 退换货 / 售后 ──
        rules.add(rule("退货", "签收后 7 天内可无理由退货，保持商品完好即可，联系客服处理", "after_sales"));
        rules.add(rule("退款", "退款审核通过后 1-3 个工作日原路返回，具体到账时间以银行为准", "after_sales"));
        rules.add(rule("换货", "收货后 15 天内可申请换货，运费由我们承担", "after_sales"));
        rules.add(rule("保修", "电子产品享有一年全国联保，食品类不支持保修哦", "after_sales"));
        rules.add(rule("售后", "售后电话 400-888-6666，工作日 9:00-18:00，也可在线联系客服", "after_sales"));
        rules.add(rule("投诉", "非常抱歉给您带来不便！请通过客服电话 400-888-6666 或在线客服反馈，我们会在 24 小时内处理", "after_sales"));
        rules.add(rule("质量问题", "如商品有质量问题，请拍照联系客服，确认后可免费退换", "after_sales"));

        // ── 物流 ──
        rules.add(rule("到货", "默认中通/圆通发货，3-5 天送达，偏远地区 5-7 天", "shipping"));
        rules.add(rule("包邮", "满 99 元包邮，不满 99 元邮费 8 元", "shipping"));
        rules.add(rule("快递", "默认中通/圆通发货，顺丰需加 10 元，3-5 天送达", "shipping"));
        rules.add(rule("发货", "下单后 24 小时内发货，节假日顺延", "shipping"));
        rules.add(rule("运费", "满 99 元包邮，不满 99 元运费 8 元，顺丰加 10 元", "shipping"));

        // ── 支付 ──
        rules.add(rule("支付方式", "支持微信支付、支付宝、银行卡、花呗分期，暂不支持货到付款", "payment"));
        rules.add(rule("怎么付款", "在订单确认页面选择支付方式：微信/支付宝/银行卡/花呗分期均可", "payment"));
        rules.add(rule("分期", "花呗分期支持 3/6/12 期，手续费率 2.5%-7.5%", "payment"));

        // ── 推荐 ──
        rules.add(rule("有什么好吃的", "热门零食：三只松鼠坚果礼盒 88 元、MazMaz 番茄薯片 15.9 元、MiniLina 饼干 12.9 元、MazMaz 土豆条 13.9 元", "recommend"));
        rules.add(rule("送礼物", "送礼推荐：坚果礼盒 88 元（体面大方）、华为 MatePad Pro 4,999 元（实用高端）、北欧风台灯 159 元（温馨家居）", "recommend"));
        rules.add(rule("数码推荐", "数码好物：小米14 Ultra 6,999 元（旗舰影像）、华为 MatePad Pro 4,999 元（办公平板）", "recommend"));
        rules.add(rule("运动推荐", "运动装备：Air Max 运动跑鞋 899 元，舒适透气，跑步健身必备", "recommend"));
        rules.add(rule("学生党", "学生党推荐：MiniLina 饼干 12.9 元、MazMaz 薯片 15.9 元、纯棉T恤 129 元、北欧台灯 159 元，都是性价比之选", "recommend"));

        // ── 对比 ──
        rules.add(rule("小米和华为哪个好", "小米14 Ultra 6,999 元（旗舰影像手机）vs 华为 MatePad Pro 4,999 元（办公平板），用途不同：一个手机一个平板。拍照选小米，办公选华为！", "compare"));
        rules.add(rule("薯片和饼干", "MazMaz 番茄薯片 15.9 元（酥脆口感）vs MiniLina 饼干 12.9 元（香甜可口），喜欢咸口选薯片，甜口选饼干~", "compare"));

        // ── 优惠 ──
        rules.add(rule("优惠", "当前优惠：坚果礼盒满 2 件 9 折、MazMaz 零食买 3 免 1、新人首单减 10 元", "discount"));
        rules.add(rule("打折", "限时活动：食品类满 50 减 5、数码类满 1000 减 50，详情见首页活动页", "discount"));
        rules.add(rule("促销", "本月促销：MazMaz 薯片第 2 件半价、三只松鼠坚果买 2 送 1", "discount"));

        // ── 商品咨询 ──
        rules.add(rule("有没有小米", "有的！小米14 Ultra 6,999 元，旗舰影像手机，骁龙处理器", "product"));
        rules.add(rule("有没有华为", "有的！华为 MatePad Pro 4,999 元，办公平板，鸿蒙系统", "product"));
        rules.add(rule("卖什么", "悦选商城主营：数码（手机/平板）、运动鞋服、食品零食、家居用品，9 款精选商品等您来选~", "category"));
        rules.add(rule("有什么品类", "我们的品类包括：📱 数码、👟 运动鞋、👕 T恤、🍪 食品、🏠 家居，共 9 款精选商品", "category"));

        // ── 其他 ──
        rules.add(rule("客服电话", "客服热线：400-888-6666，工作日 9:00-18:00", "other"));
        rules.add(rule("营业时间", "在线客服工作时间：工作日 9:00-18:00，周末 10:00-17:00", "other"));
        rules.add(rule("怎么联系", "在线客服：右下角聊天窗口 / 电话：400-888-6666 / 邮箱：support@yuexuan.com", "other"));

        return rules;
    }

    private Map<String, String> rule(String keyword, String answer, String intent) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("keyword", keyword);
        r.put("answer", answer);
        r.put("intent", intent);
        return r;
    }
}
