package com.num.controller.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class SynchronizedLruCache<K, V> extends LinkedHashMap<K, V> {
    private int capacity;
    private RemovalCallback callback;

    public SynchronizedLruCache(int capacity, RemovalCallback callback) {
        super(capacity + 1, 1, true);
        this.capacity = capacity;
        this.callback = callback;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        if (size() > capacity) {
            callback.remove(eldest);
            return true;
        }
        return false;
    }

    public static interface RemovalCallback<K, V> {
        public void remove(Entry<K, V> eldest);
    }
}
