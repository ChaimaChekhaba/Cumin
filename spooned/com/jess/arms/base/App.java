package com.jess.arms.base;


import android.support.annotation.NonNull;
import com.jess.arms.di.component.AppComponent;


public interface App {
    @NonNull
    AppComponent getAppComponent();
}

