package net.intari.AndroidToolbox;

import android.databinding.ObservableField;
import android.location.Location;
import android.opengl.GLES20;
import android.support.annotation.NonNull;

import net.intari.AndroidToolboxCore.Geometry;
import net.intari.CustomLogger.CustomLog;

import io.reactivex.Observable;

/**
 * Support utils for my projects
 * (c) Dmitriy Kazimirov 2015-2016, e-mail:dmitriy.kazimirov@viorsan.com
 *
 *
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();


    /**
     * Converts DataBinding's ObservableField  to RxJava2's Observable
     * see https://habrahabr.ru/post/328512/
     * @param observableField
     * @param <T>
     * @return Observable (RxJava2)
     */
    public static <T> Observable<T> toObservable(@NonNull final ObservableField<T> observableField) {

        return Observable.fromPublisher(asyncEmitter -> {
            final android.databinding.Observable.OnPropertyChangedCallback callback = new android.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(android.databinding.Observable dataBindingObservable, int propertyId) {
                    if (dataBindingObservable == observableField) {
                        asyncEmitter.onNext(observableField.get());
                    }
                }
            };
            observableField.addOnPropertyChangedCallback(callback);
        });
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String tag, String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            CustomLog.e(tag, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }


    /**
     * Расстояние в метрах из GPS-координат
     * @param location1 начальная координата
     * @param location2 конечная координата
     * @return Расстояние в метрах между точками
     */
    public static float distFrom(Location location1,Location location2) {
        return distFrom(
                location1.getLatitude(),location1.getLongitude(),
                location2.getLatitude(),location2.getLongitude());
    }
    /**
     * Расстояние в метрах из GPS-координат
     * https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
     * @param lat1 начальная широта
     * @param lng1 начальная долгота
     * @param lat2 конечная широта
     * @param lng2 конечная долгота
     * @return Расстояние в метрах между точками
     */
    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //радиус Земли в метрах
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    /**
     * Преобразование из GPS-координат на плоскость
     * на основе https://stackoverflow.com/questions/3024404/transform-longitude-latitude-into-meters?rq=1
     * Мирные советские тракторы поддерживаются только в наземном режиме.
     * @param baseLocation координаты опорной точки
     * @param location текущие координаты
     * @return
     */
    public static Geometry.Point getToXY(Location baseLocation, Location location) {

        double deltaLatitude = location.getLatitude() - baseLocation.getLatitude();
        double deltaLongitude = location.getLongitude() - baseLocation.getLongitude();
        /*
            The circumference at the equator (latitude 0) is 40075160 meters.
            The circumference of a circle at a given latitude will be proportional to the cosine, so
            the formula will be deltaLongitude * 40075160 * cos(latitude) / 360
         */
        double latitudeCircumference = 40075160.0 * Math.cos(Math.toRadians(baseLocation.getLatitude()));
        double resultX = deltaLongitude * latitudeCircumference / 360.0;

        /*
            We know that 360 degrees is a full circle around the earth through the poles,
            and that distance is 40008000 meters. As long as you don't need to account for
            the errors due to the earth being not perfectly spherical,
            the formula is deltaLatitude * 40008000 / 360.
         */
        double resultY = deltaLatitude * 40008000.0 / 360.0;
        //Point тут используется не совсем по назначению, нужен класс вроде 2D location...
        //потому что будет еще фактор масштабирования использоваться
        Geometry.Point result=new Geometry.Point((float)resultX,(float)resultY,0f);
        return result;
    }
}
