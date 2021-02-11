package com.example.musicplayer;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.albumlist.AlbumListContract;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistsBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = "BottomSheetFragment";

    private PlaylistDataProvider playlistDataProvider;
    private ArrayList<Playlist> playlists;
    private boolean[] checked;

    private ArrayList<MediaMetadataCompat> trackList;

    public PlaylistsBottomSheetFragment(ArrayList<MediaMetadataCompat> trackList) {
        super();

        this.trackList = trackList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlists_bottom_sheet_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.playlists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PlaylistsAdapter());

        Button doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            addTracksToPlaylist();

            dismiss();
        });

        Button addNewPlaylistButton = view.findViewById(R.id.add_new_playlist_button);
        addNewPlaylistButton.setOnClickListener(v -> {
            dismiss();
            FragmentManager manager = getActivity().getSupportFragmentManager();

            EnterPlaylistNameDialogFragment fragment = new EnterPlaylistNameDialogFragment();
            fragment.show(manager, "enter new playlist name");
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void addTracksToPlaylist() {

            if (playlistDataProvider != null && trackList != null) {
                for (int i = 0; i < playlists.size(); i++) {

                    if (checked[i]) {
                        long playlistId = playlists.get(i).getId();

                        Cursor cursor = playlistDataProvider.queryTracksFromPlaylist(playlistId);
                        int playOrder = 0;
                        if (cursor != null && cursor.moveToLast()) {
                            playOrder = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)) + 1;
                        }

                        for (int j = 0; j < trackList.size(); j++) {
                            playlistDataProvider.addTrackToPlaylist(playlistId,
                                    trackList.get(j), playOrder, 0);
                            playOrder++;
                        }
                    }
                }
            }
        /*else {

            if (playlistDataProvider != null && trackList != null) {
                Playlist firstCheckedPlaylist = null;
                for (int i = 0; firstCheckedPlaylist == null && i < playlists.size(); i++) {
                    if (checked[i]) {
                        firstCheckedPlaylist = playlists.get(i);
                    }
                }

                if (firstCheckedPlaylist != null) {
                    for (int i = 0; i < trackList.size(); i++) {
                        playlistDataProvider.addTrackToPlaylist(firstCheckedPlaylist.getPlaylistId(),
                                trackList.get(i), 0, i);
                    }
                }
            }
        }*/
    }

    private class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {

        public PlaylistsAdapter() {
            playlistDataProvider = new PlaylistDataProvider(getContext());

            playlists = new ArrayList<>();
            // quering all playlists
            Cursor cursor = playlistDataProvider.queryAllPlaylists();
            if (cursor != null) {
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameColumn);
                    long id = cursor.getLong(idColumn);

                    playlists.add(new Playlist(name, id));
                }
                checked = new boolean[playlists.size()];
                cursor.close();
            }
        }

        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_playlist, parent, false);
            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            Playlist playlist = playlists.get(position);
            View view = holder.getView();

            TextView playlistName = view.findViewById(R.id.playlist_name);
            playlistName.setText(playlist.getName());

            CheckBox checkBox = view.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checked[position] = isChecked);

            view.setOnClickListener(v -> checkBox.toggle());
        }

        @Override
        public int getItemCount() {
            return playlists.size();
        }
    }

    private class PlaylistViewHolder extends RecyclerView.ViewHolder {
        View view;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
        public View getView() { return view; }
    }

    /*public void addTrackToPlaylists(int requestCode) {
        if (trackList != null && requestCode < trackList.size()) {
            for (int i = 0; i < playlists.size(); i++) {
                if (checked[i]) {
                    long playlistId = playlists.get(i).getPlaylistId();

                    Cursor cursor = playlistDataProvider.queryTracksFromPlaylist(playlistId);
                    int playOrder = 0;
                    if (cursor != null && cursor.moveToLast()) {
                        playOrder = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)) + 1;
                    }

                    playlistDataProvider.addTrackToPlaylist(playlistId, trackList.get(requestCode), playOrder, -1);
                }
            }
        }
    }*/

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
