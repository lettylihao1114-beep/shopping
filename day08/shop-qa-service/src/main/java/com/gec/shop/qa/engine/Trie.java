package com.gec.shop.qa.engine;

import java.util.*;

/**
 * Trie 树 — 高效实体链接（最长前缀匹配）。
 * 对应 RAG4Pro src/trie.py
 */
public class Trie {

    static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean isEnd;
        String entityName;
        String entityType;
    }

    private final Node root = new Node();

    public void insert(String word, String entityName, String entityType) {
        Node node = root;
        for (char ch : word.toCharArray()) {
            node = node.children.computeIfAbsent(ch, k -> new Node());
        }
        node.isEnd = true;
        node.entityName = entityName != null ? entityName : word;
        node.entityType = entityType;
    }

    public MatchResult longestMatch(String text, int start) {
        Node node = root;
        int matchEnd = -1;
        String matchName = null, matchType = null;
        int i = start;
        while (i < text.length() && node.children.containsKey(text.charAt(i))) {
            node = node.children.get(text.charAt(i));
            i++;
            if (node.isEnd) {
                matchEnd = i;
                matchName = node.entityName;
                matchType = node.entityType;
            }
        }
        if (matchEnd > start) {
            return new MatchResult(matchEnd, matchName, matchType);
        }
        return new MatchResult(start + 1, null, null);
    }

    public List<EntityMatch> searchAll(String text) {
        List<EntityMatch> results = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            MatchResult mr = longestMatch(text, i);
            if (mr.name != null) {
                results.add(new EntityMatch(i, mr.end, mr.name, mr.type));
            }
            i = mr.end;
        }
        return results;
    }

    public void buildFromEntities(Map<String, String> entities) {
        for (Map.Entry<String, String> e : entities.entrySet()) {
            insert(e.getKey(), e.getKey(), e.getValue());
        }
    }

    public static class MatchResult {
        public final int end;
        public final String name;
        public final String type;
        MatchResult(int end, String name, String type) {
            this.end = end; this.name = name; this.type = type;
        }
    }

    public static class EntityMatch {
        public final int start, end;
        public final String name, type;
        EntityMatch(int start, int end, String name, String type) {
            this.start = start; this.end = end; this.name = name; this.type = type;
        }
    }
}
