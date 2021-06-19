package com.example.musicplayer.albumlist;

import com.example.musicplayer.data.Album;
import com.example.musicplayer.BasePresenter;
import com.example.musicplayer.BaseView;

public interface AlbumListContract {

    interface Presenter extends BasePresenter<Album> {

        void addAlbumTrackListFragment(android.view.View view, String transitionName, Album album);

        void showBottomDialogFragment(int position);
    }

    interface View extends BaseView<AlbumListPresenter> {

    }
}
