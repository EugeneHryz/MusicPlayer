package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import com.example.musicplayer.data.DataProvider;
import com.example.musicplayer.data.PlaylistDataProvider;
import com.example.musicplayer.service.MusicService;
import com.example.musicplayer.tracklist.TrackListPresenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppContainer {

    private static final int THREAD_POOL_SIZE = 8;

    public AppContainer(Context context) {

        executorService  = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        dataProvider = new DataProvider(context, executorService, mainThreadHandler);
        playlistDataProvider = new PlaylistDataProvider(context, executorService, mainThreadHandler);
    }

    // FIXME: need to place these fields into a separate class
    public ArrayList<ContentValues> savedValues;
    public long playListId;
    public int valuesToInsert;


    public PlaylistDataProvider playlistDataProvider;

    public DataProvider dataProvider;

    public final ExecutorService executorService;

    public final Handler mainThreadHandler;

    public MusicService.LocalBinder binder;

    public TrackListPresenter trackListPresenter;
}
