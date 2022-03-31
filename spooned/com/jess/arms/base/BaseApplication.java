package com.jess.arms.base;


import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import com.jess.arms.base.delegate.AppDelegate;
import com.jess.arms.base.delegate.AppLifecycles;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.Preconditions;


public class BaseApplication extends Application implements App {
    private AppLifecycles mAppDelegate;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if ((mAppDelegate) == null)
            this.mAppDelegate = new AppDelegate(base);

        this.mAppDelegate.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if ((mAppDelegate) != null)
            this.mAppDelegate.onCreate(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if ((mAppDelegate) != null)
            this.mAppDelegate.onTerminate(this);

    }

    @NonNull
    @Override
    public AppComponent getAppComponent() {
        Preconditions.checkNotNull(mAppDelegate, "%s cannot be null", AppDelegate.class.getName());
        Preconditions.checkState(((mAppDelegate) instanceof App), "%s must be implements %s", AppDelegate.class.getName(), App.class.getName());
        return ((App) (mAppDelegate)).getAppComponent();
    }
}

