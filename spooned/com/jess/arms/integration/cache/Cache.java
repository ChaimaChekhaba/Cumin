package com.jess.arms.integration.cache;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.jess.arms.di.module.GlobalConfigModule;
import java.util.Set;


public interface Cache<K, V> {
    interface Factory {
        @NonNull
        Cache build(CacheType type);
    }

    int size();

    int getMaxSize();

    @Nullable
    V get(K key);

    @Nullable
    V put(K key, V value);

    @Nullable
    V remove(K key);

    boolean containsKey(K key);

    Set<K> keySet();

    void clear();
}

