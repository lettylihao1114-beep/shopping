package com.gec.shop.qa.engine;

import java.util.*;

/**
 * 实体链接器 — 基于 Trie 进行品牌/类目匹配。
 * 对应 RAG4Pro src/query_parser.py EntityLinker
 */
public class EntityLinker {

    private final Trie trie = new Trie();
    private final Map<String, String> entityMap = new HashMap<>();

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
        return trie.searchAll(text);
    }

    public String getCypherAttr(String attrName) {
        return ATTR_MAP.getOrDefault(attrName, attrName);
    }

    public String getEntityType(String name) {
        return entityMap.get(name);
    }
}
