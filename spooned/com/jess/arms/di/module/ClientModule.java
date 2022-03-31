package com.jess.arms.di.module;


import Retrofit.Builder;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.jess.arms.http.GlobalHttpHandler;
import com.jess.arms.http.log.RequestInterceptor;
import com.jess.arms.utils.DataHelper;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public abstract class ClientModule {
    private static final int TIME_OUT = 10;

    @Singleton
    @Provides
    static Retrofit provideRetrofit(Application application, @Nullable
    ClientModule.RetrofitConfiguration configuration, Retrofit.Builder builder, OkHttpClient client, HttpUrl httpUrl, Gson gson) {
        builder.baseUrl(httpUrl).client(client);
        if (configuration != null)
            configuration.configRetrofit(application, builder);

        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(gson));
        return builder.build();
    }

    @Singleton
    @Provides
    static OkHttpClient provideClient(Application application, @Nullable
    ClientModule.OkhttpConfiguration configuration, OkHttpClient.Builder builder, Interceptor intercept, @Nullable
    List<Interceptor> interceptors, @Nullable
    GlobalHttpHandler handler) {
        builder.connectTimeout(ClientModule.TIME_OUT, TimeUnit.SECONDS).readTimeout(ClientModule.TIME_OUT, TimeUnit.SECONDS).addNetworkInterceptor(intercept);
        if (handler != null)
            builder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                    return chain.proceed(handler.onHttpRequestBefore(chain, chain.request()));
                }
            });

        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        if (configuration != null)
            configuration.configOkhttp(application, builder);

        return builder.build();
    }

    @Singleton
    @Provides
    static Builder provideRetrofitBuilder() {
        return new Retrofit.Builder();
    }

    @Singleton
    @Provides
    static OkHttpClient.Builder provideClientBuilder() {
        return new OkHttpClient.Builder();
    }

    @Binds
    abstract Interceptor bindInterceptor(RequestInterceptor interceptor);

    @Singleton
    @Provides
    static RxCache provideRxCache(Application application, @Nullable
    ClientModule.RxCacheConfiguration configuration, @Named("RxCacheDirectory")
    File cacheDirectory) {
        RxCache.Builder builder = new RxCache.Builder();
        RxCache rxCache = null;
        if (configuration != null) {
            rxCache = configuration.configRxCache(application, builder);
        }
        if (rxCache != null)
            return rxCache;

        return builder.persistence(cacheDirectory, new GsonSpeaker());
    }

    @Singleton
    @Provides
    @Named("RxCacheDirectory")
    static File provideRxCacheDirectory(File cacheDir) {
        File cacheDirectory = new File(cacheDir, "RxCache");
        return DataHelper.makeDirs(cacheDirectory);
    }

    @Singleton
    @Provides
    static RxErrorHandler proRxErrorHandler(Application application, ResponseErrorListener listener) {
        return RxErrorHandler.builder().with(application).responseErrorListener(listener).build();
    }

    public interface RetrofitConfiguration {
        void configRetrofit(Context context, Retrofit.Builder builder);
    }

    public interface OkhttpConfiguration {
        void configOkhttp(Context context, OkHttpClient.Builder builder);
    }

    public interface RxCacheConfiguration {
        RxCache configRxCache(Context context, RxCache.Builder builder);
    }
}

