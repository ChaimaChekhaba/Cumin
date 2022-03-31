package com.jess.arms.integration;


import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import com.jess.arms.base.delegate.AppLifecycles;
import com.jess.arms.di.module.GlobalConfigModule;
import java.util.List;


public interface ConfigModule {
    void applyOptions(Context context, GlobalConfigModule.Builder builder);

    void injectAppLifecycle(Context context, List<AppLifecycles> lifecycles);

    void injectActivityLifecycle(Context context, List<Application.ActivityLifecycleCallbacks> lifecycles);

    void injectFragmentLifecycle(Context context, List<FragmentManager.FragmentLifecycleCallbacks> lifecycles);
}

