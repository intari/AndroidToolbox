package net.intari.AndroidToolbox;

import android.app.Activity;
import android.databinding.BaseObservable;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 13.05.17.
 * idea from https://habrahabr.ru/post/328512/
 * TODO:translate comments to English
 */
public abstract class BaseViewModel extends BaseObservable {


    private CompositeDisposable disposables; //Для удобного управления подписками
    private Activity activity;


    protected BaseViewModel() {
        disposables = new CompositeDisposable();
    }


    /**
     * Метод добавления новых подписчиков
     */
    protected void newDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    /**
     * Метод для отписки всех подписок разом
     */
    public void globalDispose() {
        disposables.dispose();
    }


    protected Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isSetActivity() {
        return (activity != null);
    }

}
