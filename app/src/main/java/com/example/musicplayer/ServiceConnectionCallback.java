package com.example.musicplayer;

import android.content.ComponentName;
import android.os.IBinder;

public interface ServiceConnectionCallback {

    void onServiceConnected(ComponentName name, IBinder service);

    void onUnbind();
}
