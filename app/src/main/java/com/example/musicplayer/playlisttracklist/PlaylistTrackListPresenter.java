package com.example.musicplayer.playlisttracklist;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.data.MusicDataProvider;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.data.Playlist;
import com.example.musicplayer.data.PlaylistDataProvider;
import com.example.musicplayer.R;

import java.util.ArrayList;

public class PlaylistTrackListPresenter implements PlaylistTrackListContract.Presenter,
        MusicDataProvider.GetTrackListCallback {
    public static final String TAG = "PlaylistTrackListPresenter";

    private final Context context;
    private final PlaylistDataProvider playlistDataProvider;
    private final Playlist playlist;
    private ArrayList<MediaMetadataCompat> trackList;

    private boolean playRequest;
    private boolean playlistQueueUpdated = true;
    private int bufferedPosition;

    private final PlaylistTrackListContract.View view;

    public PlaylistTrackListPresenter(PlaylistDataProvider playlistDataProvider,
                                   PlaylistTrackListContract.View view, Playlist playlist, Context context) {
        this.playlistDataProvider = playlistDataProvider;
        this.context = context;
        this.playlist = playlist;
        this.view = view;

        playlistDataProvider.getTrackListAsynchronous(playlist, this);

        view.setPresenter(this);
    }

    @Override
    public MediaMetadataCompat getDataItem(int position) {
        MediaMetadataCompat metadata;
        while ((metadata = playlistDataProvider.getTrack(position)) == null);
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
    public void playAll() {
        play(0);
    }

    @Override
    public Playlist getPlaylist() {
        return playlist;
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
        if (!playlistQueueUpdated) {
            updatePlaylistQueue();
            playlistQueueUpdated = true;
        }
        view.setTrackNumber(trackList.size());
    }

    private void playAfterTrackListLoaded(int position) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        PlayerControlsFragment fragment = (PlayerControlsFragment) manager.findFragmentByTag(PlayerControlsFragment.TAG);

        if (fragment == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            fragment = new PlayerControlsFragment(trackList, position, null);
            transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom);
            transaction.add(R.id.container, fragment, PlayerControlsFragment.TAG);
            transaction.commit();
        } else {
            MediaSessionCompat.Callback callback = fragment.getMediaSessionCallback();

            updateTrackQueue(fragment);
            callback.onSkipToQueueItem(position);
        }
    }

    private void updatePlaylistQueue() {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        PlayerControlsFragment fragment = (PlayerControlsFragment) manager.findFragmentByTag(PlayerControlsFragment.TAG);

        if (fragment != null) {
            updateTrackQueue(fragment);

            if (trackList.isEmpty()) {
                fragment.deletePlayerControls(true);
            }
        }
    }

    private void updateTrackQueue(PlayerControlsFragment fragment) {
        if (trackList.size() > 0) {
            if (!(fragment.getTrackQueue().equals(trackList))) {
                fragment.setTrackQueue(trackList);
                fragment.updateViewPager();
            }
        }
    }

    @Override
    public int getDataItemCount() {
        return playlistDataProvider.getItemCount();
    }

    @Override
    public void deleteTrackFromPlaylist(MediaMetadataCompat trackMetadata, int position) {
        playlistDataProvider.deleteTrackFromPlaylist(playlist.getId(), trackMetadata);
        trackList = null;
        playlistQueueUpdated = false;
        playlistDataProvider.getTrackListAsynchronous(playlist, this);

        view.updateRecyclerView(position);
    }

    @Override
    public int updateTrackPosition(MediaMetadataCompat metadata) {
        return trackList.indexOf(metadata);
    }
}

