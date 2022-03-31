package com.jess.arms.integration;


import Intent.FLAG_ACTIVITY_NEW_TASK;
import android.R.id.content;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Message;
import android.os.Process;
import android.support.design.widget.Snackbar;
import android.view.View;
import com.jess.arms.base.delegate.AppLifecycles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;
import timber.log.Timber;


@Singleton
public final class AppManager {
    protected final String TAG = this.getClass().getSimpleName();

    public static final String APPMANAGER_MESSAGE = "appmanager_message";

    public static final String IS_NOT_ADD_ACTIVITY_LIST = "is_not_add_activity_list";

    public static final int START_ACTIVITY = 5000;

    public static final int SHOW_SNACKBAR = 5001;

    public static final int KILL_ALL = 5002;

    public static final int APP_EXIT = 5003;

    @Inject
    Application mApplication;

    private List<Activity> mActivityList;

    private Activity mCurrentActivity;

    private AppManager.HandleListener mHandleListener;

    @Inject
    public AppManager() {
    }

    @Inject
    void init() {
        EventBus.getDefault().register(this);
    }

    @Subscriber(tag = AppManager.APPMANAGER_MESSAGE, mode = ThreadMode.MAIN)
    public void onReceive(Message message) {
        switch (message.what) {
            case AppManager.START_ACTIVITY :
                if ((message.obj) == null)
                    break;

                dispatchStart(message);
                break;
            case AppManager.SHOW_SNACKBAR :
                if ((message.obj) == null)
                    break;

                showSnackbar(((String) (message.obj)), ((message.arg1) == 0 ? false : true));
                break;
            case AppManager.KILL_ALL :
                killAll();
                break;
            case AppManager.APP_EXIT :
                appExit();
                break;
            default :
                Timber.tag(TAG).w("The message.what not match");
                break;
        }
        if ((mHandleListener) != null) {
            mHandleListener.handleMessage(this, message);
        }
    }

    private void dispatchStart(Message message) {
        if ((message.obj) instanceof Intent)
            startActivity(((Intent) (message.obj)));
        else
            if ((message.obj) instanceof Class)
                startActivity(((Class) (message.obj)));


    }

    public AppManager.HandleListener getHandleListener() {
        return mHandleListener;
    }

    public void setHandleListener(AppManager.HandleListener handleListener) {
        this.mHandleListener = handleListener;
    }

    public static void post(Message msg) {
        EventBus.getDefault().post(msg, AppManager.APPMANAGER_MESSAGE);
    }

    public void showSnackbar(String message, boolean isLong) {
        if ((getCurrentActivity()) == null) {
            Timber.tag(TAG).w("mCurrentActivity == null when showSnackbar(String,boolean)");
            return;
        }
        View view = getCurrentActivity().getWindow().getDecorView().findViewById(content);
        Snackbar.make(view, message, (isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT)).show();
    }

    public void startActivity(Intent intent) {
        if ((getTopActivity()) == null) {
            Timber.tag(TAG).w("mCurrentActivity == null when startActivity(Intent)");
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            mApplication.startActivity(intent);
            return;
        }
        getTopActivity().startActivity(intent);
    }

    public void startActivity(Class activityClass) {
        startActivity(new Intent(mApplication, activityClass));
    }

    public void release() {
        EventBus.getDefault().unregister(this);
        mActivityList.clear();
        mHandleListener = null;
        mActivityList = null;
        mCurrentActivity = null;
        mApplication = null;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.mCurrentActivity = currentActivity;
    }

    public Activity getCurrentActivity() {
        return (mCurrentActivity) != null ? mCurrentActivity : null;
    }

    public Activity getTopActivity() {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when getTopActivity()");
            return null;
        }
        return (mActivityList.size()) > 0 ? mActivityList.get(((mActivityList.size()) - 1)) : null;
    }

    public List<Activity> getActivityList() {
        if ((mActivityList) == null) {
            mActivityList = new LinkedList();
        }
        return mActivityList;
    }

    public void addActivity(Activity activity) {
        synchronized(AppManager.class) {
            List<Activity> activities = getActivityList();
            if (!(activities.contains(activity))) {
                activities.add(activity);
            }
        }
    }

    public void removeActivity(Activity activity) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when removeActivity(Activity)");
            return;
        }
        synchronized(AppManager.class) {
            if (mActivityList.contains(activity)) {
                mActivityList.remove(activity);
            }
        }
    }

    public Activity removeActivity(int location) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when removeActivity(int)");
            return null;
        }
        synchronized(AppManager.class) {
            if ((location > 0) && (location < (mActivityList.size()))) {
                return mActivityList.remove(location);
            }
        }
        return null;
    }

    public void killActivity(Class<?> activityClass) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when killActivity(Class)");
            return;
        }
        synchronized(AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                if (next.getClass().equals(activityClass)) {
                    iterator.remove();
                    next.finish();
                }
            } 
        }
    }

    public boolean activityInstanceIsLive(Activity activity) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when activityInstanceIsLive(Activity)");
            return false;
        }
        return mActivityList.contains(activity);
    }

    public boolean activityClassIsLive(Class<?> activityClass) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when activityClassIsLive(Class)");
            return false;
        }
        for (Activity activity : mActivityList) {
            if (activity.getClass().equals(activityClass)) {
                return true;
            }
        }
        return false;
    }

    public Activity findActivity(Class<?> activityClass) {
        if ((mActivityList) == null) {
            Timber.tag(TAG).w("mActivityList == null when findActivity(Class)");
            return null;
        }
        for (Activity activity : mActivityList) {
            if (activity.getClass().equals(activityClass)) {
                return activity;
            }
        }
        return null;
    }

    public void killAll() {
        synchronized(AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                iterator.remove();
                next.finish();
            } 
        }
    }

    public void killAll(Class<?>... excludeActivityClasses) {
        List<Class<?>> excludeList = Arrays.asList(excludeActivityClasses);
        synchronized(AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                if (excludeList.contains(next.getClass()))
                    continue;

                iterator.remove();
                next.finish();
            } 
        }
    }

    public void killAll(String... excludeActivityName) {
        List<String> excludeList = Arrays.asList(excludeActivityName);
        synchronized(AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                if (excludeList.contains(next.getClass().getName()))
                    continue;

                iterator.remove();
                next.finish();
            } 
        }
    }

    public void appExit() {
        try {
            killAll();
            Process.killProcess(Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface HandleListener {
        void handleMessage(AppManager appManager, Message message);
    }
}

