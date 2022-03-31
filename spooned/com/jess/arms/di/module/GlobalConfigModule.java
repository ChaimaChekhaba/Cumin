package com.jess.arms.di.module;


import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.jess.arms.http.BaseUrl;
import com.jess.arms.http.GlobalHttpHandler;
import com.jess.arms.http.imageloader.BaseImageLoaderStrategy;
import com.jess.arms.http.imageloader.glide.GlideImageLoaderStrategy;
import com.jess.arms.http.log.DefaultFormatPrinter;
import com.jess.arms.http.log.FormatPrinter;
import com.jess.arms.http.log.RequestInterceptor;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.CacheType;
import com.jess.arms.integration.cache.IntelligentCache;
import com.jess.arms.integration.cache.LruCache;
import com.jess.arms.utils.DataHelper;
import com.jess.arms.utils.Preconditions;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;

import static com.jess.arms.http.log.RequestInterceptor.Level.ALL;


@Module
public class GlobalConfigModule {
    private HttpUrl mApiUrl;

    private BaseUrl mBaseUrl;

    private BaseImageLoaderStrategy mLoaderStrategy;

    private GlobalHttpHandler mHandler;

    private List<Interceptor> mInterceptors;

    private ResponseErrorListener mErrorListener;

    private File mCacheFile;

    private ClientModule.RetrofitConfiguration mRetrofitConfiguration;

    private ClientModule.OkhttpConfiguration mOkhttpConfiguration;

    private ClientModule.RxCacheConfiguration mRxCacheConfiguration;

    private AppModule.GsonConfiguration mGsonConfiguration;

    private RequestInterceptor.Level mPrintHttpLogLevel;

    private FormatPrinter mFormatPrinter;

    private Cache.Factory mCacheFactory;

    private GlobalConfigModule(GlobalConfigModule.Builder builder) {
        this.mApiUrl = builder.apiUrl;
        this.mBaseUrl = builder.baseUrl;
        this.mLoaderStrategy = builder.loaderStrategy;
        this.mHandler = builder.handler;
        this.mInterceptors = builder.interceptors;
        this.mErrorListener = builder.responseErrorListener;
        this.mCacheFile = builder.cacheFile;
        this.mRetrofitConfiguration = builder.retrofitConfiguration;
        this.mOkhttpConfiguration = builder.okhttpConfiguration;
        this.mRxCacheConfiguration = builder.rxCacheConfiguration;
        this.mGsonConfiguration = builder.gsonConfiguration;
        this.mPrintHttpLogLevel = builder.printHttpLogLevel;
        this.mFormatPrinter = builder.formatPrinter;
        this.mCacheFactory = builder.cacheFactory;
    }

    public static GlobalConfigModule.Builder builder() {
        return new GlobalConfigModule.Builder();
    }

    @Singleton
    @Provides
    @Nullable
    List<Interceptor> provideInterceptors() {
        return mInterceptors;
    }

    @Singleton
    @Provides
    HttpUrl provideBaseUrl() {
        if ((mBaseUrl) != null) {
            HttpUrl httpUrl = mBaseUrl.url();
            if (httpUrl != null) {
                return httpUrl;
            }
        }
        return (mApiUrl) == null ? HttpUrl.parse("https://api.github.com/") : mApiUrl;
    }

    @Singleton
    @Provides
    BaseImageLoaderStrategy provideImageLoaderStrategy() {
        return (mLoaderStrategy) == null ? new GlideImageLoaderStrategy() : mLoaderStrategy;
    }

    @Singleton
    @Provides
    @Nullable
    GlobalHttpHandler provideGlobalHttpHandler() {
        return mHandler;
    }

    @Singleton
    @Provides
    File provideCacheFile(Application application) {
        return (mCacheFile) == null ? DataHelper.getCacheFile(application) : mCacheFile;
    }

    @Singleton
    @Provides
    ResponseErrorListener provideResponseErrorListener() {
        return (mErrorListener) == null ? ResponseErrorListener.EMPTY : mErrorListener;
    }

    @Singleton
    @Provides
    @Nullable
    ClientModule.RetrofitConfiguration provideRetrofitConfiguration() {
        return mRetrofitConfiguration;
    }

    @Singleton
    @Provides
    @Nullable
    ClientModule.OkhttpConfiguration provideOkhttpConfiguration() {
        return mOkhttpConfiguration;
    }

    @Singleton
    @Provides
    @Nullable
    ClientModule.RxCacheConfiguration provideRxCacheConfiguration() {
        return mRxCacheConfiguration;
    }

    @Singleton
    @Provides
    @Nullable
    AppModule.GsonConfiguration provideGsonConfiguration() {
        return mGsonConfiguration;
    }

