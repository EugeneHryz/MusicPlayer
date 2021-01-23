package com.example.musicplayer.tracklist;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer.DividerItemDecoration;
import com.example.musicplayer.PlayerControlsFragment;
import com.example.musicplayer.R;

import java.util.ArrayList;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

public class TrackListFragment extends Fragment implements TrackListContract.View {
    public static final String TAG = "TrackListFragment";

    private TrackListPresenter presenter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_album_list, container);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new TrackRecyclerViewAdapter());
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext()));
        recyclerView.setHasFixedSize(true);
        Transition transition = TransitionInflater.from(getContext()).inflateTransition(R.transition.shared_image);
        setSharedElementReturnTransition(transition);

        return recyclerView;
    }

    @Override
    public void setPresenter(TrackListPresenter presenter) {
        this.presenter = presenter;
    }

    public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackViewHolder> {
        private static final String TAG = "TrackRecyclerAdapter";

        @NonNull
        @Override
        public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_track, parent, false);
            return new TrackViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
            View view = holder.getView();
            Log.d(TAG, "onBind... " + position);

            MediaMetadataCompat metadata = presenter.getDataItem(position);

            TextView trackTitle = view.findViewById(R.id.track_title);
            TextView trackArtistName = view.findViewById(R.id.track_artist_name);
            trackTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            trackArtistName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            ImageView imageView = view.findViewById(R.id.track_album_cover);
            Glide.with(view.getContext()).load(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)))
                    .placeholder(R.drawable.music_note_icon)
                    .apply(new RequestOptions().centerCrop())
                    .into(imageView);

            ImageButton trackOptionsButton = view.findViewById(R.id.track_options_button);

            PopupMenu optionsMenu = new PopupMenu(getContext(), trackOptionsButton);
            optionsMenu.getMenuInflater().inflate(R.menu.track_popup_menu, optionsMenu.getMenu());

            trackOptionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionsMenu.show();
                }
            });

            view.setOnClickListener((v) -> {
                presenter.play(position);
            });
        }

        @Override
        public int getItemCount() {
            return presenter.getDataItemCount();
        }
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public TrackViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
        }
        public View getView() { return view; }
    }
}
