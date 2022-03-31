package com.jess.arms.integration.lifecycle;


import ActivityEvent.CREATE;
import ActivityEvent.DESTROY;
import ActivityEvent.PAUSE;
import ActivityEvent.RESUME;
import ActivityEvent.START;
import ActivityEvent.STOP;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.trello.rxlifecycle2.android.ActivityEvent;
import dagger.Lazy;
import io.reactivex.subjects.Subject;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class ActivityLifecycleForRxLifecycle implements Application.ActivityLifecycleCallbacks {
    @Inject
    Lazy<FragmentLifecycleForRxLifecycle> mFragmentLifecycle;

    @Inject
    public ActivityLifecycleForRxLifecycle() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(CREATE);
            if (activity instanceof FragmentActivity) {
                getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycle.get(), true);
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(START);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(RESUME);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(PAUSE);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(STOP);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof ActivityLifecycleable) {
            obtainSubject(activity).onNext(DESTROY);
        }
    }

    private Subject<ActivityEvent> obtainSubject(Activity activity) {
        return ((ActivityLifecycleable) (activity)).provideLifecycleSubject();
    }
}

