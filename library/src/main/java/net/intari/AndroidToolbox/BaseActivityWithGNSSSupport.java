package net.intari.AndroidToolbox;


/**
 * (c) Dmitriy Kazimirov, 2015-2016, e-mail:dmitriy.kazimirov@viorsan.com
 * Let's have this logic in Fragment's base class, idea based on http://chrisjenx.com/android-looper-oddness/
 * GNSS support
 */

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.navi2.Event;
import com.trello.navi2.rx.RxNavi;

import net.intari.CustomLogger.CustomLog;


import io.reactivex.disposables.CompositeDisposable;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Dmitriy Kazimirov, e-mail dmitriy.kazimirov@viorsan.com on 12.03.15.
 * idea based on http://chrisjenx.com/android-looper-oddness/
 * Crude simulation of GCD from iOS
 * and other helper tools
 *
 */
public class BaseActivityWithGNSSSupport extends BaseActivity {
    public static final String TAG = BaseActivityWithGNSSSupport.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ask for permissions we need
        //rx-way -:)
        //alternative is using Permission Dispatcher library (it will provide reasons,etc)

        //request location permission (if we don't get it arleady) and init
        //Request permissions to do GPS/GLONASS. use RxPermissions in correct way
        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
        // Must be done during an initialization phase like onCreate
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS
                )
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // Got permissions
                        CustomLog.i(TAG,"Got permissions, proceeding with startup");
                        initGNSS();//really just bind services AND starts updates
                    } else {
                        // Oups permission denied
                        CustomLog.e(TAG,"Failed to permissionss. App will not work");
                    }
                });

        //тупит Event.CREATE так что пока - не используем
        RxNavi.observe(this, Event.CREATE)
                .subscribe(bundle -> {
                    CustomLog.i(TAG,"navi Create (baseGNSS) - asking for permissions...");
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
                });

        RxNavi.observe(this,Event.PAUSE)
                .subscribe(object -> {
                    CustomLog.i(TAG,"Pausing...");
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




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    // GNSS Support
    // disposables which are connected to specific services
    private CompositeDisposable servicesDisposables = new CompositeDisposable();
    //services:GNSSLocation service instance
    //private GNSSLocationService gnssLocationService;
    private GNSSLocationService locationService;
    //private MyLocationService locationService;
    //are we connected to location service?
    private boolean connectedToLocationService;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            //gnssLocationService=((GNSSLocationService.LocalBinder)service).getService();
            //locationService=((MyLocationService) service).getService();
            //locationService=(MyLocationService.LocalBinder)service).getService();

            //This brokes DI. TODO: do something about it
            locationService=((GNSSLocationService.LocalBinder)service).getService();

            connectedToLocationService=true;
            CustomLog.d(TAG,"Connected to location service:"+locationService);
            //connection is only done if we have permission so start updates
            locationService.gotPermissionSoStartUpdates()
                    .subscribe(
                            () -> {
                                CustomLog.d(TAG,"Location updates active");
                            },
                            s -> {
                                CustomLog.e(TAG,"Failed to start location updates."+s.toString()+" because "+s.getMessage());
                                CustomLog.logException(s);
                            });


            //no need to touch GUI at this time so use io scheduler
            /*
            Disposable locationUpdatesDisposable=locationService.observe()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            location -> CustomLog.v(TAG,"Location is "+location.toString())
                    );

            servicesDisposables.add(locationUpdatesDisposable);
            */

            /*
            Disposable satUpdatesDisposable=locationService.observeSatUpdates()
                    .observeOn(Schedulers.computation())
                    .distinctUntilChanged()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            sats -> CustomLog.d(TAG, "Sats: total " + sats.first + ", used in fix " + sats.second)
                    );
            servicesDisposables.add(satUpdatesDisposable);
            */

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            //mark all as not connected
            locationService=null;
            connectedToLocationService=false;
            //free disposables (=observables in rx1)
            servicesDisposables.clear();
            CustomLog.d(TAG,"Disconnected from location service");

        }
    };


    /**
     * Connect to our service(s)
     * Base-GNSS version
     */
    protected void doBindServices() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        CustomLog.d(TAG,"Binding services...");
        CustomLog.d(TAG,"Binding services...binding Location:"+GNSSLocationService.class);
        Intent intentPlayer=new Intent(this,GNSSLocationService.class);
        bindService(intentPlayer,locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Disconnect from our service(s)
     * Base-GNSS version
     */
    protected void doUnbindServices() {
        CustomLog.d(TAG,"Unbinding services");
        if (connectedToLocationService) {
            CustomLog.d(TAG,"Unbinding location");
            unbindService(locationServiceConnection);
            connectedToLocationService=false;
        }
    }


    /**
     * Init GNSS - just start and bind
     * in case this wasn't done before - we need this NOW!
     */
    public void initGNSS() {
        doBindServices();
    }


    /*

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForGNSS(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_gps_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedForGNSS() {
        //user denied permission
        Toast.makeText(this, R.string.permission_gps_denied, Toast.LENGTH_LONG).show();
        CustomLog.e(TAG,"GNSS access denied");
        //todo:just finish activity?
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showNeverAskForGNSS() {
        //be persistent, we are sure user just make a mistake -:)
        CustomLog.w(TAG,"'GNSS access denied and we told to never ask for it");
        openApplicationSettings();
        Toast.makeText(this, R.string.permission_gps_never_askagain, Toast.LENGTH_LONG).show();
        //TODO:show snackbar (for example) with offer to open this app's settings and instructions to manually grant permission

    }
    //Open application's settins pagerCurrentPage so user can allow necessary permissions for us. He really wants to do this. right?
    public void openApplicationSettings() {
        CustomLog.v(TAG,"Opening settings with permission details");
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, PERMISSIONS_REQUEST_CODE);
    }
    private static final int PERMISSIONS_REQUEST_CODE = 1984;
    */

}
