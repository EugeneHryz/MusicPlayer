package com.example.musicplayer.albumtracklist;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.Album;
import com.example.musicplayer.DataProvider;
import com.example.musicplayer.MusicDataProvider;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.PlaylistsBottomSheetFragment;
import com.example.musicplayer.R;

import java.util.ArrayList;

public class AlbumTrackListPresenter implements AlbumTrackListContract.Presenter,
        MusicDataProvider.GetTrackListCallback {

    public static final String TAG = "AlbumTrackListPresenter";

    private final Context context;
    private final DataProvider dataProvider;
    private final Album album;
    private ArrayList<MediaMetadataCompat> trackList;

    private boolean playRequest = false;
    private int bufferedPosition;

    private AlbumTrackListContract.View view;

    public AlbumTrackListPresenter(DataProvider dataProvider, AlbumTrackListContract.View view,
                                      Album album, Context context) {
        this.dataProvider = dataProvider;
        this.context = context;
        this.album = album;
        this.view = view;

        dataProvider.getTrackListAsynchronous(album, this);

        view.setPresenter(this);
    }

    @Override
    public MediaMetadataCompat getDataItem(int position) {
        MediaMetadataCompat metadata;
        while ((metadata = dataProvider.getTrack(position)) == null);
        return metadata;
    }

    @Override
    public void play(int position) {
        if (trackList == null) {
            playRequest = true;
            bufferedPosition = position;
        } else {
            playAfterTrackListLoaded(position);
        }
    }

    @Override
    public void trackListLoadStarted() {
        view.setupChildViews();
    }

    @Override
    public void onTrackListLoaded(ArrayList<MediaMetadataCompat> trackList) {
        this.trackList = trackList;
        if (playRequest) {
            playAfterTrackListLoaded(bufferedPosition);
            playRequest = false;
        }
    }

    private void playAfterTrackListLoaded(int position) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        PlayerControlsFragment playerControlsFragment = (PlayerControlsFragment)
                manager.findFragmentByTag(PlayerControlsFragment.TAG);

        if (playerControlsFragment == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            playerControlsFragment = new PlayerControlsFragment(trackList, position, null);
            transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom)
                    .add(R.id.container, playerControlsFragment, PlayerControlsFragment.TAG)
                    .commit();
        } else {
            MediaSessionCompat.Callback callback = playerControlsFragment.getMediaSessionCallback();

            if (!(playerControlsFragment.getTrackQueue().equals(trackList))) {
                playerControlsFragment.setTrackQueue(trackList);
                playerControlsFragment.updateViewPager();
            }
            callback.onSkipToQueueItem(position);
        }
    }

    @Override
    public int getDataItemCount() {
        return dataProvider.getItemCount();
    }

    @Override
    public Album getAlbum() {
        return album;
    }

    @Override
    public void showBottomDialogFragment(int position) {
        ArrayList<MediaMetadataCompat> trackList = new ArrayList<>();
        if (this.trackList != null) {
            trackList.add(this.trackList.get(position));
        }

        PlaylistsBottomSheetFragment fragment = new PlaylistsBottomSheetFragment(trackList, context);
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        fragment.showNow(manager, PlaylistsBottomSheetFragment.TAG);
    }
}