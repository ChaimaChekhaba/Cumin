package com.jess.arms.integration;


import android.app.Application;
import android.content.Context;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.CacheType;
import com.jess.arms.mvp.IModel;
import com.jess.arms.utils.Preconditions;
import dagger.Lazy;
import io.rx_cache2.internal.RxCache;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Retrofit;


@Singleton
public class RepositoryManager implements IRepositoryManager {
    @Inject
    Lazy<Retrofit> mRetrofit;

    @Inject
    Lazy<RxCache> mRxCache;

    @Inject
    Application mApplication;

    @Inject
    Cache.Factory mCachefactory;

    private Cache<String, Object> mRetrofitServiceCache;

    private Cache<String, Object> mCacheServiceCache;

    @Inject
    public RepositoryManager() {
    }

    @Override
    public synchronized <T> T obtainRetrofitService(Class<T> service) {
        if ((mRetrofitServiceCache) == null)
            mRetrofitServiceCache = mCachefactory.build(com.jess.arms.integration.cache.CacheType.RETROFIT_SERVICE_CACHE);

        Preconditions.checkNotNull(mRetrofitServiceCache, "Cannot return null from a Cache.Factory#build(int) method");
        T retrofitService = ((T) (mRetrofitServiceCache.get(service.getCanonicalName())));
        if (retrofitService == null) {
            retrofitService = mRetrofit.get().create(service);
            mRetrofitServiceCache.put(service.getCanonicalName(), retrofitService);
        }
        return retrofitService;
    }

    @Override
    public synchronized <T> T obtainCacheService(Class<T> cache) {
        if ((mCacheServiceCache) == null)
            mCacheServiceCache = mCachefactory.build(com.jess.arms.integration.cache.CacheType.CACHE_SERVICE_CACHE);

        Preconditions.checkNotNull(mCacheServiceCache, "Cannot return null from a Cache.Factory#build(int) method");
        T cacheService = ((T) (mCacheServiceCache.get(cache.getCanonicalName())));
        if (cacheService == null) {
            cacheService = mRxCache.get().using(cache);
            mCacheServiceCache.put(cache.getCanonicalName(), cacheService);
        }
        return cacheService;
    }

    @Override
    public void clearAllCache() {
        mRxCache.get().evictAll();
    }

    @Override
    public Context getContext() {
        return mApplication;
    }
}

