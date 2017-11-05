package net.intari.AndroidToolbox.interfaces;

import android.location.Location;

import io.reactivex.Observable;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.11.2017.
 * support interface for BaseActivityWithGNSSSupport to provide required provider parameters
 */

public interface LocationParameters {
    /**
     * Request Kalman filter interval (interpolated updated), ms
     * @return
     */
    public long filterInterval();

    /**
     * Requested GPS update interval, ms
     * @return
     */
    public long gpsInterval();

    /**
     * Requested 'network location' update interval, ms (to be used in future)
     * @return
     */
    public long networkInterval();
}
