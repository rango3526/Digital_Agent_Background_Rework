package com.example.testapp2;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AppLifeCycleObserver extends Application implements LifecycleObserver {

    Context context = this;


    ///////////////////////////////////////////////
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        isAppInBackground(false);
        DataTrackingManager.appMovedToForeground();
//        Toast.makeText(context, "App enter foreground", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER FOREGROUND");
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        isAppInBackground(true);
        DataTrackingManager.appMovedToBackground();
//        Toast.makeText(context, "App enter background", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER BACKGROUND");
    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    public void onPause() {
////        isAppInBackground(true);
//        Toast.makeText(context, "App Pause", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER PAUSE");
//    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    public void onResume() {
////        isAppInBackground(true);
//        Toast.makeText(context, "App Resume", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER RESUME");
//    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    public void onCreateEvent() {
////        isAppInBackground(true);
//        Toast.makeText(context, "App Create", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER CREATE");
//    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    public void onDestroyEvent() {
////        isAppInBackground(true);
//        Toast.makeText(context, "App Destroy", Toast.LENGTH_SHORT).show();
//        Log.e("Stuff", "ENTER DESTROY");
//    }
///////////////////////////////////////////////



    // Adding some callbacks for test and log
    public interface ValueChangeListener {
        void onChanged(Boolean value);
    }
    private ValueChangeListener visibilityChangeListener;
    public void setOnVisibilityChangeListener(ValueChangeListener listener) {
        this.visibilityChangeListener = listener;
    }
    private void isAppInBackground(Boolean isBackground) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener.onChanged(isBackground);
        }
    }
    private static AppLifeCycleObserver mInstance;
    public static AppLifeCycleObserver getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("Stuff", "ON CREATE IN LIFECYCLE");

        mInstance = this;

        // addObserver
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

}
