package net.intari.AndroidToolbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.ObservableField;
import android.location.Location;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import com.amplitude.api.Amplitude;
import com.yandex.metrica.YandexMetrica;


import net.intari.CustomLogger.CustomLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Support utils for my projects
 * (c) Dmitriy Kazimirov 2015-2016, e-mail:dmitriy.kazimirov@viorsan.com
 *
 *
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    //per https://stackoverflow.com/questions/880365/any-way-to-invoke-a-private-method
    public static Object genericInvokMethod(Object obj, String methodName,
                                            int paramCount, Object... params) {
        Method method;
        Object requiredObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i].getClass();
        }
        try {
            method = obj.getClass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
            CustomLog.logException(e);
        } catch (IllegalArgumentException e) {
            CustomLog.logException(e);
        } catch (IllegalAccessException e) {
            CustomLog.logException(e);
        } catch (InvocationTargetException e) {
            CustomLog.logException(e);
        }

        return requiredObj;
    }

    //add prefix to every map elemement
    public static Map<String,String> addPrefixToMap(Map<String,String> map,String prefix) {
        Map<String,String> result=new HashMap<>();
        for (String key:map.keySet()) {
            result.put(prefix+key,map.get(key));
        }
        return result;
    }

    /*
      ConvertS JSON to form which can be used as params for Volley HTTP posting
      Supported types:JSONObject, JSONArray,Integer,Long,Double,String
      .toString is called for everything else
      Exception handling is caller's responsibility
      needed for metadata.music processing
     */
    public static Map<String,String> encodeJSONToMapWithPrefix(JSONObject json,String prefix2) throws JSONException, UnsupportedEncodingException {
        Map<String,String> result=new HashMap<>();
        Iterator<String> keys = json.keys();
        String keyPrefix="";
        if (prefix2!=null) {
            keyPrefix=prefix2;
        }
        while (keys.hasNext()) {
            String key = keys.next();
            Object value= json.get(key);

            if (value instanceof JSONObject) {
                Map<String, String> r = encodeJSONToMapWithPrefix((JSONObject) value, keyPrefix + "[" + key + "]");
                result.putAll(r);

            } else if (value instanceof JSONArray) {
                JSONArray jarr=(JSONArray)value;
                for (int i=0;i<jarr.length();i++) {
                    Map<String, String> r = encodeJSONToMapWithPrefix(jarr.getJSONObject(i), keyPrefix + "[" + key + "][]");
                    result.putAll(r);
                }
            } else if (value instanceof Integer) {
                result.put(keyPrefix+"["+key+"]",Integer.toString((Integer)value));
            } else if (value instanceof Long) {
                result.put(keyPrefix+"["+key+"]",Long.toString((Long)value));
            } else if (value instanceof Double) {
                result.put(keyPrefix+"["+key+"]",Double.toString((Double)value));
            } else if (value instanceof String) {
                result.put(keyPrefix+"["+key+"]",(String)value);
            } else {
                result.put(keyPrefix+"["+key+"]",value.toString());
            }


        }
        return result;

    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density );
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    //thread utils
    public static long getThreadId()
    {
        Thread t = Thread.currentThread();
        return t.getId();
    }

    public static String getThreadSignature()
    {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name;
        if (Utils.isUiThread()) {
            name="(UI)"+ t.getName();
        } else {
            name= t.getName();
        }
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        return (name
                + ":(id)" + l
                + ":(priority)" + p
                + ":(group)" + gname);
    }

    /**
     * Is this UI Thread?
     * @return
     */
    public static boolean isUiThread()
    {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }

    private static  Map<String, Object> superAttributes = new HashMap<String, Object>();

    private static boolean analytics_YandexMetricaActive = false;

    private static boolean analytics_AmplitudeActive = false;

    //TODO:add actual code helper to init analytics
    /**
     * Enable Yandex App Metrica for reportAnalyticsEvent.
     * It's up to client to perform actual initialization and provide keys
     * See
     * https://tech.yandex.ru/appmetrica/doc/mobile-sdk-dg/concepts/android-initialize-docpage/ or
     * @param activate
     */
    public static void activateYandexMetrica(boolean activate) {
        analytics_YandexMetricaActive=activate;
    }
    /**
     * Enable Amplitude for reportAnalyticsEvent.
     * It's up to client to perform actual initialization and provide keys
     * See https://amplitude.zendesk.com/hc/en-us/articles/115002935588#installation
     * @param activate
     */
    public static void activateAmplitude(boolean activate) {
        analytics_AmplitudeActive=activate;
    }

    /**
     * Adds 'super attribute' to be sent with each report (or replace one if it's exist)
     * @param key attribute name
     * @param obj attribute value
     */
    public static void addAnalyticsSuperAttribute(String key,Object obj) {
        superAttributes.put(key,obj);
        /*
        if (superAttributes.containsKey(key)) {
            superAttributes.replace(key,obj);
        } else {
            superAttributes.put(key,obj);
        }
        */
    }
    /**
     * Report event to analytics
     * @param event event name
     */
    public static void reportAnalyticsEvent(String event) {
        reportAnalyticsEvent(event,null);
    }

    /**
     * Report event to analytics
     * It's assumed that analytics libs are initialized
     * @param event event name
     * @param eventAttributes attributes to send with event
     *
     */
    public static void reportAnalyticsEvent(String event, Map<String, Object> eventAttributes) {
        //add super attrivutes
        Map<String, Object> attributes=new HashMap<String, Object>();
        if (eventAttributes!=null) {
            attributes.putAll(eventAttributes);
        }
        attributes.putAll(superAttributes);

        //send event to  Yandex.Metrica
        if (analytics_YandexMetricaActive) {
            YandexMetrica.reportEvent(event,attributes);
        }
        //Convert attributes for Amplitude and Mixpanel
        try {
            JSONObject props = new JSONObject();
            for (String key:attributes.keySet()) {
                props.put(key,attributes.get(key));
            }

            if (analytics_AmplitudeActive) {
                Amplitude.getInstance().logEvent(event,props);

            }
            //WARNING!. Mixpanel has rather low free limits!
            //AppController.getInstance().getMixpanel().track(event);
        }  catch (JSONException e) {
            CustomLog.logException(e);
        }
        //TODO: also write to (encrypted) log file
    }

    /**
     * Helper to safely work with progress dialogs, accounting for possible issues like dialog being in another view hierarchy
     * based off http://stackoverflow.com/questions/2224676/android-view-not-attached-to-window-manager
     * @param context
     * @param dialog
     */
    public static boolean isDialogUsable(Context context, AlertDialog dialog) {
        //Can't touch other View of other Activiy..
        //http://stackoverflow.com/questions/23458162/dismiss-progress-dialog-in-another-activity-android
        if (context==null) {
            CustomLog.e(TAG,"Null context passed");
            CustomLog.logException(new RuntimeException("Null context passed"));
            return false;
        }
        if ( (dialog != null) && dialog.isShowing()) {

            //is it the same context from the caller ?
            CustomLog.w(TAG, "the dialog is from "+dialog.getContext());

            Class caller_context= context.getClass();
            Activity call_Act = (Activity)context;
            Class progress_context= dialog.getContext().getClass();

            Boolean is_act= ( (dialog.getContext()) instanceof  Activity )?true:false;
            Boolean is_ctw= ( (dialog.getContext()) instanceof ContextThemeWrapper)?true:false;

            if (is_ctw) {
                ContextThemeWrapper cthw=(ContextThemeWrapper) dialog.getContext();
                Boolean is_same_acivity_with_Caller= ((Activity)(cthw).getBaseContext() ==  call_Act )?true:false;

                if (is_same_acivity_with_Caller){
                    //ok to work with dialog
                    return true;
                }
                else {
                    CustomLog.e(TAG, "the dialog is NOT from the same context! Can't touch.."+((Activity)(cthw).getBaseContext()).getClass());
                    //not ok to work with dialog
                    return false;
                }
            } else {
                CustomLog.e(TAG,progress_context.getName()+" is not ctw(1)");
                //not ok to work with dialog - not ContextThemeWrapper
                return false;
            }
        } else {
            //dialog is either null or not showing
            return false;
        }

    }
    /**
     * Helper to safely dismiss progress dialog, accounting for possible issues like dialog being in another view hierarchy
     * based off http://stackoverflow.com/questions/2224676/android-view-not-attached-to-window-manager
     * @param context
     * @param dialog
     */
    public static void dismissProgressDialog(Context context, ProgressDialog dialog) {
        //Can't touch other View of other Activiy..
        //http://stackoverflow.com/questions/23458162/dismiss-progress-dialog-in-another-activity-android
        if (context==null) {
            CustomLog.e(TAG,"Null context passed");
            CustomLog.logException(new RuntimeException("Null context passed"));
            return;
        }
        if ( (dialog != null) && dialog.isShowing()) {

            //is it the same context from the caller ?
            CustomLog.w(TAG, "the dialog is from "+dialog.getContext());

            Class caller_context= context.getClass();
            Activity call_Act = (Activity)context;
            Class progress_context= dialog.getContext().getClass();

            Boolean is_act= ( (dialog.getContext()) instanceof  Activity )?true:false;
            Boolean is_ctw= ( (dialog.getContext()) instanceof ContextThemeWrapper)?true:false;

            if (is_ctw) {
                ContextThemeWrapper cthw=(ContextThemeWrapper) dialog.getContext();
                Boolean is_same_acivity_with_Caller= ((Activity)(cthw).getBaseContext() ==  call_Act )?true:false;

                if (is_same_acivity_with_Caller){
                    dialog.dismiss();
                    dialog = null;
                }
                else {
                    CustomLog.e(TAG, "the dialog is NOT from the same context! Can't touch.."+((Activity)(cthw).getBaseContext()).getClass());
                    dialog = null;
                }
            } else {
                CustomLog.e(TAG,progress_context.getName()+" is not ctw(1)");
            }


        }
    }
    /**
     * Helper to safely cancel alert dialog, accounting for possible issues like dialog being in another view hierarchy
     * based off http://stackoverflow.com/questions/2224676/android-view-not-attached-to-window-manager
     * @param context
     * @param dialog
     */
    public static void cancelAlertDialogFromSupportLibrary(Context context, android.support.v7.app.AlertDialog dialog) {
        //Can't touch other View of other Activiy..
        //http://stackoverflow.com/questions/23458162/dismiss-progress-dialog-in-another-activity-android
        if (context==null) {
            CustomLog.e(TAG,"Null context passed");
            CustomLog.logException(new RuntimeException("Null context passed"));
            return;
        }
        if ( (dialog != null) && dialog.isShowing()) {

            //is it the same context from the caller ?
            CustomLog.w(TAG, "the dialog is from "+dialog.getContext());

            Class caller_context= context.getClass();
            Activity call_Act = (Activity)context;
            Class progress_context= dialog.getContext().getClass();

            Boolean is_act= ( (dialog.getContext()) instanceof  Activity )?true:false;
            Boolean is_ctw= ( (dialog.getContext()) instanceof ContextThemeWrapper)?true:false;

            if (is_ctw) {
                ContextThemeWrapper cthw=(ContextThemeWrapper) dialog.getContext();
                Boolean is_same_acivity_with_Caller= ((Activity)(cthw).getBaseContext() ==  call_Act )?true:false;

                if (is_same_acivity_with_Caller){
                    dialog.cancel();
                    dialog = null;
                }
                else {
                    CustomLog.e(TAG, "the dialog is NOT from the same context! Can't touch.."+((Activity)(cthw).getBaseContext()).getClass());
                    dialog = null;
                }
            } else {
                CustomLog.e(TAG,progress_context.getName()+" is not ctw(1)");
            }


        }
    }
    /**
     * Helper to safely dismiss alert dialog, accounting for possible issues like dialog being in another view hierarchy
     * based off http://stackoverflow.com/questions/2224676/android-view-not-attached-to-window-manager
     * @param context
     * @param dialog
     */
    public static void dismissAlertDialogFromSupportLibrary(Context context, android.support.v7.app.AlertDialog dialog) {
        //Can't touch other View of other Activiy..
        //http://stackoverflow.com/questions/23458162/dismiss-progress-dialog-in-another-activity-android
        if (context==null) {
            CustomLog.e(TAG,"Null context passed");
            CustomLog.logException(new RuntimeException("Null context passed"));
            return;
        }
        if ( (dialog != null) && dialog.isShowing()) {

            //is it the same context from the caller ?
            CustomLog.w(TAG, "the dialog is from "+dialog.getContext());

            Class caller_context= context.getClass();
            Activity call_Act = (Activity)context;
            Class progress_context= dialog.getContext().getClass();

            Boolean is_act= ( (dialog.getContext()) instanceof  Activity )?true:false;
            Boolean is_ctw= ( (dialog.getContext()) instanceof ContextThemeWrapper)?true:false;

            if (is_ctw) {
                ContextThemeWrapper cthw=(ContextThemeWrapper) dialog.getContext();
                Boolean is_same_acivity_with_Caller= ((Activity)(cthw).getBaseContext() ==  call_Act )?true:false;

                if (is_same_acivity_with_Caller){
                    dialog.dismiss();
                    dialog = null;
                }
                else {
                    CustomLog.e(TAG, "the dialog is NOT from the same context! Can't touch.."+((Activity)(cthw).getBaseContext()).getClass());
                    dialog = null;
                }
            } else {
                CustomLog.e(TAG,progress_context.getName()+" is not ctw(1)");
            }


        }
    }

    /**
     * Returns a user agent string based on the given application name
     *
     * @param context A valid context of the calling application.
     * @param applicationName String that will be prefix'ed to the generated user agent.
     * @return A user agent string generated using the applicationName.
     */
    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") ";
    }

    /**
     * method checks to see if app is currently set as default launcher
     * @return boolean true means currently set as default, otherwise false
     */
    public static boolean isMyAppLauncherDefault(Context context) {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);
        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) context.getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

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
