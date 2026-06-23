package com.gec.shop.common;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * Day08 订单确认缓存 — 内存实现（替代 Redis）
 *
 * 借鉴 mall4cloud OrderCacheNames + Redis 两步式 confirm→submit 模式：
 * - confirm 时将订单预览数据缓存（5分钟TTL）
 * - submit 时取出并立即删除（天然防重）
 */
public class ConfirmCache {

    /** 缓存过期时间：5分钟 */
    private static final long TTL_MILLIS = 5 * 60 * 1000L;

    private static final ConcurrentHashMap<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    static {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "confirm-cache-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            CACHE.entrySet().removeIf(e -> now > e.getValue().expireAt);
        }, 60, 60, TimeUnit.SECONDS);
    }

    /** 存入缓存，返回 token */
    public static String put(Object data) {
        String token = UUID.randomUUID().toString().replace("-", "");
        CACHE.put(token, new CacheEntry(data, System.currentTimeMillis() + TTL_MILLIS));
        return token;
    }

    /** 取出并立即删除（防重：每个 token 只能用一次） */
    @SuppressWarnings("unchecked")
    public static <T> T getAndRemove(String token) {
        CacheEntry entry = CACHE.remove(token);
        if (entry == null || System.currentTimeMillis() > entry.expireAt) {
            return null;
        }
        return (T) entry.data;
    }

    public static int size() {
        return CACHE.size();
    }

    private static class CacheEntry {
        final Object data;
        final long expireAt;
        CacheEntry(Object data, long expireAt) { this.data = data; this.expireAt = expireAt; }
    }
}
