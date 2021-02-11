package com.example.musicplayer.albumlist;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.example.musicplayer.Album;
import com.example.musicplayer.DataProvider;
import com.example.musicplayer.PlaylistsBottomSheetFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.albumtracklist.AlbumTrackListFragment;
import com.example.musicplayer.albumtracklist.AlbumTrackListPresenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AlbumListPresenter implements AlbumListContract.Presenter {

    private ExecutorService executorService;
    private Handler mainThreadHandler;

    private DataProvider dataProvider;
    private AlbumListContract.View view;
    private ArrayList<Album> albumList;

    private AlbumTrackListPresenter trackListPresenter;

    public AlbumListPresenter(DataProvider dataProvider, AlbumListContract.View view,
                              ExecutorService executorService, Handler mainThreadHandler) {
        this.dataProvider = dataProvider;
        this.view = view;
        this.executorService = executorService;
        this.mainThreadHandler = mainThreadHandler;
        albumList = dataProvider.getAlbumListSynchronous();

        view.setPresenter(this);
    }

    @Override
    public Album getDataItem(int position) {
        if (position < albumList.size()) {
            return albumList.get(position);
        }
        return null;
    }

    @Override
    public int getDataItemCount() { return dataProvider.getItemCount(); }

    @Override
    public void addAlbumTrackListFragment(android.view.View view, String transitionName, Album album) {
        AlbumTrackListFragment fragment = new AlbumTrackListFragment(transitionName);

        AlbumTrackListPresenter presenter = new AlbumTrackListPresenter(new DataProvider(view.getContext(), executorService, mainThreadHandler),
                fragment, album, view.getContext());

        FragmentTransaction transaction = ((AppCompatActivity)view.getContext())
                .getSupportFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        androidx.transition.Transition sharedElementTransition = androidx.transition.TransitionInflater.from(view.getContext()).inflateTransition(R.transition.shared_image);
        fragment.setSharedElementEnterTransition(sharedElementTransition);
        Transition fadeTransition = TransitionInflater.from(view.getContext()).inflateTransition(R.transition.fade_in);
        fragment.setEnterTransition(fadeTransition);
        transaction.replace(R.id.fragment_container, fragment, AlbumTrackListFragment.TAG);
        transaction.addSharedElement(view.findViewById(R.id.album_cover), transitionName);
        transaction.addToBackStack(null).commit();
    }

    @Override
    public void showBottomDialogFragment(int position, Context context) {
        DataProvider dataProvider = new DataProvider(context, executorService);
        ArrayList<MediaMetadataCompat> trackList = dataProvider
                .getTrackListSynchronous(albumList.get(position), null, null);
        PlaylistsBottomSheetFragment fragment = new PlaylistsBottomSheetFragment(trackList);

        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
        fragment.showNow(manager, PlaylistsBottomSheetFragment.TAG);
    }
}
