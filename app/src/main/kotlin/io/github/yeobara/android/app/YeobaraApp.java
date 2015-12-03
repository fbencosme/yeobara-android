package io.github.yeobara.android.app;

import android.app.Application;

import com.firebase.client.Firebase;

public class YeobaraApp extends Application {

    public static YeobaraApp app;
    private YeobaraComponent component;

    @Override
    public void onCreate() {
        app = this;

        super.onCreate();
        Firebase.setAndroidContext(this);
        component = DaggerYeobaraComponent.builder()
                .yeobaraModule(new YeobaraModule())
                .build();
    }

    public YeobaraComponent getComponent() {
        return component;
    }
}
