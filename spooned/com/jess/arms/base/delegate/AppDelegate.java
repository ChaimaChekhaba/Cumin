package com.jess.arms.base.delegate;


import Application.ActivityLifecycleCallbacks;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import com.jess.arms.base.App;
import com.jess.arms.base.BaseApplication;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.integration.ConfigModule;
import com.jess.arms.integration.ManifestParser;
import com.jess.arms.integration.cache.IntelligentCache;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.Preconditions;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;


public class AppDelegate implements App , AppLifecycles {
    private Application mApplication;

    private AppComponent mAppComponent;

    @Inject
    @Named("ActivityLifecycle")
    protected ActivityLifecycleCallbacks mActivityLifecycle;

    @Inject
    @Named("ActivityLifecycleForRxLifecycle")
    protected ActivityLifecycleCallbacks mActivityLifecycleForRxLifecycle;

    private List<ConfigModule> mModules;

    private List<AppLifecycles> mAppLifecycles = new ArrayList<>();

    private List<Application.ActivityLifecycleCallbacks> mActivityLifecycles = new ArrayList<>();

    private ComponentCallbacks2 mComponentCallback;

    public AppDelegate(@NonNull
    Context context) {
        this.mModules = new ManifestParser(context).parse();
        for (ConfigModule module : mModules) {
            module.injectAppLifecycle(context, mAppLifecycles);
            module.injectActivityLifecycle(context, mActivityLifecycles);
        }
    }

    @Override
    public void attachBaseContext(@NonNull
    Context base) {
        for (AppLifecycles lifecycle : mAppLifecycles) {
            lifecycle.attachBaseContext(base);
        }
    }

    @Override
    public void onCreate(@NonNull
    Application application) {
        this.mApplication = application;
        mAppComponent = com.jess.arms.di.component.DaggerAppComponent.builder().application(mApplication).globalConfigModule(getGlobalConfigModule(mApplication, mModules)).build();
        mAppComponent.inject(this);
        mAppComponent.extras().put(((IntelligentCache.KEY_KEEP) + (ConfigModule.class.getName())), mModules);
        this.mModules = null;
        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycle);
        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycleForRxLifecycle);
        for (Application.ActivityLifecycleCallbacks lifecycle : mActivityLifecycles) {
            mApplication.registerActivityLifecycleCallbacks(lifecycle);
        }
        mComponentCallback = new AppDelegate.AppComponentCallbacks(mApplication, mAppComponent);
        mApplication.registerComponentCallbacks(mComponentCallback);
        for (AppLifecycles lifecycle : mAppLifecycles) {
            lifecycle.onCreate(mApplication);
        }
    }

    @Override
    public void onTerminate(@NonNull
    Application application) {
        if ((mActivityLifecycle) != null) {
            mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycle);
        }
        if ((mActivityLifecycleForRxLifecycle) != null) {
            mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycleForRxLifecycle);
        }
        if ((mComponentCallback) != null) {
            mApplication.unregisterComponentCallbacks(mComponentCallback);
        }
        if (((mActivityLifecycles) != null) && ((mActivityLifecycles.size()) > 0)) {
            for (Application.ActivityLifecycleCallbacks lifecycle : mActivityLifecycles) {
                mApplication.unregisterActivityLifecycleCallbacks(lifecycle);
            }
        }
        if (((mAppLifecycles) != null) && ((mAppLifecycles.size()) > 0)) {
            for (AppLifecycles lifecycle : mAppLifecycles) {
                lifecycle.onTerminate(mApplication);
            }
        }
        this.mAppComponent = null;
        this.mActivityLifecycle = null;
        this.mActivityLifecycleForRxLifecycle = null;
        this.mActivityLifecycles = null;
        this.mComponentCallback = null;
        this.mAppLifecycles = null;
        this.mApplication = null;
    }

    private GlobalConfigModule getGlobalConfigModule(Context context, List<ConfigModule> modules) {
        GlobalConfigModule.Builder builder = GlobalConfigModule.builder();
        for (ConfigModule module : modules) {
            module.applyOptions(context, builder);
        }
        return builder.build();
    }

    @NonNull
    @Override
    public AppComponent getAppComponent() {
        Preconditions.checkNotNull(mAppComponent, "%s cannot be null,first call %s#onCreate(Application) in %s#onCreate()", AppComponent.class.getName(), getClass().getName(), Application.class.getName());
        return mAppComponent;
    }

    private static class AppComponentCallbacks implements ComponentCallbacks2 {
        private Application mApplication;

        private AppComponent mAppComponent;

        public AppComponentCallbacks(Application application, AppComponent appComponent) {
            this.mApplication = application;
            this.mAppComponent = appComponent;
        }

        @Override
        public void onTrimMemory(int level) {
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override
        public void onLowMemory() {
        }
    }
}

