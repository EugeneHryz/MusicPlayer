package com.example.musicplayer.tracklist;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;

public interface TrackListContract {

    interface Presenter extends BasePresenter<MediaMetadataCompat> {

        void play(int position);

        String getCurrentMediaId();

        boolean isPlaying();

        void showBottomDialogFragment(int position);

        void setView(TrackListContract.View view);
    }

    interface View extends BaseView<TrackListPresenter> {

        void toggleEqualizerAnimation(android.view.View view, boolean resume, boolean visible);

        void rebindItems();

        Context getUpdatedContext();
    }
}
