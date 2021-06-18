package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.core.os.HandlerCompat;

import com.example.musicplayer.controlspanel.slidingimage.SlidingImageFragmentStateSaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppContainer {

    public AppContainer(Context context) {

        executorService  = Executors.newFixedThreadPool(10);
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        dataProvider = new DataProvider(context, executorService, mainThreadHandler);
        playlistDataProvider = new PlaylistDataProvider(context, executorService, mainThreadHandler);
    }

    // TODO: need to place these fields into a separate class
    public ArrayList<ContentValues> savedValues;
    public long playListId;
    public int valuesToInsert;

    public PlaylistDataProvider playlistDataProvider;

    public DataProvider dataProvider;

    public final ExecutorService executorService;

    public final Handler mainThreadHandler;

    public MusicService.LocalBinder binder;

    public SlidingImageFragmentStateSaver stateSaver;
}
