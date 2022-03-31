package com.jess.arms.base.delegate;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.base.BaseFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.ActivityLifecycle;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.LruCache;


public interface IActivity {
    @NonNull
    Cache<String, Object> provideCache();

    void setupActivityComponent(@NonNull
    AppComponent appComponent);

    boolean useEventBus();

    int initView(@Nullable
    Bundle savedInstanceState);

    void initData(@Nullable
    Bundle savedInstanceState);

    boolean useFragment();
}

