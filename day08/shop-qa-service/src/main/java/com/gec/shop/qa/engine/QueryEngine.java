package com.gec.shop.qa.engine;

import org.neo4j.driver.*;
import java.util.*;
import java.util.regex.*;

/**
 * 问答引擎 — 意图识别 → 实体链接 → 模板匹配 → Cypher 执行 → 结果格式化。
 * 对应 RAG4Pro src/query_engine.py
 */
public class QueryEngine {

    private final Driver driver;
    private final IntentRecognizer intentRecognizer;
    private final EntityLinker entityLinker;
    private final List<Map<String, String>> qaRules = new ArrayList<>();

    public QueryEngine(Driver driver) {
        this.driver = driver;
        this.intentRecognizer = new IntentRecognizer();
        this.entityLinker = new EntityLinker();
    }

    public void buildIndex(Collection<String> brands, Collection<String> categories) {
        entityLinker.buildIndex(brands, categories);
    }

    public void loadQARules(List<Map<String, String>> rules) {
        qaRules.clear();
        qaRules.addAll(rules);
    }

    /**
     * 主问答入口 — 四阶段流水线
     */
    public Map<String, Object> answer(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", query);

        // ══════ 1) QA 规则库精确匹配 ══════
        for (Map<String, String> r : qaRules) {
            String kw = r.get("keyword");
            if (kw != null && query.contains(kw)) {
                result.put("answer", r.get("answer"));
                result.put("intent", r.getOrDefault("intent", "qa"));
                result.put("method", "rule");
                return result;
            }
        }

        // ══════ 2) 意图识别 + 实体链接 ══════
        String intent = intentRecognizer.recognize(query);
        List<Trie.EntityMatch> entities = entityLinker.link(query);
        String attrName = intentRecognizer.extractAttrName(query);
        String cypherAttr = entityLinker.getCypherAttr(attrName);

        result.put("intent", intent);
        if (!entities.isEmpty()) {
            result.put("entities", entities.stream()
                .map(e -> Map.of("name", e.name, "type", e.type)).toList());
        }

        // ══════ 3) 按意图执行图谱查询 ══════
        List<Map<String, Object>> graphResult = null;
        try {
            graphResult = executeByIntent(intent, entities, cypherAttr, query);
        } catch (Exception e) {
            result.put("answer", "查询执行出错: " + e.getMessage());
            result.put("method", "error");
            return result;
        }

        if (graphResult != null && !graphResult.isEmpty() && hasResultData(graphResult)) {
            result.put("data", graphResult);
            result.put("answer", formatGraphResult(graphResult, intent, attrName));
            result.put("method", "graph");
        } else {
            // ══════ 4) 兜底：模糊搜索商品 ══════
            String searchKw = (!entities.isEmpty()) ? entities.get(0).name : query;
            Map<String, Object> product = fuzzySearchProduct(searchKw);
            if (product == null) product = fuzzySearchProduct(query);
            if (product != null) {
                result.put("answer", product.get("name") + " 售价 " + product.get("price") + " 元");
                result.put("intent", "product");
                result.put("product", product);
                result.put("method", "fuzzy_product");
            } else {
                result.put("answer", "抱歉，我还没学会这个问题。您可以问价格、库存、退换货、物流、推荐等相关问题~");
                result.put("method", "fallback");
            }
        }
        return result;
    }

