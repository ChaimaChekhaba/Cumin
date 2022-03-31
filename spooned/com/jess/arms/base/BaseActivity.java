package com.jess.arms.base;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.jess.arms.base.delegate.IActivity;
import com.jess.arms.integration.cache.Cache;
import com.jess.arms.integration.cache.CacheType;
import com.jess.arms.integration.lifecycle.ActivityLifecycleable;
import com.jess.arms.mvp.IPresenter;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.ThirdViewUtil;
import com.trello.rxlifecycle2.android.ActivityEvent;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import javax.inject.Inject;

import static com.jess.arms.utils.ThirdViewUtil.convertAutoView;


public abstract class BaseActivity<P extends IPresenter> extends AppCompatActivity implements IActivity , ActivityLifecycleable {
    protected final String TAG = this.getClass().getSimpleName();

    private final BehaviorSubject<ActivityEvent> mLifecycleSubject = BehaviorSubject.create();

    private Cache<String, Object> mCache;

    private Unbinder mUnbinder;

    @Inject
    @Nullable
    protected P mPresenter;

    @NonNull
    @Override
    public synchronized Cache<String, Object> provideCache() {
        if ((mCache) == null) {
            mCache = ArmsUtils.obtainAppComponentFromContext(this).cacheFactory().build(CacheType.ACTIVITY_CACHE);
        }
        return mCache;
    }

    @NonNull
    @Override
    public final Subject<ActivityEvent> provideLifecycleSubject() {
        return mLifecycleSubject;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = convertAutoView(name, context, attrs);
        return view == null ? super.onCreateView(name, context, attrs) : view;
    }

    @Override
    protected void onCreate(@Nullable
    Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            int layoutResID = initView(savedInstanceState);
            if (layoutResID != 0) {
                BaseActivity.setContentView(layoutResID);
                mUnbinder = ButterKnife.bind(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initData(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (((mUnbinder) != null) && ((mUnbinder) != (Unbinder.EMPTY)))
            mUnbinder.unbind();

        this.mUnbinder = null;
        if ((mPresenter) != null)
            mPresenter.onDestroy();

        this.mPresenter = null;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean useFragment() {
        return true;
    }
}

