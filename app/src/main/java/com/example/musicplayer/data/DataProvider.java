package com.example.musicplayer.data;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.example.musicplayer.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class DataProvider implements MusicDataProvider {

    public static final String TAG = "DataProvider";

    private Cursor cursor;
    private final Context context;
    private Executor executor;
    private Handler mainThreadHandler;
    private int itemCount;

    private final ArrayList<MediaMetadataCompat> trackList;

    public DataProvider(Context context) {
        this.context = context;

        trackList = new ArrayList<>();
    }

    public DataProvider(Context context, Executor executor) {
        this(context);

        this.executor = executor;
    }

    public DataProvider(Context context, Executor executor, Handler mainThreadHandler) {
        this(context, executor);

        this.mainThreadHandler = mainThreadHandler;
    }

    public ArrayList<Album> getAlbumListSynchronous() {
        String[] projection = { MediaStore.Audio.Albums.ALBUM_ID,  MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST };
        cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection,
                null, null, null);

        ArrayList<Album> albumList = null;
        if (cursor != null) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);

            albumList = new ArrayList<>();
            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);

                if (!hasAlbumWithTitle(albumList, title)) {
                    albumList.add(new Album(title, artist, albumCoverUri.toString()));
                }
            }
        }
        if (albumList != null) {
            itemCount = albumList.size();
        }

        return albumList;
    }

    private boolean hasAlbumWithTitle(ArrayList<Album> albumList, String albumTitle) {
        for (Album album : albumList) {
            if (album.getTitle().equals(albumTitle)) {
                return true;
            }
        }
        return false;
    }

    public void getTrackListAsynchronous(Album album, MusicDataProvider.GetTrackListCallback trackListCallback) {
        if (executor != null) {
            executor.execute(() -> {
                getTrackListSynchronous(album, null, trackListCallback);
                if (mainThreadHandler != null) {
                    mainThreadHandler.post(() -> trackListCallback.onTrackListLoaded(trackList));
                }
            });
        }
    }

    public ArrayList<MediaMetadataCompat> getTrackListSynchronous(Album album, String searchQuery,
                                                                   MusicDataProvider.GetTrackListCallback trackListCallback) {
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION};
        String selection = null;
        String[] selectionArgs = null;
        if (album != null) {
            selection = MediaStore.Audio.Media.ALBUM + "=?";
            selectionArgs = new String[]{album.getTitle()};
        }
        if (searchQuery != null) {
            selection = MediaStore.Audio.Media.TITLE + " LIKE ? OR " + MediaStore.Audio.Media.ARTIST + " LIKE ?";
            selectionArgs = new String[]{"%" + searchQuery + "%", "%" + searchQuery + "%"};
        }
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);
        if (mainThreadHandler != null) {
            mainThreadHandler.post(() -> {
                if (trackListCallback != null) {
                    trackListCallback.trackListLoadStarted();
                }
            });
        }
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        if (cursor != null) {
            itemCount = cursor.getCount();

            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (!cursor.isClosed() && cursor.moveToNext()) {
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String albumTitle = cursor.getString(albumColumn);
                long id = cursor.getLong(idColumn);
                long albumId = cursor.getLong(albumIdColumn);
                long duration = cursor.getLong(durationColumn);

                Uri mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                if (searchQuery == null) {
                    if (albumTitle == null || albumTitle.isEmpty() || !checkIfContentUriExists(albumCoverUri)) {
                        albumCoverUri = Uri.parse("android.resource://com.example.musicplayer/" + R.drawable.music_note_icon_light);
                    }
                }

                trackList.add(metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumCoverUri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, Long.toString(id))
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .build());
        }
            cursor.close();
        }
        return trackList;
    }

    public MediaMetadataCompat getTrack(int position) {
        if (position < trackList.size()) {
            return trackList.get(position);
        }
        return null;
    }

    private boolean checkIfContentUriExists(Uri uri) {
        boolean result = true;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            inputStream.close();
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }
}
