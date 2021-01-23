package com.example.musicplayer.tracklist;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.DataProvider;
import com.example.musicplayer.MusicDataProvider;
import com.example.musicplayer.MusicService;
import com.example.musicplayer.PlayerControlsFragment;
import com.example.musicplayer.R;

import java.util.ArrayList;

public class TrackListPresenter implements TrackListContract.Presenter,
        MusicDataProvider.GetTrackListCallback {

    public static final String TAG = "TrackListPresenter";

    private DataProvider dataProvider;
    private TrackListContract.View view;
    private Context context;
    private ArrayList<MediaMetadataCompat> trackList;

    private boolean playRequest = false;
    private int bufferedPosition;

    private boolean isPlaying;

    private MusicService.LocalBinder binder;
    private MediaControllerCompat controller;

    public TrackListPresenter(DataProvider dataProvider, TrackListContract.View view, Context context) {
        this.dataProvider = dataProvider;
        this.view = view;
        this.context = context;

        dataProvider.getTrackListAsynchronous(null, this);

        view.setPresenter(this);
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
            playerControlsFragment = new PlayerControlsFragment(trackList, position);
            transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom);
            transaction.add(R.id.container, playerControlsFragment, PlayerControlsFragment.FRAGMENT_TAG);
            transaction.commit();
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
    public int getCurrentPlayingPosition() {
        if (binder != null) {
            return binder.getCurrentQueuePosition();
        }
        return -1;
    }
}
