package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class AppContainer {

    private final Context mContext;

    public AppContainer(Context context) {
        mContext = context;

        playlistDataProvider = new PlaylistDataProvider(mContext);
    }

    // TODO: need to place these fields into separate class
    public ArrayList<ContentValues> savedValues;
    public long playListId;
    public int valuesToInsert;

    public final PlaylistDataProvider playlistDataProvider;
}
