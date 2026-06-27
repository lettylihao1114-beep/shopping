package com.gec.shop.qa.engine;

import java.util.*;

/**
 * Cypher 模板库 — 4 类意图共 10 条模板。
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
        TEMPLATES.put(IntentRecognizer.INTENT_QUERY_ATTR, List.of(
            new Template("brand_attr",
                "MATCH (b:Brand {name: {entity}}) RETURN b.{attr} AS result",
                "查询品牌的单个属性"),
            new Template("product_attr",
                "MATCH (p:Product {name: {entity}}) RETURN p.{attr} AS result",
                "查询商品的单个属性"),
            new Template("brand_attr_fallback",
                "MATCH (b:Brand {name: {entity}}) OPTIONAL MATCH (b)-[:HAS_ATTRIBUTE]->(a:AttributeValue {name: {attr}}) RETURN COALESCE(a.value, b.{attr}) AS result",
                "查询品牌属性（含 fallback）")
        ));

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
                "获取类目下所有商品")
        ));

        TEMPLATES.put(IntentRecognizer.INTENT_COMPARE, List.of(
            new Template("compare_products",
                "MATCH (b1:Brand {name: {entity1}})<-[:BELONGS_TO_BRAND]-(p1:Product) MATCH (b2:Brand {name: {entity2}})<-[:BELONGS_TO_BRAND]-(p2:Product) RETURN COLLECT(p1.name) AS products1, COLLECT(p2.name) AS products2",
                "比较两个品牌的商品列表"),
            new Template("compare_price",
                "MATCH (p1:Product {name: {entity1}}) MATCH (p2:Product {name: {entity2}}) RETURN p1.name AS name1, p1.price AS price1, p2.name AS name2, p2.price AS price2",
                "比较两个商品的价格")
        ));

        TEMPLATES.put(IntentRecognizer.INTENT_FILTER, List.of(
            new Template("price_filter",
                "MATCH (p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN COLLECT(p.name) AS result, COLLECT(p.price) AS prices",
                "按价格区间筛选商品"),
            new Template("brand_price_filter",
                "MATCH (b:Brand {name: {entity}})<-[:BELONGS_TO_BRAND]-(p:Product) WHERE p.price >= {min_price} AND p.price <= {max_price} RETURN COLLECT(p.name) AS result",
                "筛选某品牌价格区间内的商品")
        ));
    }

    public static List<Template> getTemplates(String intent) {
        return TEMPLATES.getOrDefault(intent, Collections.emptyList());
    }

    public static String fillTemplate(String template, Map<String, Object> params) {
        String filled = template;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String placeholder = "{" + e.getKey() + "}";
            Object val = e.getValue();
            if (val instanceof String s) {
                filled = filled.replace(placeholder, "'" + s + "'");
            } else {
                filled = filled.replace(placeholder, String.valueOf(val));
            }
        }
        return filled;
    }

    public static String formatAttr(String template, String attr) {
        return template.replace("{attr}", attr);
    }
}
