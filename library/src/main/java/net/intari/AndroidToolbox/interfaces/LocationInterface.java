package net.intari.AndroidToolbox.interfaces;


import android.location.Location;
import android.os.Binder;
import android.support.v4.util.Pair;


import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 16.05.17.
 * Interface(s) other parts need from every Location Service
 * Also see BaseLocationService (parts of binding logic)
 */

public interface LocationInterface {
    /**
     * Allows to observe location updates
     * @return hot observable to use
     */
    public Observable<Location> observe();

    /**
     * Allows to observe number of satellites in fix
     * @return hot observerable with  pair - first - total number of satellites, second - used in fix
     */
    public Observable<Pair<Integer,Integer>> observeSatUpdates();

    /**
     * Request location permissions and start updates
     * Will perform early successful return if location manager arleady initialized
     * To be called from activity
     * will use default values for time intervals
     * @return
     */
    public Completable gotPermissionSoStartUpdates();

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
    public Completable gotPermissionSoStartUpdates(long gpsTime,long netTime,long filterTime);
    /**
     * Returns service
     * See implementations of class
     * @return service used to create
     */
    public class LocalBinder extends Binder {
    }
}
