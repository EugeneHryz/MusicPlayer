package com.example.musicplayer.tracklist;

import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.view.View;

import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;
import com.example.musicplayer.Track;

public interface TrackListContract {

    interface Presenter extends BasePresenter<MediaMetadataCompat> {

        void play(int position);

        String getCurrentMediaId();

        boolean isPlaying();
    }

    interface View extends BaseView<TrackListPresenter> {

        void toggleEqualizerAnimation(android.view.View view, boolean resume, boolean visible);

        void rebindItems();
    }
}
