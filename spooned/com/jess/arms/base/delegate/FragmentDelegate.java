package com.jess.arms.base.delegate;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;


public interface FragmentDelegate {
    String FRAGMENT_DELEGATE = "FRAGMENT_DELEGATE";

    void onAttach(@NonNull
    Context context);

    void onCreate(@Nullable
    Bundle savedInstanceState);

    void onCreateView(@Nullable
    View view, @Nullable
    Bundle savedInstanceState);

    void onActivityCreate(@Nullable
    Bundle savedInstanceState);

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onSaveInstanceState(@NonNull
    Bundle outState);

    void onDestroyView();

    void onDestroy();

    void onDetach();

    boolean isAdded();
}

