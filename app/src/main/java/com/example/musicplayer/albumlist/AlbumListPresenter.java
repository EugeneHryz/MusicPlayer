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

import com.example.musicplayer.data.Album;
import com.example.musicplayer.AppContainer;
import com.example.musicplayer.data.DataProvider;
import com.example.musicplayer.data.MusicDataProvider;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.playlistdialog.PlaylistsBottomSheetFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.albumtracklist.AlbumTrackListFragment;
import com.example.musicplayer.albumtracklist.AlbumTrackListPresenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AlbumListPresenter implements AlbumListContract.Presenter,
        MusicDataProvider.GetTrackListCallback {

    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;

    private final DataProvider dataProvider;
    private AlbumListContract.View view;
    private final ArrayList<Album> albumList;

    private AlbumTrackListPresenter trackListPresenter;

    public AlbumListPresenter(Context context, AlbumListContract.View view, DataProvider dataProvider) {
        this.context = context;
        AppContainer container = ((MusicPlayerApp) context.getApplicationContext()).appContainer;

        this.dataProvider = dataProvider;
        this.view = view;
        this.executorService = container.executorService;
        this.mainThreadHandler = container.mainThreadHandler;

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
        AlbumTrackListFragment fragment = new AlbumTrackListFragment();
        Bundle args = new Bundle();
        args.putString("transition_name", transitionName);
        fragment.setArguments(args);

        AlbumTrackListPresenter presenter = new AlbumTrackListPresenter(new DataProvider(view.getContext(),
                executorService, mainThreadHandler), fragment, album, view.getContext());

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
    public void trackListLoadStarted() {
    }

    @Override
    public void onTrackListLoaded(ArrayList<MediaMetadataCompat> trackList) {
        PlaylistsBottomSheetFragment fragment = new PlaylistsBottomSheetFragment(trackList, context);

        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        fragment.show(manager, PlaylistsBottomSheetFragment.TAG);
    }

    @Override
    public void showBottomDialogFragment(int position) {
        DataProvider dataProvider = new DataProvider(context, executorService, mainThreadHandler);
        dataProvider.getTrackListAsynchronous(albumList.get(position), this);
    }
}