    private List<Map<String, Object>> executeByIntent(
            String intent, List<Trie.EntityMatch> entities, String cypherAttr, String query) {

        List<CypherTemplates.Template> templates = CypherTemplates.getTemplates(intent);
        if (templates.isEmpty()) return null;

        // ── 有实体：按模板执行 ──
        if (!entities.isEmpty()) {
            String entityName = entities.get(0).name;
            String entityType = entities.get(0).type;

            for (CypherTemplates.Template tmpl : templates) {
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("entity", entityName);
                if (entities.size() >= 2 && intent.equals(IntentRecognizer.INTENT_COMPARE)) {
                    params.put("entity1", entities.get(0).name);
                    params.put("entity2", entities.get(1).name);
                }
                String cypher = tmpl.template;
                // 替换 {attr} 占位符
                if (cypherAttr != null && cypher.contains("{attr}")) {
                    cypher = CypherTemplates.formatAttr(cypher, cypherAttr);
                }
                // 填充所有参数
                cypher = CypherTemplates.fillTemplate(cypher, params);
                List<Map<String, Object>> data = executeCypher(cypher);
                if (data != null && hasResultData(data)) return data;
            }
        }

        // ── 无实体但有意图：走全局查询 ──
        // filter: 价格筛选
        if (intent.equals(IntentRecognizer.INTENT_FILTER)) {
            double[] range = extractPriceRange(query);
            if (range != null) {
                Map<String, Object> params = Map.of("min_price", range[0], "max_price", range[1]);
                String cypher = CypherTemplates.fillTemplate(
                    "MATCH (p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN p.name AS result, p.price AS price ORDER BY p.price ASC",
                    params);
                return executeCypher(cypher);
            }
        }

        // recommend: 全局热销
        if (intent.equals(IntentRecognizer.INTENT_RECOMMEND)) {
            return executeCypher(
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price DESC LIMIT 5");
        }

        // discount: 最优惠
        if (intent.equals(IntentRecognizer.INTENT_DISCOUNT)) {
            return executeCypher(
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price ASC LIMIT 5");
        }

        return null;
    }

    // ═══════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════

    private List<Map<String, Object>> executeCypher(String cypher) {
        try (Session session = driver.session()) {
            return session.run(cypher).list(record -> {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String key : record.keys()) {
                    row.put(key, record.get(key).asObject());
                }
                return row;
            });
        } catch (Exception e) {
            return null;
        }
    }

    /** 检查图谱结果是否有实际数据（非全 null/空列表） */
    private boolean hasResultData(List<Map<String, Object>> data) {
        for (Map<String, Object> row : data) {
            for (Object val : row.values()) {
                if (val != null && !(val instanceof List<?> list && list.isEmpty())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 模糊搜索商品：先精确→再分段 */
    private Map<String, Object> fuzzySearchProduct(String keyword) {
        try (Session session = driver.session()) {
            // 精确 CONTAINS
            var results = session.run(
                "MATCH (p:Product) WHERE p.name CONTAINS $kw RETURN p LIMIT 3",
                Map.of("kw", keyword)
            ).list(r -> r.get("p").asMap());
            if (!results.isEmpty()) return results.get(0);
            // 2-5 字滑动窗口
            for (int i = 0; i < keyword.length(); i++) {
                for (int j = i + 2; j <= Math.min(i + 5, keyword.length()); j++) {
                    String seg = keyword.substring(i, j);
                    results = session.run(
                        "MATCH (p:Product) WHERE p.name CONTAINS $kw RETURN p LIMIT 3",
                        Map.of("kw", seg)
                    ).list(r -> r.get("p").asMap());
                    if (!results.isEmpty()) return results.get(0);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 格式化图谱查询结果为自然语言 */
    private String formatGraphResult(List<Map<String, Object>> data, String intent, String attrName) {
        if (data.isEmpty()) return null;

        // ── 多行结果（filter/recommend/discount）逐一展示 ──
        if (intent.equals(IntentRecognizer.INTENT_FILTER) ||
            intent.equals(IntentRecognizer.INTENT_RECOMMEND) ||
            intent.equals(IntentRecognizer.INTENT_DISCOUNT)) {
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> row : data) {
                String name = stringVal(row.get("result"));
                Object price = row.get("price");
                if (name != null && price != null) {
                    sb.append(name).append(" ").append(price).append("元; ");
                }
            }
            if (sb.length() > 0) return sb.toString().trim();
        }

        Map<String, Object> row = data.get(0);

        // ── 对比结果格式化 ──
        if (intent.equals(IntentRecognizer.INTENT_COMPARE)) {
            StringBuilder sb = new StringBuilder();
            if (row.containsKey("result1") && row.containsKey("result2")) {
                formatListValue(sb, "品牌一商品", row.get("result1"));
                if (row.containsKey("avg_price1")) sb.append("均价").append(row.get("avg_price1")).append("; ");
                formatListValue(sb, "品牌二商品", row.get("result2"));
                if (row.containsKey("avg_price2")) sb.append("均价").append(row.get("avg_price2"));
            } else if (row.containsKey("name1") && row.containsKey("name2")) {
                sb.append(row.get("name1")).append(": ").append(row.get("price1")).append("元 vs ");
                sb.append(row.get("name2")).append(": ").append(row.get("price2")).append("元");
            }
            if (sb.length() > 0) return sb.toString();
        }

        // ── 单值结果 ──
        if (row.containsKey("result")) {
            Object val = row.get("result");
            if (val instanceof List<?> list && !list.isEmpty()) {
                return String.join("、", list.stream().map(Object::toString).toList());
            }
            if (val != null && !"null".equals(String.valueOf(val))) {
                return val.toString();
            }
            return "未找到信息";
        }

        // ── 多列结果（推荐/筛选表格） ──
        if (row.containsKey("result") || row.containsKey("name")) {
            // handled above
        }
        if (row.containsKey("shipping")) {
            return row.get("result") + " — " + row.get("shipping");
        }

        // ── 通用拼接 ──
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String key = e.getKey().replace("_", " ");
            Object val = e.getValue();
            if (val instanceof List<?> list && !list.isEmpty()) {
                sb.append("、").append(String.join(", ", list.stream().map(Object::toString).toList()));
            } else if (val != null && !"null".equals(String.valueOf(val))) {
                sb.append(val).append(" ");
            }
        }
        return sb.length() > 0 ? sb.toString().trim() : "未找到信息";
    }

    /** 安全取字符串 */
    private String stringVal(Object val) {
        if (val == null) return null;
        String s = val.toString();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private void formatListValue(StringBuilder sb, String label, Object val) {
        if (val instanceof List<?> list && !list.isEmpty()) {
            sb.append(label).append(": ").append(String.join(", ", list.stream().map(Object::toString).toList())).append("; ");
        }
    }

    /** 从问题中提取价格区间 */
    private double[] extractPriceRange(String query) {
        java.util.regex.Matcher m1 = Pattern.compile("(\\d+)\\s*元?\\s*以[内下]").matcher(query);
        if (m1.find()) return new double[]{0, Double.parseDouble(m1.group(1))};
        java.util.regex.Matcher m2 = Pattern.compile("[大于高超至少](\\d+)").matcher(query);
        if (m2.find()) return new double[]{Double.parseDouble(m2.group(1)), Double.MAX_VALUE};
        java.util.regex.Matcher m3 = Pattern.compile("(\\d+)\\s*[-~至到]\\s*(\\d+)\\s*元?").matcher(query);
        if (m3.find()) return new double[]{Double.parseDouble(m3.group(1)), Double.parseDouble(m3.group(2))};
        java.util.regex.Matcher m4 = Pattern.compile("不超过(\\d+)").matcher(query);
        if (m4.find()) return new double[]{0, Double.parseDouble(m4.group(1))};
        java.util.regex.Matcher m5 = Pattern.compile("预算(\\d+)").matcher(query);
        if (m5.find()) return new double[]{0, Double.parseDouble(m5.group(1))};
        return null;
    }
}
