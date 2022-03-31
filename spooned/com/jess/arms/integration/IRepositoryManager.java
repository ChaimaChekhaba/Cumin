package com.jess.arms.integration;


import android.content.Context;
import com.jess.arms.mvp.IModel;


public interface IRepositoryManager {
    <T> T obtainRetrofitService(Class<T> service);

    <T> T obtainCacheService(Class<T> cache);

    void clearAllCache();

    Context getContext();
}

