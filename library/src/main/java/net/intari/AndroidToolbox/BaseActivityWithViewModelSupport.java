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

    private static final String DATA = "data"; //Для сохранения данных
    private T data; //Дженерик, ибо для каждого активити используется своя ViewModel


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            data = savedInstanceState.getParcelable(DATA); //Восстанавливаем данные если они есть
        else
            connectData(); //Если нету - подключаем новые


        setActivity(); //Привязываем активити для ViewModel (если не используем Dagger)
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
     * Метод onDestroy будет вызываться при любом повороте экрана, так что нам нужно знать
     * что мы сами закрываем активити, что бы уничтожить данные.
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
     * Возвращаем данные
     *
     * @return возврощает ViewModel, которая прикреплена за конкретной активити
     */
    public T getData() {
        CustomLog.d(TAG, "Returning data "+data.toString());
        return data;
    }

    /**
     * Прикрепляем ViewModel к активити
     *
     * @param data
     */
    public void setData(T data) {
        this.data = data;
    }


    /**
     * Уничтожаем данные, предварительно отписываемся от всех подписок Rx
     */
    public void destroyData() {
        if (data != null) {
            data.globalDispose();
            data = null;
            CustomLog.d(TAG,"Data destroyed");
        }
    }


    /**
     * Абстрактный метод, который вызывается, если у нас еще нет сохраненных данных
     */
    protected abstract void connectData();




}
