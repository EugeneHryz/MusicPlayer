package com.example.musicplayer.tracklist;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.DataProvider;
import com.example.musicplayer.MusicDataProvider;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.MusicService;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.PlaylistsBottomSheetFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.ServiceConnectionCallback;

import java.util.ArrayList;

public class TrackListPresenter implements TrackListContract.Presenter,
        MusicDataProvider.GetTrackListCallback, ServiceConnectionCallback {

    public static final String TAG = "TrackListPresenter";

    private final DataProvider dataProvider;
    private final TrackListContract.View view;
    private final Context context;
    private ArrayList<MediaMetadataCompat> trackList;

    private boolean playRequest = false;
    private int bufferedPosition;

    private boolean isPlaying;

    private MusicService.LocalBinder binder;
    private MediaControllerCompat controller;
    private final MediaControllerCompat.Callback controllerCallback;

    public TrackListPresenter(Context context, TrackListContract.View view) {
        this.context = context;
        this.dataProvider = ((MusicPlayerApp) context.getApplicationContext())
                .appContainer.dataProvider;
        this.view = view;

        dataProvider.getTrackListAsynchronous(null, this);

        view.setPresenter(this);
        controllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                    isPlaying = false;
                    view.rebindItems();
                } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    isPlaying = true;
                    view.rebindItems();
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                view.rebindItems();
            }
        };
    }

    @Override
    public MediaMetadataCompat getDataItem(int position) {
        MediaMetadataCompat metadata;
        while ((metadata = dataProvider.getTrack(position)) == null) {}
        return metadata;
    }

    @Override
    public void trackListLoadStarted() {
    }

    @Override
    public void onTrackListLoaded(ArrayList<MediaMetadataCompat> trackList) {
        this.trackList = trackList;
        if (playRequest) {

            playAfterTrackListLoaded(bufferedPosition);
            playRequest = false;
        }
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

    private void playAfterTrackListLoaded(int position) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        PlayerControlsFragment playerControlsFragment = (PlayerControlsFragment) manager.findFragmentByTag(PlayerControlsFragment.FRAGMENT_TAG);

        if (playerControlsFragment == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            playerControlsFragment = new PlayerControlsFragment(trackList, position, this);
            transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom);
            transaction.add(R.id.container, playerControlsFragment, PlayerControlsFragment.FRAGMENT_TAG);
            transaction.commit();
        } else {
            MediaSessionCompat.Callback callback = playerControlsFragment.getMediaSessionCallback();

            if (playerControlsFragment.getTrackQueue() != null &&
                    !(playerControlsFragment.getTrackQueue().equals(trackList))) {
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
    public String getCurrentMediaId() {
        if (binder != null) {
            return binder.getCurrentTrackMetadata()
                    .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        }
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MusicService.LocalBinder) service;
        controller = binder.getMediaController();

        controller.registerCallback(controllerCallback);
    }

    @Override
    public void onUnbind() {
        view.rebindItems();
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void showBottomDialogFragment(int position) {
        ArrayList<MediaMetadataCompat> trackList = new ArrayList<>();
        if (this.trackList != null) {
            trackList.add(this.trackList.get(position));
        }

        PlaylistsBottomSheetFragment fragment = new PlaylistsBottomSheetFragment(trackList, context);
        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
        fragment.showNow(manager, PlaylistsBottomSheetFragment.TAG);
    }
}
