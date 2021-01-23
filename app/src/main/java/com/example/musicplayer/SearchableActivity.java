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

    private ArrayList<MediaMetadataCompat> searchResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            searchResults = performSearch(getIntent().getStringExtra(SearchManager.QUERY));

            if (searchResults.size() > 0) {
                setListAdapter(new SearchResultsListAdapter());
            }
        }
    }

    private ArrayList<MediaMetadataCompat> performSearch(String query) {
        ArrayList<MediaMetadataCompat> searchResults = new ArrayList<>();

        String[] projection = new String[] {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Audio.Media.TITLE + " LIKE ? OR " + MediaStore.Audio.Media.ARTIST + " LIKE ?";
        String[] selectionArgs = new String[] { "%" + query + "%", "%" + query + "%" };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder);

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                String album = cursor.getString(albumColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                long albumId = cursor.getLong(albumIdColumn);
                long duration = cursor.getLong(durationColumn);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                Uri albumCoverUri;
                if (album == null || album.equals(MainActivity.defaultSongCollectionTilte)) {
                    albumCoverUri = Uri.parse("android.resource://com.example.musicplayer/" + R.drawable.music_note_icon);
                } else {
                    albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                }

                searchResults.add(metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, contentUri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumCoverUri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album).build());

            }
        }
        return searchResults;
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
            Glide.with(getApplicationContext()).load(searchResults.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)).
                    apply(new RequestOptions().centerCrop()).into(albumCover);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
