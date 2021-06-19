package com.example.musicplayer.data;

import android.support.v4.media.MediaMetadataCompat;
import android.view.View;

import java.util.ArrayList;

public interface MusicDataProvider {

    interface GetTrackListCallback {

        void trackListLoadStarted();

        void onTrackListLoaded(ArrayList<MediaMetadataCompat> trackList);
    }

    int getItemCount();
}
