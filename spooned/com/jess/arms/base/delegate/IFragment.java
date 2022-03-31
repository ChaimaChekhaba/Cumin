package com.jess.arms.base.delegate;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jess.arms.base.BaseFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.LruCache;


public interface IFragment {
    @NonNull
    Cache<String, Object> provideCache();

    void setupFragmentComponent(@NonNull
    AppComponent appComponent);

    boolean useEventBus();

    View initView(@NonNull
    LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState);

    void initData(@Nullable
    Bundle savedInstanceState);

    void setData(@Nullable
    Object data);
}

