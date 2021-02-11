package com.example.musicplayer.playlistlist;

import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.example.musicplayer.Playlist;
import com.example.musicplayer.PlaylistDataProvider;
import com.example.musicplayer.R;
import com.example.musicplayer.playlisttracklist.PlaylistTrackListFragment;
import com.example.musicplayer.playlisttracklist.PlaylistTrackListPresenter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class PlaylistListPresenter implements PlaylistListContract.Presenter {
    private static final String TAG = "PlaylistListPresenter";

    private ExecutorService executorService;
    private Handler mainThreadHandler;

    private PlaylistDataProvider playlistDataProvider;
    private PlaylistListContract.View view;
    private ArrayList<Playlist> playlists;

    public PlaylistListPresenter(PlaylistDataProvider playlistDataProvider, PlaylistListContract.View view,
                                 ExecutorService executorService, Handler mainThreadHandler) {
        this.playlistDataProvider = playlistDataProvider;
        this.view = view;
        this.executorService = executorService;
        this.mainThreadHandler = mainThreadHandler;

        playlists = new ArrayList<>();
        Cursor cursor = playlistDataProvider.queryAllPlaylists();
        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);

                playlists.add(new Playlist(name, id));
            }
        }

        view.setPresenter(this);
    }

    @Override
    public Playlist getDataItem(int position) {
        if (position < playlists.size()) {
            return playlists.get(position);
        }
        return null;
    }

    @Override
    public int getDataItemCount() { return playlists.size(); }

    @Override
    public void addPlaylistTrackListFragment(android.view.View view, String transitionName, Playlist playlist) {
        PlaylistTrackListFragment fragment = new PlaylistTrackListFragment(transitionName);

        PlaylistTrackListPresenter presenter = new PlaylistTrackListPresenter(new PlaylistDataProvider(view.getContext(),
                executorService, mainThreadHandler), fragment, playlist, view.getContext());

        FragmentTransaction transaction = ((AppCompatActivity)view.getContext())
                .getSupportFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        androidx.transition.Transition sharedElementTransition = androidx.transition.TransitionInflater
                .from(view.getContext()).inflateTransition(R.transition.shared_image);
        fragment.setSharedElementEnterTransition(sharedElementTransition);
        Transition fadeTransition = TransitionInflater.from(view.getContext()).inflateTransition(R.transition.fade_in);
        fragment.setEnterTransition(fadeTransition);

        transaction.replace(R.id.fragment_container, fragment, PlaylistTrackListFragment.TAG);
        transaction.addSharedElement(view.findViewById(R.id.playlist_image), transitionName);
        transaction.addToBackStack(null).commit();
    }

    @Override
    public int getTracksNumber(long playlistId) {
        int tracksNumber = 0;
        Cursor cursor = playlistDataProvider.queryTracksFromPlaylist(playlistId);
        if (cursor != null) {
            tracksNumber = cursor.getCount();
            cursor.close();
        }
        return tracksNumber;
    }

    @Override
    public void deletePlaylist(long playlistId, int position) {
        Log.d(TAG, Integer.toString(playlistDataProvider.deletePlaylist(playlistId)));
        view.updateRecyclerView(position);
    }
}

