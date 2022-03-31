package com.jess.arms.base.delegate;


import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;


public interface AppLifecycles {
    void attachBaseContext(@NonNull
    Context base);

    void onCreate(@NonNull
    Application application);

    void onTerminate(@NonNull
    Application application);
}

