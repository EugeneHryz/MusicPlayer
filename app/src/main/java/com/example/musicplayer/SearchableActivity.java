package com.example.musicplayer;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity {

    private static final String TAG = "SearchableActivity";

    public static final String PLAY_ACTION = "play action";
    public static final String QUEUE_KEY = "queue key";
    public static final String POSITION_KEY = "position key";

    private DataProvider dataProvider;
    private ArrayList<MediaMetadataCompat> searchResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        dataProvider = new DataProvider(getApplicationContext());

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            searchResults = dataProvider.getTrackListSynchronous(null,
                    getIntent().getStringExtra(SearchManager.QUERY), null);

            if (searchResults.size() > 0) {
                setListAdapter(new SearchResultsListAdapter());
            }
        }
    }

    private class SearchResultsListAdapter implements ListAdapter {

        public SearchResultsListAdapter() {
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public int getCount() {
            return searchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_item_track, parent, false);
            }
            TextView trackTitle = view.findViewById(R.id.track_title);
            TextView artistName = view.findViewById(R.id.track_artist_name);
            ImageView albumCover = view.findViewById(R.id.track_album_cover);

            trackTitle.setText(searchResults.get(position).getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistName.setText(searchResults.get(position).getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            Glide.with(getApplicationContext())
                    .load(searchResults.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                    .placeholder(R.drawable.music_note_icon_light)
                    .apply(new RequestOptions().centerCrop())
                    .into(albumCover);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setAction(PLAY_ACTION);

                    Bundle args = new Bundle();
                    args.putSerializable(QUEUE_KEY, searchResults);
                    args.putInt(POSITION_KEY, position);
                    intent.putExtras(args);
                    startActivity(intent);
                    onBackPressed();
                }
            });
            return view;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return searchResults.size();
        }

        @Override
        public boolean isEmpty() {
            return searchResults.isEmpty();
        }
    }
}
