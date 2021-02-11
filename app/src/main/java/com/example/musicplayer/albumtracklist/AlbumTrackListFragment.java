package com.example.musicplayer.albumtracklist;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.musicplayer.Album;
import com.example.musicplayer.DividerItemDecoration;
import com.example.musicplayer.PlayerControlsFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.SearchableActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

public class AlbumTrackListFragment extends Fragment implements AlbumTrackListContract.View {
    public static final String TAG = "AlbumTrackListFragment";

    private AlbumTrackListContract.Presenter presenter;

    private int trackListSize;

    public AlbumTrackListFragment(String transitionName) {
        super();
        Bundle args = new Bundle();
        args.putString("transition_name", transitionName);
        this.setArguments(args);
        trackListSize = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_track_list, container, false);
    }

    @Override
    public void setPresenter(AlbumTrackListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_album_search, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_album_tracklist_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getContext(), SearchableActivity.class)));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag(PlayerControlsFragment.FRAGMENT_TAG);
                if (fragment != null) {
                    fragment.getView().setFocusableInTouchMode(true);
                    fragment.getView().requestFocus();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(v -> searchView.onActionViewCollapsed());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupChildViews();
    }

    @Override
    public void setupChildViews() {
        View view = getView();
        if (view != null && trackListSize == 0) {
            postponeEnterTransition();
            Album album = presenter.getAlbum();

            ImageView imageView = view.findViewById(R.id.album_cover_art);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setTransitionName(getArguments().getString("transition_name"));
            }
            Glide.with(view.getContext()).load(album.getAlbumCoverUri())
                    .placeholder(R.drawable.music_note_icon_light)
                    .into(new ImageViewTarget<Drawable>(imageView) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            this.setDrawable(resource);
                        }
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            startPostponedEnterTransition();
                            super.onResourceReady(resource, transition);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            startPostponedEnterTransition();
                            super.onLoadFailed(errorDrawable);
                        }
                    });

            ((TextView)view.findViewById(R.id.album_title)).setText(album.getTitle());
            String albumArtist = album.getArtist() + " " + "\u2022";
            ((TextView)view.findViewById(R.id.album_artist_name)).setText(albumArtist);

            trackListSize = presenter.getDataItemCount();

            String trackNumberText = presenter.getDataItemCount() + " " + (presenter.getDataItemCount() > 1 ?
                    getString(R.string.tracks_text) : getString(R.string.track_text));
            ((TextView)view.findViewById(R.id.track_number)).setText(trackNumberText);

            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setAdapter(new AlbumTrackRecyclerViewAdapter());
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext()));
            recyclerView.setHasFixedSize(true);

            MaterialToolbar toolbar = view.findViewById(R.id.album_tracklist_toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            setHasOptionsMenu(true);
            toolbar.setNavigationOnClickListener((v) -> {
                getActivity().onBackPressed();
            });

            AppBarLayout appBar = view.findViewById(R.id.app_bar_layout);
            appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                int appBarHeight = appBarLayout.getMeasuredHeight();
                int toolBarHeight = toolbar.getMeasuredHeight();
                float value = ((float)(appBarHeight - toolBarHeight + verticalOffset) / (float)(appBarHeight - toolBarHeight));
                imageView.setAlpha(value);
            });

            FloatingActionButton playAllButton = view.findViewById(R.id.play_all_button);
            playAllButton.setOnClickListener((v) -> {

            });
        }
    }

    private class AlbumTrackRecyclerViewAdapter extends RecyclerView.Adapter<AlbumTrackRecyclerViewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_album_track, parent, false);
            return new AlbumTrackRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            View view = holder.getView();

            MediaMetadataCompat metadata = presenter.getDataItem(position);

            ((TextView) view.findViewById(R.id.track_number))
                    .setText(String.format(Locale.getDefault(), "%2d", position + 1));
            TextView trackTitle = view.findViewById(R.id.track_title);
            trackTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            int minutes = (int) (metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 60000);
            int seconds = (int) ((metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) % 60000) / 1000);
            ((TextView) view.findViewById(R.id.track_duration))
                    .setText((minutes + ":" + String.format(Locale.getDefault(), "%02d", seconds)));

            ImageButton trackOptionsButton = view.findViewById(R.id.track_options_button);
            PopupMenu optionsMenu = new PopupMenu(getContext(), trackOptionsButton);
            optionsMenu.getMenuInflater().inflate(R.menu.track_popup_menu, optionsMenu.getMenu());

            trackOptionsButton.setOnClickListener(v -> optionsMenu.show());
            optionsMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.add_track_to_playlist_action:
                        presenter.showBottomDialogFragment(position);
                        break;
                }

                return false;
            });

            view.setOnClickListener((v) -> {
                presenter.play(position);
            });
        }

        @Override
        public int getItemCount() {
            return presenter.getDataItemCount();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View view;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;
            }
            public View getView() { return view; }
        }
    }
}
