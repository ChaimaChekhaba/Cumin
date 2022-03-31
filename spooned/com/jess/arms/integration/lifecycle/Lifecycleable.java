package com.jess.arms.integration.lifecycle;


import android.support.annotation.NonNull;
import com.jess.arms.utils.RxLifecycleUtils;
import io.reactivex.subjects.Subject;


public interface Lifecycleable<E> {
    @NonNull
    Subject<E> provideLifecycleSubject();
}

