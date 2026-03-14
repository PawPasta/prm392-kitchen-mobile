package com.prm392_sp26.prm392_kitchen_mobile.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.concurrent.atomic.AtomicInteger;

public final class AppForegroundTracker implements Application.ActivityLifecycleCallbacks {

    private static AppForegroundTracker instance;
    private final AtomicInteger startedCount = new AtomicInteger(0);
    private volatile Activity currentActivity;

    private AppForegroundTracker() {
    }

    public static void init(Application application) {
        if (instance != null) {
            return;
        }
        instance = new AppForegroundTracker();
        application.registerActivityLifecycleCallbacks(instance);
    }

    public static boolean isAppInForeground() {
        return instance != null && instance.startedCount.get() > 0;
    }

    public static Activity getCurrentActivity() {
        return instance != null ? instance.currentActivity : null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        startedCount.incrementAndGet();
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        int count = startedCount.decrementAndGet();
        if (count < 0) {
            startedCount.set(0);
        }
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
}
