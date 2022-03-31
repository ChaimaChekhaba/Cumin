package com.jess.arms.di.module;


import Application.ActivityLifecycleCallbacks;
import FragmentManager.FragmentLifecycleCallbacks;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jess.arms.integration.ActivityLifecycle;
import com.jess.arms.integration.FragmentLifecycle;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.integration.RepositoryManager;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.CacheType;
import com.jess.arms.integration.lifecycle.ActivityLifecycleForRxLifecycle;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;


@Module
public abstract class AppModule {
    @Singleton
    @Provides
    static Gson provideGson(Application application, @Nullable
    AppModule.GsonConfiguration configuration) {
        GsonBuilder builder = new GsonBuilder();
        if (configuration != null)
            configuration.configGson(application, builder);

        return builder.create();
    }

    @Binds
    abstract IRepositoryManager bindRepositoryManager(RepositoryManager repositoryManager);

    @Singleton
    @Provides
    static Cache<String, Object> provideExtras(Cache.Factory cacheFactory) {
        return cacheFactory.build(CacheType.EXTRAS);
    }

    @Binds
    @Named("ActivityLifecycle")
    abstract ActivityLifecycleCallbacks bindActivityLifecycle(ActivityLifecycle activityLifecycle);

    @Binds
    @Named("ActivityLifecycleForRxLifecycle")
    abstract ActivityLifecycleCallbacks bindActivityLifecycleForRxLifecycle(ActivityLifecycleForRxLifecycle activityLifecycleForRxLifecycle);

    @Binds
    abstract FragmentLifecycleCallbacks bindFragmentLifecycle(FragmentLifecycle fragmentLifecycle);

    @Singleton
    @Provides
    static List<FragmentManager.FragmentLifecycleCallbacks> provideFragmentLifecycles() {
        return new ArrayList<>();
    }

    public interface GsonConfiguration {
        void configGson(Context context, GsonBuilder builder);
    }
}