    @Singleton
    @Provides
    RequestInterceptor.Level providePrintHttpLogLevel() {
        return (mPrintHttpLogLevel) == null ? ALL : mPrintHttpLogLevel;
    }

    @Singleton
    @Provides
    FormatPrinter provideFormatPrinter() {
        return (mFormatPrinter) == null ? new DefaultFormatPrinter() : mFormatPrinter;
    }

    @Singleton
    @Provides
    Cache.Factory provideCacheFactory(Application application) {
        return (mCacheFactory) == null ? new Cache.Factory() {
            @NonNull
            @Override
            public Cache build(CacheType type) {
                switch (type.getCacheTypeId()) {
                    case CacheType.EXTRAS_TYPE_ID :
                    case CacheType.ACTIVITY_CACHE_TYPE_ID :
                    case CacheType.FRAGMENT_CACHE_TYPE_ID :
                        return new IntelligentCache(type.calculateCacheSize(application));
                    default :
                        return new LruCache(type.calculateCacheSize(application));
                }
            }
        } : mCacheFactory;
    }

    public static final class Builder {
        private HttpUrl apiUrl;

        private BaseUrl baseUrl;

        private BaseImageLoaderStrategy loaderStrategy;

        private GlobalHttpHandler handler;

        private List<Interceptor> interceptors;

        private ResponseErrorListener responseErrorListener;

        private File cacheFile;

        private ClientModule.RetrofitConfiguration retrofitConfiguration;

        private ClientModule.OkhttpConfiguration okhttpConfiguration;

        private ClientModule.RxCacheConfiguration rxCacheConfiguration;

        private AppModule.GsonConfiguration gsonConfiguration;

        private RequestInterceptor.Level printHttpLogLevel;

        private FormatPrinter formatPrinter;

        private Cache.Factory cacheFactory;

        private Builder() {
        }

        public GlobalConfigModule.Builder baseurl(String baseUrl) {
            if (TextUtils.isEmpty(baseUrl)) {
                throw new NullPointerException("BaseUrl can not be empty");
            }
            this.apiUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public GlobalConfigModule.Builder baseurl(BaseUrl baseUrl) {
            this.baseUrl = Preconditions.checkNotNull(baseUrl, ((BaseUrl.class.getCanonicalName()) + "can not be null."));
            return this;
        }

        public GlobalConfigModule.Builder imageLoaderStrategy(BaseImageLoaderStrategy loaderStrategy) {
            this.loaderStrategy = loaderStrategy;
            return this;
        }

        public GlobalConfigModule.Builder globalHttpHandler(GlobalHttpHandler handler) {
            this.handler = handler;
            return this;
        }

        public GlobalConfigModule.Builder addInterceptor(Interceptor interceptor) {
            if ((interceptors) == null)
                interceptors = new java.util.ArrayList();

            this.interceptors.add(interceptor);
            return this;
        }

        public GlobalConfigModule.Builder responseErrorListener(ResponseErrorListener listener) {
            this.responseErrorListener = listener;
            return this;
        }

        public GlobalConfigModule.Builder cacheFile(File cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }

        public GlobalConfigModule.Builder retrofitConfiguration(ClientModule.RetrofitConfiguration retrofitConfiguration) {
            this.retrofitConfiguration = retrofitConfiguration;
            return this;
        }

        public GlobalConfigModule.Builder okhttpConfiguration(ClientModule.OkhttpConfiguration okhttpConfiguration) {
            this.okhttpConfiguration = okhttpConfiguration;
            return this;
        }

        public GlobalConfigModule.Builder rxCacheConfiguration(ClientModule.RxCacheConfiguration rxCacheConfiguration) {
            this.rxCacheConfiguration = rxCacheConfiguration;
            return this;
        }

        public GlobalConfigModule.Builder gsonConfiguration(AppModule.GsonConfiguration gsonConfiguration) {
            this.gsonConfiguration = gsonConfiguration;
            return this;
        }

        public GlobalConfigModule.Builder printHttpLogLevel(RequestInterceptor.Level printHttpLogLevel) {
            this.printHttpLogLevel = Preconditions.checkNotNull(printHttpLogLevel, "The printHttpLogLevel can not be null, use RequestInterceptor.Level.NONE instead.");
            return this;
        }

        public GlobalConfigModule.Builder formatPrinter(FormatPrinter formatPrinter) {
            this.formatPrinter = Preconditions.checkNotNull(formatPrinter, ((FormatPrinter.class.getCanonicalName()) + "can not be null."));
            return this;
        }

        public GlobalConfigModule.Builder cacheFactory(Cache.Factory cacheFactory) {
            this.cacheFactory = cacheFactory;
            return this;
        }

        public GlobalConfigModule build() {
            return new GlobalConfigModule(this);
        }
    }
}

