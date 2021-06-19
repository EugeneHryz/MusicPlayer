package com.example.musicplayer.playlistlist;

import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;
import com.example.musicplayer.data.Playlist;

public interface PlaylistListContract {

    interface Presenter extends BasePresenter<Playlist> {

        void addPlaylistTrackListFragment(android.view.View view, String transitionName, Playlist playlist);

        int getTracksNumber(long playlistId);

        void deletePlaylist(long playlistId, int position);
    }

    interface View extends BaseView<PlaylistListPresenter> {

        void updateRecyclerView(int position);
    }
}
