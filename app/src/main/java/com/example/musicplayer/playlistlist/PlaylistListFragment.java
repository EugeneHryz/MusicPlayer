package com.example.musicplayer.playlistlist;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer.Playlist;
import com.example.musicplayer.PlaylistDataProvider;
import com.example.musicplayer.R;
import com.example.musicplayer.SpacingItemDecoration;

import java.util.ArrayList;

public class PlaylistListFragment extends Fragment implements PlaylistListContract.View {
    public static final int SPAN_COUNT = 2;

    private RecyclerView recyclerView;
    private PlaylistListContract.Presenter presenter;
    private ArrayList<Playlist> playlists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerView((RecyclerView) view);
    }

    @Override
    public void setPresenter(PlaylistListPresenter presenter) {
        this.presenter = presenter;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT));
        recyclerView.setAdapter(new PlaylistRecyclerViewAdapter());
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration((int) getResources().getDimension(R.dimen.grid_item_spacing), SPAN_COUNT);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistListFragment.PlaylistRecyclerViewAdapter.PlaylistViewHolder> {
        private static final String TAG = "PlaylistAdapter";

        public PlaylistRecyclerViewAdapter() {
            super();
            playlists = new ArrayList<>();

            PlaylistDataProvider playlistDataProvider = new PlaylistDataProvider(getContext());
            Cursor cursor = playlistDataProvider.queryAllPlaylists();
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);

                    playlists.add(new Playlist(name, id));
                }
                cursor.close();
            }
        }

        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_playlist_card, parent, false);

            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            View view = holder.getView();

            Playlist playlist = playlists.get(position);
            if (playlist != null) {
                TextView playlistTitle = view.findViewById(R.id.playlist_name_card);
                TextView tracksNumber = view.findViewById(R.id.playlist_tracks_number);
                playlistTitle.setText(playlist.getName());

                int tracksNum = presenter.getTracksNumber(playlist.getId());
                String tracksNumberString = tracksNum + " ";
                if (tracksNum == 1) {
                    tracksNumberString += "track";
                } else {
                    tracksNumberString += "tracks";
                }
                tracksNumber.setText(tracksNumberString);

                ImageView imageView = view.findViewById(R.id.playlist_image);
                Glide.with(holder.getView().getContext()).load(R.drawable.music_note_icon_light)
                        .apply(new RequestOptions().centerCrop()).into(imageView);

                String transitionName = "playlist_name" + position;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setTransitionName(transitionName);
                }

                ImageButton playlistOptionsButton = view.findViewById(R.id.playlist_options_button);
                PopupMenu optionsMenu = new PopupMenu(getContext(), playlistOptionsButton);
                optionsMenu.getMenuInflater().inflate(R.menu.playlist_popup_menu, optionsMenu.getMenu());
                playlistOptionsButton.setOnClickListener(v -> optionsMenu.show());
                optionsMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.delete_action:
                            presenter.deletePlaylist(playlist.getId(), position);
                            break;
                    }
                    return false;
                });

                view.setOnClickListener((v) -> {
                    presenter.addPlaylistTrackListFragment(v, transitionName, playlist);
                });
            }
        }

        @Override
        public int getItemCount() {
            return playlists.size();
        }

        public class PlaylistViewHolder extends RecyclerView.ViewHolder {
            private final View view;

            public PlaylistViewHolder(View view) {
                super(view);
                this.view = view;
            }
            public View getView() { return view; }
        }
    }

    @Override
    public void updateRecyclerView(int position) {
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            playlists.remove(position);
            recyclerView.getAdapter().notifyItemRemoved(position);
        }
    }
}
