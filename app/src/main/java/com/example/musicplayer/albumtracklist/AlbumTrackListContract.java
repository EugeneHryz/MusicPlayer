package com.example.musicplayer.albumtracklist;

import android.support.v4.media.MediaMetadataCompat;

import com.example.musicplayer.Album;
import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;

public interface AlbumTrackListContract {

    interface Presenter extends BasePresenter<MediaMetadataCompat> {

        void play(int position);

        Album getAlbum();
    }

    interface View extends BaseView<AlbumTrackListPresenter> {

        void setupChildViews();
    }
}
