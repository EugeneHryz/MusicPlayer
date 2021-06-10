package com.example.musicplayer.playlisttracklist;

import android.support.v4.media.MediaMetadataCompat;

import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;
import com.example.musicplayer.Playlist;

public interface PlaylistTrackListContract {

    interface Presenter extends BasePresenter<MediaMetadataCompat> {

        void play(int position);

        Playlist getPlaylist();

        void deleteTrackFromPlaylist(MediaMetadataCompat trackMetadata, int position);

        int updateTrackPosition(MediaMetadataCompat metadata);
    }

    interface View extends BaseView<PlaylistTrackListPresenter> {

        void setTrackNumber(int number);

        void setupChildViews();

        void updateRecyclerView(int position);
    }
}
