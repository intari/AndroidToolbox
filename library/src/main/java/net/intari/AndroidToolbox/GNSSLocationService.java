package net.intari.AndroidToolbox;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.widget.Toast;

import net.intari.AndroidToolbox.interfaces.LocationInterface;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

import net.intari.CustomLogger.CustomLog;
import net.intari.KalmanLocationManager.KalmanLocationManager;

/**
 * Location service using real GNSS (GPS/GLONASS/etc) interfaces
 * Network location provider is not supported
 * Fusion location provider is also not supported
 * (c) Dmitriy Kazimirov, 2017, e-mail:dmitriy.kazimirov@viorsan.com
 */
public class GNSSLocationService extends Service implements LocationListener,LocationInterface {
    public static final String TAG = GNSSLocationService.class.getSimpleName();

    //service
    final IBinder mBinder = new GNSSLocationService.LocalBinder();

    private boolean initDone=false;

    //so we can signal service it's time to die
    private BroadcastReceiver stopRequestReceiver=null;

    //command to stop service
    public static final String STOP_REQUEST = "net.intari.android.GNSSService.StopRequest";

    //TODO:ConnectableObservable?
    private final BehaviorSubject<Location> locationSubject = BehaviorSubject.createDefault(new Location(LocationManager.GPS_PROVIDER));
    private final BehaviorSubject<Pair<Integer,Integer>> satsSubject = BehaviorSubject.createDefault(new Pair(0,0));


    // Default constants for KalmanLocationManager
    /**
     * Request location updates with the highest possible frequency on gps.
     * Typically, this means one update per second for gps.
     */
    private static final long GPS_TIME = 1000;

    /**
     * For the network provider, which gives locations with less accuracy (less reliable),
     * request updates every 5 seconds.
     */
    private static final long NET_TIME = 5000;

    /**
     * For the filter-time argument we use a "real" value: the predictions are triggered by a timer.
     * Lets say we want 5 updates (estimates) per second = update each 200 millis.
     */
    private static final long FILTER_TIME = 200;

    private KalmanLocationManager kalmanLocationManager = null;

    private Location lastLocation=new Location(LocationManager.GPS_PROVIDER);//must be at least something non-null, so if we get first fix before location update, we not crash

    /**
     * Service constructor and initial init (in future)
     */
    public GNSSLocationService() {

        CustomLog.i(TAG,"GNSSLocationService()");
    }

    /**
     * Work around Kotlin's package protection issues
     * Prevents issues like 'Error:(113, 76) Cannot access 'service': it is public *package* for synthetic extension in '<dependencies of app_debug>'
     * see also https://discuss.kotlinlang.org/t/kotlin-to-support-package-protected-visibility/1544/11 ?
     * @param binder IBinder instance of our connected service
     * @return connected service
     */
    public static GNSSLocationService getMyServiceFromLocalBinder(IBinder binder) {
        return ((GNSSLocationService.LocalBinder)binder).getService();
    }

    /**
     * Make it easy to start this service by users
     * @param context context to use
     * @return ComponentName (regular return from startService)
     */
    public static ComponentName startThisService(Context context) {
        return context.startService(new Intent(context,GNSSLocationService.class));
    }
    /**
     * Get service instance
     * @return service instance
     */
    public GNSSLocationService getService() {
        return GNSSLocationService.this;
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CustomLog.i(TAG,"onBind("+intent+")");
        return mBinder;
    }


    public class LocalBinder extends Binder {
        GNSSLocationService getService() {
            return GNSSLocationService.this;
        }
    }

