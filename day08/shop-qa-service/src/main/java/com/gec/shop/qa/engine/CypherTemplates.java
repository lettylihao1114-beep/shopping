package com.gec.shop.qa.engine;

import java.util.*;

/**
 * Cypher 模板库 — 10 类意图共 25+ 条模板。
 * 对应 RAG4Pro src/cypher_templates.py
 */
public class CypherTemplates {

    public static class Template {
        public final String name;
        public final String template;
        public final String description;

        Template(String name, String template, String description) {
            this.name = name; this.template = template; this.description = description;
        }
    }

    private static final Map<String, List<Template>> TEMPLATES = new LinkedHashMap<>();
    static {
        // ────────── query_attr: 属性查询 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_QUERY_ATTR, List.of(
            new Template("brand_attr",
                "MATCH (b:Brand {name: {entity}}) RETURN b.{attr} AS result",
                "查询品牌的单个属性"),
            new Template("product_attr",
                "MATCH (p:Product) WHERE p.name CONTAINS {entity} RETURN p.{attr} AS result",
                "查询商品属性（模糊匹配）"),
            new Template("category_products_count",
                "MATCH (c:Category {name: {entity}})<-[:BELONGS_TO]-(p:Product) RETURN COLLECT(p.name) AS result, COLLECT(p.price) AS prices",
                "查询类目下所有商品"),
            new Template("brand_all_attrs",
                "MATCH (b:Brand {name: {entity}}) RETURN b AS result",
                "查询品牌全部属性")
        ));

        // ────────── query_relation: 关系查询 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_QUERY_RELATION, List.of(
            new Template("brand_products",
                "MATCH (b:Brand {name: {entity}})<-[:BELONGS_TO_BRAND]-(p:Product) RETURN COLLECT(p.name) AS result, COLLECT(p.price) AS prices",
                "获取品牌下所有商品"),
            new Template("brand_categories",
                "MATCH (b:Brand {name: {entity}})-[:HAS_CATEGORY]->(c:Category) RETURN COLLECT(c.name) AS result",
                "获取品牌的所有类目"),
            new Template("brand_main_business",
                "MATCH (b:Brand {name: {entity}}) RETURN b.main_business AS result",
                "获取品牌主营业务"),
            new Template("category_products",
                "MATCH (c:Category {name: {entity}})<-[:BELONGS_TO]-(p:Product) RETURN COLLECT(p.name) AS result, COLLECT(p.price) AS prices",
                "获取类目下所有商品"),
            new Template("product_brand",
                "MATCH (p:Product) WHERE p.name CONTAINS {entity} MATCH (p)-[:BELONGS_TO_BRAND]->(b:Brand) RETURN b.name AS result",
                "查询商品所属品牌")
        ));

        // ────────── recommend: 智能推荐 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_RECOMMEND, List.of(
            new Template("hot_by_category",
                "MATCH (c:Category {name: {entity}})<-[:BELONGS_TO]-(p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price DESC LIMIT 5",
                "按类目热销推荐"),
            new Template("hot_overall",
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price DESC LIMIT 5",
                "全站热销推荐"),
            new Template("brand_hot",
                "MATCH (b:Brand {name: {entity}})<-[:BELONGS_TO_BRAND]-(p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price DESC LIMIT 5",
                "品牌热销"),
            new Template("cheap_by_category",
                "MATCH (c:Category {name: {entity}})<-[:BELONGS_TO]-(p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price ASC LIMIT 5",
                "按类目性价比推荐")
        ));

        // ────────── compare: 对比 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_COMPARE, List.of(
            new Template("compare_brands",
                "MATCH (b1:Brand {name: {entity1}})<-[:BELONGS_TO_BRAND]-(p1:Product) MATCH (b2:Brand {name: {entity2}})<-[:BELONGS_TO_BRAND]-(p2:Product) RETURN COLLECT(p1.name) AS result1, AVG(p1.price) AS avg_price1, COLLECT(p2.name) AS result2, AVG(p2.price) AS avg_price2",
                "比较两个品牌的商品列表和均价"),
            new Template("compare_products",
                "MATCH (p1:Product) WHERE p1.name CONTAINS {entity1} MATCH (p2:Product) WHERE p2.name CONTAINS {entity2} RETURN p1.name AS name1, p1.price AS price1, p2.name AS name2, p2.price AS price2",
                "比较两个商品的价格")
        ));

        // ────────── filter: 筛选 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_FILTER, List.of(
            new Template("price_range",
                "MATCH (p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN p.name AS result, p.price AS price ORDER BY p.price ASC",
                "按价格区间筛选"),
            new Template("brand_price_range",
                "MATCH (b:Brand {name: {entity}})<-[:BELONGS_TO_BRAND]-(p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN p.name AS result, p.price AS price ORDER BY p.price ASC",
                "某品牌价格区间筛选"),
            new Template("category_price_range",
                "MATCH (c:Category {name: {entity}})<-[:BELONGS_TO]-(p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN p.name AS result, p.price AS price ORDER BY p.price ASC",
                "某类目价格区间筛选"),
            new Template("cheapest",
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price ASC LIMIT 5",
                "最便宜商品"),
            new Template("most_expensive",
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price DESC LIMIT 5",
                "最贵商品")
        ));

        // ────────── shipping: 物流查询 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_SHIPPING, List.of(
            new Template("shipping_info",
                "MATCH (p:Product) WHERE p.name CONTAINS {entity} RETURN p.name AS result, '默认中通/圆通, 3-5天送达, 满99包邮' AS shipping",
                "查询商品物流信息")
        ));

        // ────────── discount: 优惠查询 ──────────
        TEMPLATES.put(IntentRecognizer.INTENT_DISCOUNT, List.of(
            new Template("cheapest_5",
                "MATCH (p:Product) RETURN p.name AS result, p.price AS price ORDER BY p.price ASC LIMIT 5",
                "最优惠商品 Top5")
        ));
    }

    public static List<Template> getTemplates(String intent) {
        return TEMPLATES.getOrDefault(intent, Collections.emptyList());
    }

    /**
     * 填充模板中的参数占位符 {key} → 'value'
     */
    public static String fillTemplate(String template, Map<String, Object> params) {
        String filled = template;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String placeholder = "{" + e.getKey() + "}";
            Object val = e.getValue();
            if (val instanceof String s) {
                filled = filled.replace(placeholder, "'" + s.replace("'", "\\'") + "'");
            } else if (val instanceof Double || val instanceof Float) {
                filled = filled.replace(placeholder, String.format("%.1f", val));
            } else {
                filled = filled.replace(placeholder, String.valueOf(val));
            }
        }
        return filled;
    }

    /**
     * 替换 {attr} 占位符为具体 Cypher 属性名
     */
    public static String formatAttr(String template, String attr) {
        return template.replace("{attr}", attr);
    }
}
