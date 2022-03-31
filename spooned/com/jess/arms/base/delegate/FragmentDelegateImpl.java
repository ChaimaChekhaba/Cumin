package com.jess.arms.base.delegate;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import butterknife.Unbinder;
import com.jess.arms.utils.ArmsUtils;
import timber.log.Timber;


public class FragmentDelegateImpl implements FragmentDelegate {
    private FragmentManager mFragmentManager;

    private Fragment mFragment;

    private IFragment iFragment;

    private Unbinder mUnbinder;

    public FragmentDelegateImpl(@NonNull
    FragmentManager fragmentManager, @NonNull
    Fragment fragment) {
        this.mFragmentManager = fragmentManager;
        this.mFragment = fragment;
        this.iFragment = ((IFragment) (fragment));
    }

    @Override
    public void onAttach(@NonNull
    Context context) {
    }

    @Override
    public void onCreate(@Nullable
    Bundle savedInstanceState) {
        if (iFragment.useEventBus())
            org.simple.eventbus.EventBus.getDefault().register(mFragment);

        iFragment.setupFragmentComponent(ArmsUtils.obtainAppComponentFromContext(mFragment.getActivity()));
    }

    @Override
    public void onCreateView(@Nullable
    View view, @Nullable
    Bundle savedInstanceState) {
        if (view != null)
            mUnbinder = butterknife.ButterKnife.bind(mFragment, view);

    }

    @Override
    public void onActivityCreate(@Nullable
    Bundle savedInstanceState) {
        iFragment.initData(savedInstanceState);
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
    public void onDestroyView() {
        if (((mUnbinder) != null) && ((mUnbinder) != (Unbinder.EMPTY))) {
            try {
                mUnbinder.unbind();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Timber.w(("onDestroyView: " + (e.getMessage())));
            }
        }
    }

    @Override
    public void onDestroy() {
        if (((iFragment) != null) && (iFragment.useEventBus()))
            org.simple.eventbus.EventBus.getDefault().unregister(mFragment);

        this.mUnbinder = null;
        this.mFragmentManager = null;
        this.mFragment = null;
        this.iFragment = null;
    }

    @Override
    public void onDetach() {
    }

    @Override
    public boolean isAdded() {
        return ((mFragment) != null) && (mFragment.isAdded());
    }
}

