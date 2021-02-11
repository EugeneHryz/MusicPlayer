package com.example.musicplayer.albumlist;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.example.musicplayer.Album;
import com.example.musicplayer.R;
import com.example.musicplayer.SpacingItemDecoration;

public class AlbumListFragment extends Fragment implements AlbumListContract.View {
    private static final String TAG = "AlbumListFragment";

    private RecyclerView recyclerView;
    private AlbumListContract.Presenter presenter;

    public static final int SPAN_COUNT = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);

        setupRecyclerView();

        return recyclerView;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), SPAN_COUNT));
        recyclerView.setAdapter(new AlbumRecyclerViewAdapter());
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration((int) getResources().getDimension(R.dimen.grid_item_spacing), SPAN_COUNT);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void setPresenter(AlbumListPresenter presenter) {
        this.presenter = presenter;
    }

    private class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.AlbumViewHolder> {

        private  static final String TAG = "AlbumListAdapter";

        @NonNull
        @Override
        public AlbumRecyclerViewAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_album, parent, false);

            return new AlbumRecyclerViewAdapter.AlbumViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumRecyclerViewAdapter.AlbumViewHolder holder, int position) {
            View view = holder.getView();

            Album album = presenter.getDataItem(position);
            if (album != null) {
                TextView albumTitle = view.findViewById(R.id.album_title);
                TextView albumArtistName = view.findViewById(R.id.album_artist_name);
                albumTitle.setText(album.getTitle());
                albumArtistName.setText(album.getArtist());

                ImageView imageView = view.findViewById(R.id.album_cover);
                Glide.with(holder.getView().getContext()).load(album.getAlbumCoverUri())
                        .placeholder(R.drawable.music_note_icon_light)
                        .apply(new RequestOptions().centerCrop()).into(imageView);

                String transitionName = "album_cover" + position;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setTransitionName(transitionName);
                }

                view.setOnClickListener((v) -> {
                    presenter.addAlbumTrackListFragment(v, transitionName, album);
                });
            }

            ImageButton albumOptionsButton = view.findViewById(R.id.album_options_button);
            PopupMenu optionsMenu = new PopupMenu(getContext(), albumOptionsButton);
            optionsMenu.getMenuInflater().inflate(R.menu.album_popup_menu, optionsMenu.getMenu());
            albumOptionsButton.setOnClickListener(v -> optionsMenu.show());
            optionsMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.add_to_playlist_action:
                        presenter.showBottomDialogFragment(position, getContext());
                        break;
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return presenter.getDataItemCount();
        }

        public class AlbumViewHolder extends RecyclerView.ViewHolder {
            private final View view;

            public AlbumViewHolder(View view) {
                super(view);
                this.view = view;
            }
            public View getView() { return view; }
        }
    }
}
