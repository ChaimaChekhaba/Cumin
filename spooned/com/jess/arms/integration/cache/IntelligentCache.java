package com.jess.arms.integration.cache;


import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class IntelligentCache<V> implements Cache<String, V> {
    private final Map<String, V> mMap;

    private final Cache<String, V> mCache;

    public static final String KEY_KEEP = "Keep=";

    public IntelligentCache(int size) {
        this.mMap = new HashMap<>();
        this.mCache = new LruCache<>(size);
    }

    @Override
    public int size() {
        return (mMap.size()) + (mCache.size());
    }

    @Override
    public int getMaxSize() {
        return (mMap.size()) + (mCache.getMaxSize());
    }

    @Nullable
    @Override
    public V get(String key) {
        if (key.startsWith(IntelligentCache.KEY_KEEP)) {
            return mMap.get(key);
        }
        return mCache.get(key);
    }

    @Nullable
    @Override
    public V put(String key, V value) {
        if (key.startsWith(IntelligentCache.KEY_KEEP)) {
            return mMap.put(key, value);
        }
        return mCache.put(key, value);
    }

    @Nullable
    @Override
    public V remove(String key) {
        if (key.startsWith(IntelligentCache.KEY_KEEP)) {
            return mMap.remove(key);
        }
        return mCache.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        if (key.startsWith(IntelligentCache.KEY_KEEP)) {
            return mMap.containsKey(key);
        }
        return mCache.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        Set<String> set = mCache.keySet();
        set.addAll(mMap.keySet());
        return set;
    }

    @Override
    public void clear() {
        mCache.clear();
        mMap.clear();
    }
}

