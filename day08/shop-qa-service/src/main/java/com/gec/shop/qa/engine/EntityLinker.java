package com.gec.shop.qa.engine;

import java.util.*;

/**
 * 实体链接器 — 基于 Trie 进行品牌/类目匹配。
 * 对应 RAG4Pro src/query_parser.py EntityLinker
 */
public class EntityLinker {

    private final Trie trie = new Trie();
    private final Map<String, String> entityMap = new HashMap<>();
    /** 类目同义词 → 规范名 */
    private static final Map<String, String> CATEGORY_SYNONYMS = new LinkedHashMap<>();
    static {
        CATEGORY_SYNONYMS.put("零食", "食品");
        CATEGORY_SYNONYMS.put("吃的", "食品");
        CATEGORY_SYNONYMS.put("食物", "食品");
        CATEGORY_SYNONYMS.put("美食", "食品");
        CATEGORY_SYNONYMS.put("小吃", "食品");
        CATEGORY_SYNONYMS.put("电子产品", "数码");
        CATEGORY_SYNONYMS.put("3C", "数码");
        CATEGORY_SYNONYMS.put("手机", "数码");
        CATEGORY_SYNONYMS.put("平板", "数码");
        CATEGORY_SYNONYMS.put("电脑", "数码");
        CATEGORY_SYNONYMS.put("球鞋", "运动鞋");
        CATEGORY_SYNONYMS.put("跑鞋", "运动鞋");
        CATEGORY_SYNONYMS.put("运动装备", "运动鞋");
        CATEGORY_SYNONYMS.put("鞋子", "运动鞋");
        CATEGORY_SYNONYMS.put("衣服", "T恤");
        CATEGORY_SYNONYMS.put("服装", "T恤");
        CATEGORY_SYNONYMS.put("上衣", "T恤");
        CATEGORY_SYNONYMS.put("穿搭", "T恤");
        CATEGORY_SYNONYMS.put("家具", "家居");
        CATEGORY_SYNONYMS.put("家用", "家居");
        CATEGORY_SYNONYMS.put("日用品", "家居");
    }

    private static final Map<String, String> ATTR_MAP = new LinkedHashMap<>();
    static {
        ATTR_MAP.put("主营业务", "main_business");
        ATTR_MAP.put("品牌发源地", "origin");
        ATTR_MAP.put("创立年份", "founded_year");
        ATTR_MAP.put("产品", "name");
        ATTR_MAP.put("类目", "name");
        ATTR_MAP.put("尺寸", "size");
        ATTR_MAP.put("颜色", "color");
        ATTR_MAP.put("价格", "price");
        ATTR_MAP.put("库存", "stock");
        ATTR_MAP.put("重量", "weight");
        ATTR_MAP.put("材质", "material");
        ATTR_MAP.put("评价", "rating");
    }

    public void buildIndex(Collection<String> brands, Collection<String> categories) {
        entityMap.clear();
        for (String b : brands) {
            entityMap.put(b, "Brand");
            trie.insert(b, b, "Brand");
        }
        for (String c : categories) {
            entityMap.put(c, "Category");
            trie.insert(c, c, "Category");
        }
    }

    public List<Trie.EntityMatch> link(String text) {
        List<Trie.EntityMatch> matches = trie.searchAll(text);
        List<Trie.EntityMatch> resolved = new ArrayList<>();
        for (Trie.EntityMatch m : matches) {
            if ("Category".equals(m.type) && CATEGORY_SYNONYMS.containsKey(m.name)) {
                resolved.add(new Trie.EntityMatch(m.start, m.end, CATEGORY_SYNONYMS.get(m.name), m.type));
            } else {
                resolved.add(m);
            }
        }
        return resolved;
    }

    public String getCypherAttr(String attrName) {
        return ATTR_MAP.getOrDefault(attrName, attrName);
    }

    public String getEntityType(String name) {
        return entityMap.get(name);
    }
}
