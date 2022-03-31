package com.jess.arms.base.delegate;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.jess.arms.utils.ArmsUtils;
import org.simple.eventbus.EventBus;


public class ActivityDelegateImpl implements ActivityDelegate {
    private Activity mActivity;

    private IActivity iActivity;

    public ActivityDelegateImpl(@NonNull
    Activity activity) {
        this.mActivity = activity;
        this.iActivity = ((IActivity) (activity));
    }

    @Override
    public void onCreate(@Nullable
    Bundle savedInstanceState) {
        if (iActivity.useEventBus()) {
            EventBus.getDefault().register(mActivity);
        }
        iActivity.setupActivityComponent(ArmsUtils.obtainAppComponentFromContext(mActivity));
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onSaveInstanceState(@NonNull
    Bundle outState) {
    }

    @Override
    public void onDestroy() {
        if (((iActivity) != null) && (iActivity.useEventBus()))
            EventBus.getDefault().unregister(mActivity);

        this.iActivity = null;
        this.mActivity = null;
    }
}

