package com.jess.arms.base.delegate;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public interface ActivityDelegate {
    String LAYOUT_LINEARLAYOUT = "LinearLayout";

    String LAYOUT_FRAMELAYOUT = "FrameLayout";

    String LAYOUT_RELATIVELAYOUT = "RelativeLayout";

    String ACTIVITY_DELEGATE = "ACTIVITY_DELEGATE";

    void onCreate(@Nullable
    Bundle savedInstanceState);

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onSaveInstanceState(@NonNull
    Bundle outState);

    void onDestroy();
}

