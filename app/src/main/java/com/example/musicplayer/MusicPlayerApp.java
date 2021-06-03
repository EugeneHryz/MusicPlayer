package com.example.musicplayer;

import android.app.Application;

public class MusicPlayerApp extends Application {

    public AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();

        appContainer = new AppContainer(getApplicationContext());
    }
}
