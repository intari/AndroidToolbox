package net.intari.AndroidToolbox;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;


import net.intari.CustomLogger.CustomLog;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 13.05.17.
 * Base Activity with supports for ModeView restore logic from https://habrahabr.ru/post/328512/
 * and with regular Base Activity code
 * TODO:translate comments to English
 */

public abstract class BaseActivityWithViewModelSupport<T extends BaseViewModel> extends BaseActivityWithGNSSSupport {
    public static final String TAG = BaseActivityWithViewModelSupport.class.getSimpleName();

    private static final String DATA = "data"; //to save data
    private T data; //Generic because every activity uses it's own ViewModel


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            data = savedInstanceState.getParcelable(DATA); //Restore data if they exist
        else
            connectData(); //Connect new data if non exist


        setActivity(); //link activity
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (data != null) {
            CustomLog.d(TAG, "Data saved");
            outState.putParcelable(DATA, (Parcelable) data);
        }

    }

    /**
     * onDestroy is called on every rotation too so we must knew that we close activity ourselves to destroy data
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomLog.d(TAG, "onDestroy");
        if (isFinishing())
            destroyData();
    }


    /**
     * Этот метод нужен только если вы не используете DI.
     * А так, это простой способ передать активити для каких-то действий с preferences или DB
     */
    private void setActivity() {
        if (data != null) {
            if (!data.isSetActivity())
                data.setActivity(this);
        }
    }


    /**
     * Return data
     *
     * @return Returns  ViewModel linked to specific activity
     *
     */
    public T getData() {
        CustomLog.d(TAG, "Returning data "+data.toString());
        return data;
    }

    /**
     * Link ViewModel to activity
     *
     * @param data
     */
    public void setData(T data) {
        this.data = data;
    }


    /**
     * Destroy data and unlink from all Rx subscriptions
     */
    public void destroyData() {
        if (data != null) {
            data.globalDispose();
            data = null;
            CustomLog.d(TAG,"Data destroyed");
        }
    }


    /**
     * Abstract method, gets called if we don't have (any) stored data yet
     */
    protected abstract void connectData();

}
