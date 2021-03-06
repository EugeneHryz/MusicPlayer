package com.example.musicplayer.playlistdialog;

import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.AppContainer;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.R;
import com.example.musicplayer.data.Playlist;
import com.example.musicplayer.data.PlaylistDataProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class PlaylistsBottomSheetFragment extends BottomSheetDialogFragment implements
        EnterPlaylistNameDialog.EnterPlaylistNameListener {
    public static final String TAG = "BottomSheetFragment";

    public static final int REQUEST_CODE = 3;

    private final Context context;

    private PlaylistDataProvider playlistDataProvider;
    private ArrayList<Playlist> playlists;
    private boolean[] checked;

    private final ArrayList<MediaMetadataCompat> trackList;

    private final AppContainer container;
    private final Executor executor;

    public PlaylistsBottomSheetFragment(ArrayList<MediaMetadataCompat> trackList, Context context) {
        super();

        this.context = context;
        this.trackList = trackList;
        container = ((MusicPlayerApp) context.getApplicationContext()).appContainer;
        executor = container.executorService;
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
        recyclerView.setAdapter(new PlaylistsAdapter(getContext()));

        Button doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            addTracksToPlaylists();

            dismiss();
        });

        Button addNewPlaylistButton = view.findViewById(R.id.add_new_playlist_button);
        addNewPlaylistButton.setOnClickListener(v -> {
            dismiss();
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

            EnterPlaylistNameDialog fragment = new EnterPlaylistNameDialog();
            fragment.setListener(this);
            fragment.show(manager, "Enter new playlist name");
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onNameEntered(long playlistId) {
        executor.execute(() -> {
            if (playlistDataProvider != null && trackList != null) {
                addTracksToPlaylist(playlistId);
            }
        });
        Toast.makeText(context, "Tracks added to playlist", Toast.LENGTH_SHORT).show();
    }

    private void addTracksToPlaylists() {
        executor.execute(() -> {
            if (playlistDataProvider != null && trackList != null) {
                for (int i = 0; i < playlists.size(); i++) {
                    if (checked[i]) {
                        long playlistId = playlists.get(i).getId();

                        addTracksToPlaylist(playlistId);
                    }
                }
            }
        });
        Toast.makeText(context, "Tracks added to playlist/s", Toast.LENGTH_SHORT).show();
    }

    private void addTracksToPlaylist(long playlistId) {
        Cursor cursor = playlistDataProvider.queryTracksFromPlaylist(playlistId);
        int base = 0;
        if (cursor != null && cursor.moveToLast()) {
            base = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)) + 1;
        }

        container.valuesToInsert = 0;
        container.playListId = playlistId;
        container.savedValues = new ArrayList<>();

        for (int j = 0; j < trackList.size(); j++) {
            long audioId = Long.parseLong(trackList.get(j)
                    .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));

            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);

            playlistDataProvider.addTrackToPlaylist(playlistId, values, REQUEST_CODE);
            base++;
        }
    }

    private class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {

        public PlaylistsAdapter(Context context) {
            playlistDataProvider = new PlaylistDataProvider(context);

            playlists = new ArrayList<>();

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
        public int getItemCount() { return playlists.size(); }
    }

    private static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        View view;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public View getView() { return view; }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
