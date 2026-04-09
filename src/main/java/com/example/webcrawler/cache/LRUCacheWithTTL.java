package com.example.webcrawler.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheWithTTL {

    private final int capacity;
    private final long ttlMillis;

    private final Map<String, Long> cache;

    public LRUCacheWithTTL(int capacity, long ttlMillis) {
        this.capacity = capacity;
        this.ttlMillis = ttlMillis;

        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized boolean contains(String key) {
        if (!cache.containsKey(key)) return false;

        long timestamp = cache.get(key);
        if (System.currentTimeMillis() - timestamp > ttlMillis) {
            cache.remove(key);
            return false;
        }

        return true;
    }

    public synchronized void put(String key) {
        cache.put(key, System.currentTimeMillis());
    }
}