    /**
     * Show message to user if needed
     * @param message
     */
    protected void showMessage(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    @Override
    public void onCreate() {
        CustomLog.i(TAG,"onCreate()");
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.i(TAG, "Received start id " + startId + ": " + intent);
        if (intent!=null) {
            if (intent.getAction()!=null) {
                //TODO:Do something ... when we have to do

            } else {
                CustomLog.d(TAG,"intent.getAction is null");
            }
        } else {
            CustomLog.d(TAG,"Intent is null");
        }
        return START_STICKY;
    }

    /**
     * Destroy service
     */
    @Override
    public void onDestroy() {
        CustomLog.i(TAG,"OnDestroy()");
        kalmanLocationManager.removeUpdates(this);
        //send event to subscribers via rx
        locationSubject.onComplete();
        //EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopRequestReceiver);
    }


    private void init() {

        if (initDone) {
            CustomLog.w(TAG, "Init arleady done");
            return;
        }
        CustomLog.i(TAG, "Naigation service starting up");
        //TODO:actual init.
        //Assume we have permissions at this time


        stopRequestReceiver=  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                CustomLog.d(TAG, "UI signaled we should logout. Stopping service");
                //commit suicide
                stopSelf();
                CustomLog.d(TAG, "stopSelf() called");
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                stopRequestReceiver,new IntentFilter(STOP_REQUEST)
        );
    }

    /**
     * Request location permissions and start updates
     * Will perform early successful return if location manager arleady initialized
     * To be called from activity
     * will use default values for time intervals
     * @return completable
     */
    public Completable gotPermissionSoStartUpdates() {
        return gotPermissionSoStartUpdates(GPS_TIME,NET_TIME,FILTER_TIME);
    }

