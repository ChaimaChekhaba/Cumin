package com.jess.arms.integration;


import FragmentManager.FragmentLifecycleCallbacks;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.jess.arms.base.BaseFragment;
import com.jess.arms.base.delegate.ActivityDelegate;
import com.jess.arms.base.delegate.ActivityDelegateImpl;
import com.jess.arms.base.delegate.FragmentDelegate;
import com.jess.arms.base.delegate.IActivity;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.IntelligentCache;
import com.jess.arms.utils.Preconditions;
import dagger.Lazy;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    @Inject
    AppManager mAppManager;

    @Inject
    Application mApplication;

    @Inject
    Cache<String, Object> mExtras;

    @Inject
    Lazy<FragmentManager.FragmentLifecycleCallbacks> mFragmentLifecycle;

    @Inject
    Lazy<List<FragmentManager.FragmentLifecycleCallbacks>> mFragmentLifecycles;

    @Inject
    public ActivityLifecycle() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        boolean isNotAdd = false;
        if ((activity.getIntent()) != null)
            isNotAdd = activity.getIntent().getBooleanExtra(AppManager.IS_NOT_ADD_ACTIVITY_LIST, false);

        if (!isNotAdd)
            mAppManager.addActivity(activity);

        if (activity instanceof IActivity) {
            ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
            if (activityDelegate == null) {
                Cache<String, Object> cache = getCacheFromActivity(((IActivity) (activity)));
                activityDelegate = new ActivityDelegateImpl(activity);
                cache.put(((IntelligentCache.KEY_KEEP) + (ActivityDelegate.ACTIVITY_DELEGATE)), activityDelegate);
            }
            activityDelegate.onCreate(savedInstanceState);
        }
        registerFragmentCallbacks(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onStart();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mAppManager.setCurrentActivity(activity);
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onResume();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onPause();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if ((mAppManager.getCurrentActivity()) == activity) {
            mAppManager.setCurrentActivity(null);
        }
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onStop();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mAppManager.removeActivity(activity);
        ActivityDelegate activityDelegate = fetchActivityDelegate(activity);
        if (activityDelegate != null) {
            activityDelegate.onDestroy();
            getCacheFromActivity(((IActivity) (activity))).clear();
        }
    }

    private void registerFragmentCallbacks(Activity activity) {
        boolean useFragment = (activity instanceof IActivity) ? ((IActivity) (activity)).useFragment() : true;
        if ((activity instanceof FragmentActivity) && useFragment) {
            getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycle.get(), true);
            if (mExtras.containsKey(((IntelligentCache.KEY_KEEP) + (ConfigModule.class.getName())))) {
                List<ConfigModule> modules = ((List<ConfigModule>) (mExtras.get(((IntelligentCache.KEY_KEEP) + (ConfigModule.class.getName())))));
                for (ConfigModule module : modules) {
                    module.injectFragmentLifecycle(mApplication, mFragmentLifecycles.get());
                }
                mExtras.remove(((IntelligentCache.KEY_KEEP) + (ConfigModule.class.getName())));
            }
            for (FragmentManager.FragmentLifecycleCallbacks fragmentLifecycle : mFragmentLifecycles.get()) {
                getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycle, true);
            }
        }
    }

    private ActivityDelegate fetchActivityDelegate(Activity activity) {
        ActivityDelegate activityDelegate = null;
        if (activity instanceof IActivity) {
            Cache<String, Object> cache = getCacheFromActivity(((IActivity) (activity)));
            activityDelegate = ((ActivityDelegate) (cache.get(((IntelligentCache.KEY_KEEP) + (ActivityDelegate.ACTIVITY_DELEGATE)))));
        }
        return activityDelegate;
    }

    @NonNull
    private Cache<String, Object> getCacheFromActivity(IActivity activity) {
        Cache<String, Object> cache = activity.provideCache();
        Preconditions.checkNotNull(cache, "%s cannot be null on Activity", Cache.class.getName());
        return cache;
    }
}

