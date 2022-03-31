package com.jess.arms.base;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jess.arms.base.delegate.IFragment;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.CacheType;
import com.jess.arms.integration.lifecycle.FragmentLifecycleable;
import com.jess.arms.mvp.IPresenter;
import com.jess.arms.utils.ArmsUtils;
import com.trello.rxlifecycle2.android.FragmentEvent;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import javax.inject.Inject;


public abstract class BaseFragment<P extends IPresenter> extends Fragment implements IFragment , FragmentLifecycleable {
    protected final String TAG = this.getClass().getSimpleName();

    private final BehaviorSubject<FragmentEvent> mLifecycleSubject = BehaviorSubject.create();

    private Cache<String, Object> mCache;

    @Inject
    @Nullable
    protected P mPresenter;

    @NonNull
    @Override
    public synchronized Cache<String, Object> provideCache() {
        if ((mCache) == null) {
            mCache = ArmsUtils.obtainAppComponentFromContext(BaseFragment.getActivity()).cacheFactory().build(CacheType.FRAGMENT_CACHE);
        }
        return mCache;
    }

    @NonNull
    @Override
    public final Subject<FragmentEvent> provideLifecycleSubject() {
        return mLifecycleSubject;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull
    LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable
    Bundle savedInstanceState) {
        return initView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ((mPresenter) != null)
            mPresenter.onDestroy();

        this.mPresenter = null;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}

