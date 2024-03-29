package com.jess.arms.base;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import org.simple.eventbus.EventBus;


public abstract class BaseService extends Service {
    protected final String TAG = this.getClass().getSimpleName();

    protected CompositeDisposable mCompositeDisposable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unDispose();
        this.mCompositeDisposable = null;
    }

    protected void addDispose(Disposable disposable) {
        if ((mCompositeDisposable) == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    protected void unDispose() {
        if ((mCompositeDisposable) != null) {
            mCompositeDisposable.clear();
        }
    }

    public abstract void init();
}

