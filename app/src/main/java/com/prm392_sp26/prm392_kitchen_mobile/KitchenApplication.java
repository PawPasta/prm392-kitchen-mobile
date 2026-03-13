package com.prm392_sp26.prm392_kitchen_mobile;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class KitchenApplication extends Application {

    private static KitchenApplication instance;

    public static KitchenApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
    }
}
