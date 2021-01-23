package com.example.musicplayer.tracklist;

import android.support.v4.media.MediaMetadataCompat;
import android.view.View;

import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;
import com.example.musicplayer.Track;

public interface TrackListContract {

    interface Presenter extends BasePresenter<MediaMetadataCompat> {

        void play(int position);

        int getCurrentPlayingPosition();
    }

    interface View extends BaseView<TrackListPresenter> {

    }
}
