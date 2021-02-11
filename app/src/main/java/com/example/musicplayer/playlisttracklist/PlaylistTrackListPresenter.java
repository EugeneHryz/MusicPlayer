package com.example.musicplayer.playlisttracklist;

import android.content.ContentUris;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.Album;
import com.example.musicplayer.DataProvider;
import com.example.musicplayer.MusicDataProvider;
import com.example.musicplayer.PlayerControlsFragment;
import com.example.musicplayer.Playlist;
import com.example.musicplayer.PlaylistDataProvider;
import com.example.musicplayer.R;
import com.example.musicplayer.albumtracklist.AlbumTrackListContract;

import java.util.ArrayList;

public class PlaylistTrackListPresenter implements PlaylistTrackListContract.Presenter,
        MusicDataProvider.GetTrackListCallback {
    public static final String TAG = "PlaylistTrackListPresenter";

    private final Context context;
    private final PlaylistDataProvider playlistDataProvider;
    private final Playlist playlist;
    private ArrayList<MediaMetadataCompat> trackList;

    private boolean playRequest = false;
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
    }

    private void playAfterTrackListLoaded(int position) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
        PlayerControlsFragment playerControlsFragment = (PlayerControlsFragment) manager.findFragmentByTag(PlayerControlsFragment.FRAGMENT_TAG);

        if (playerControlsFragment == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            playerControlsFragment = new PlayerControlsFragment(trackList, position, null);
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
        return playlistDataProvider.getItemCount();
    }

    @Override
    public void deleteTrackFromPlaylist(MediaMetadataCompat trackMetadata, int position) {
        playlistDataProvider.deleteTrackFromPlaylist(playlist.getId(),
                Long.parseLong(trackMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        playlistDataProvider.getTrackListSynchronous(playlist, this);
        view.updateRecyclerView(position);
    }
}