    /**
     * Request location permissions and start updates
     * To be called from activity
     * Will perform early successful return if location manager arleady initialized
     * @param filterTime time between kalman updates
     * @param gpsTime  requested time between GPS updates
     * @param netTime requested time between Network Location updates
     *
     * @return
     */
    public Completable gotPermissionSoStartUpdates(long gpsTime,long netTime,long filterTime) {
        //early successful return if updates arleady started (and somebode called us twice in row, it's possible because
        if (kalmanLocationManager!=null) {
            CustomLog.v(TAG,"Location updates arleady started");
            return Completable.create(s -> {
                s.onComplete();
            });
        }

        //no early return, perform real initialization
        Completable completable=initGNSS(gpsTime, netTime, filterTime)
                .subscribeOn(AndroidSchedulers.mainThread()) //we need looper here
                .observeOn(AndroidSchedulers.mainThread()) // We can touch in showMessage so...
                .cache();//cache result. we need it twice

        completable
                .subscribe(
                        () -> {
                            CustomLog.d(TAG,"Init done");
                            initDone=true;
                        },
                        s -> {
                            CustomLog.e(TAG,"Failed to init:"+s.toString()+" because "+s.getMessage());
                            CustomLog.logException(s);
                            stopSelf();
                            showMessage(s.getMessage());
                        });

        return completable;
    }
    /**
     * Init location monitoring
     * TODO:make it also possible to ask for Network Provider (and also support fused location provider)
     * @param gpsTime
     * @param netTime
     * @param filterTime time between updates (via calman)
     * @return
     */
    private Completable initGNSS(long gpsTime,long netTime,long filterTime) {
        return Completable.create( s -> {

            //get Location Manager
            kalmanLocationManager=new KalmanLocationManager(this);

            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                CustomLog.e(TAG,"initGNSS called without necessary permissions. Cannot continue");
                //just return error
                s.onError(new SecurityException(getResources().getString(R.string.no_gnss)));
                //showMessage(getResources().getString(R.string.no_gnss));
                //this.stopSelf();
                //return;
            }

            // wrote what we can use
            String providers = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            CustomLog.d(TAG,"Allowed GPS Providers:"+providers);

            // request maximum (requested) frequence updates
            kalmanLocationManager.requestLocationUpdates(KalmanLocationManager.UseProvider.GPS,filterTime,gpsTime,netTime,this,true);


            /*
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                CustomLog.d(TAG, "GPS enabled");
                //assume 10 Hz rate. Do not assume movement at all
                //rate-limit updates to 10 Hz
                //we need prepared looper for this to work. it's better to call this on main thread at this time
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_UPDATE_INTERVAL_MILLIS,0,this);

                //activate listener to show alert message if user disabled GPS while we are here
                GpsStatus.Listener listener = new GpsStatus.Listener() {
                    public void onGpsStatusChanged(int event) {
                        //send to everybody who wants via EventBus
                        LocationUpdatedEvent luevent = new LocationUpdatedEvent(getLastLocation());
                        EventBus.getDefault().post(luevent);

                        switch (event) {
                            case GpsStatus.GPS_EVENT_STOPPED:
                                CustomLog.d(TAG, "GPS Stopped");
                                break;
                            case GpsStatus.GPS_EVENT_STARTED:
                                CustomLog.d(TAG, "GPS Started");
                                break;
                            case GpsStatus.GPS_EVENT_FIRST_FIX:
                                CustomLog.d(TAG, "GPS First Fix");
                                //update rx just in case. should we?
                                locationSubject.onNext(getLastLocation());
                                break;
                            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                                //CustomLog.d(TAG,"GPS Satellite status");
                                //TODO:separate observable so we knew current sat stats?
                                break;
                        }
                        int numSatsInFix = getNumSatsInFix(locationManager);
                        int numSats = getNumSats(locationManager);
                        //CustomLog.d(TAG, "Sats: total " + numSats + ", used in fix " + numSatsInFix);
                        satsSubject.onNext(new Pair<>(Integer.valueOf(numSats),Integer.valueOf(numSatsInFix)));
                    }
                };
                locationManager.addGpsStatusListener(listener);
                s.onComplete();
            } else {
                CustomLog.e(TAG,"GPS Provider NOT Enabled!");
                s.onError(new RuntimeException(getResources().getString(R.string.no_gps_provider)));
            }
            */
        });

    }
    /**
     * Called when the location has changed.
     * <p>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        CustomLog.i(TAG,"Location update to "+location + " via "+location.getProvider()+" accuracy "+location.getAccuracy()+" speed "+location.getSpeed()+" m/s");
        lastLocation=location;

        //send event to subscribers via rx
        locationSubject.onNext(location);
    }

    /**
     * Get last location.
     * .observe() must be used be external clients!
     *
     * @return last observed location or null
     */
    private Location getLastLocation() {
        return lastLocation;
    }


    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        CustomLog.v(TAG,"Provider status "+provider+" status "+status+" extras "+extras);

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        CustomLog.v(TAG,"Provider "+provider+" enabled");

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {
        CustomLog.w(TAG,"Provider "+provider+" disabled by user");

    }

    private int getNumSatsInFix(LocationManager locationManager) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            CustomLog.e(TAG,"Logic error. no permissions to get number of satellites in fix. Cannot continue");
            return Constants.INVALID_NUMBER_OF_SATS;
        }
        int numSats=0;
        int numSatsInFix=0;
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                numSatsInFix++;
            }
            numSats++;
        }
        return numSatsInFix;
    }

    private int getNumSats(LocationManager locationManager) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            CustomLog.e(TAG,"Logic error. no permissions to get number of satellites. Cannot continue");
            return Constants.INVALID_NUMBER_OF_SATS;
        }
        int numSats=0;
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            numSats++;
        }
        return numSats;
    }

    /**
     * Allows to observe location updates
     * @return hot observable to use
     */
    public Observable<Location> observe() {
        return locationSubject
                .doOnSubscribe((v) -> CustomLog.i(TAG,"subscribed to locationObserver:"+v));
    }

    /**
     * Allows to observe number of satellites in fix
     * @return hot observerable with  pair - first - total number of satellites, second - used in fix
     */
    public Observable<Pair<Integer,Integer>> observeSatUpdates() {
        return satsSubject
                .doOnSubscribe((v) -> CustomLog.i(TAG,"subscribed to satObserver:"+v));

    }

}
