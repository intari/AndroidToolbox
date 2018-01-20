package net.intari.AndroidToolbox;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;


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

}
