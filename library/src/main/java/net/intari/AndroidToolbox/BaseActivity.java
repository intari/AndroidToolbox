/*
 * Copyright (c) 2017 Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com
 *
 */
package net.intari.AndroidToolbox;


/**
 * (c) Dmitriy Kazimirov, 2015-2016, e-mail:dmitriy.kazimirov@viorsan.com
 * Let's have this logic in Fragment's base class, idea based on http://chrisjenx.com/android-looper-oddness/
 * Base Activity part
 */

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.navi2.component.support.NaviAppCompatActivity;
import com.trello.navi2.Event;
import com.trello.navi2.rx.RxNavi;

import net.intari.CustomLogger.CustomLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Dmitriy Kazimirov, e-mail dmitriy.kazimirov@viorsan.com on 12.03.15.
 * idea based on http://chrisjenx.com/android-looper-oddness/
 * Crude simulation of GCD from iOS
 * and other helper tools
 */
public class BaseActivity extends NaviAppCompatActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();

    public static boolean isUiThread()
    {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }
    // UI Runnables
    private final List<Runnable> mUiRunnables = new LinkedList<Runnable>();
    private boolean mIsPaused = false;
    // ============


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //тупит Event.CREATE так что пока - не используем
        RxNavi.observe(this, Event.CREATE)
                .subscribe(bundle -> {
                    CustomLog.i(TAG,"navi Create (base) - asking for permissions...");
                    //TODO:combine observables


                });

        //Lifecycle events
        //all rx power is available
        RxNavi.observe(this,Event.START)
                //rxNavi uses dummy objects here, likely to unify handler types (some of events needs objct
                .subscribe(object -> {
                    CustomLog.i(TAG,"Starting...");
                    doBindServices();
                });

        RxNavi.observe(this,Event.STOP)
                .subscribe(object -> {
                    CustomLog.i(TAG,"Stopping...");
                });

        RxNavi.observe(this,Event.RESUME)
                .subscribe(object -> {
                    CustomLog.i(TAG,"Resuming...");
                    mIsPaused = false;
                    runQueuedUiRunnables();
                });

        RxNavi.observe(this,Event.PAUSE)
                .subscribe(object -> {
                    CustomLog.i(TAG,"Pausing...");
                    mIsPaused = true;
                });

        RxNavi.observe(this,Event.DESTROY)
                .subscribe(object -> {
                    CustomLog.v(TAG,"Destroying...");
                    doUnbindServices();
                });


    }

    /*
    @Override
    protected void onStart()
    {
        super.onStart();
        //doBindServices();
        //MainActivityPermissionsDispatcher.initGNSSWithCheck(this);
    }
    @Override
    protected void onStop()
    {
        super.onStop();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        mIsPaused = false;
        runQueuedUiRunnables();
    }

    @Override
    protected void onPause()
    {
        mIsPaused = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindServices();//also clears servicesDisposables
    }
    */

    /**
     * Is the activity paused?
     *
     * @return
     */
    public boolean isPaused()
    {
        return mIsPaused;
    }

    /**
     * Add a runnable task that can only be run during the activity being alive, things like dismissing dialogs when a background
     * task completes when the user is away from the activity.
     *
     * @param runnable runnable to run during the ui being alive.
     */
    protected void postUiRunnable(final Runnable runnable)
    {
        //CustomLog.v(TAG, "UiRunnables = " + runnable);
        if (null == runnable){
            return;
        }
        if (!mIsPaused && BaseActivityWithGNSSSupport.isUiThread())
        {
            runnable.run();
        }
        else if (!mIsPaused && !BaseActivityWithGNSSSupport.isUiThread() && this != null)
        {
            this.runOnUiThread(runnable);
        }
        else
        {
            mUiRunnables.add(runnable);
        }
    }

    /**
     * Will run any pending UiRunnables on resuming the activity
     */
    private void runQueuedUiRunnables()
    {
        if (mIsPaused) return;
        if (mUiRunnables.isEmpty()) return;

        //CustomLog.d(TAG,"UiRunnables Running");
        final Iterator<Runnable> it = mUiRunnables.iterator();
        Runnable run;
        while (it.hasNext())
        {
            run = it.next();
            run.run();
            it.remove();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Connect to our service(s)
     * Base version
     */
    protected void doBindServices() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        CustomLog.d(TAG,"Binding services...");
    }

    /**
     * Disconnect from our service(s)
     * Base version
     */
    protected void doUnbindServices() {
        CustomLog.d(TAG,"Unbinding services");
    }



}
