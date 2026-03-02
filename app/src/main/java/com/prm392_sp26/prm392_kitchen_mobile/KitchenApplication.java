package com.prm392_sp26.prm392_kitchen_mobile;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class KitchenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
