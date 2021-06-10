package com.example.musicplayer;

import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class PlaylistDataProvider implements MusicDataProvider {
    private static final String TAG = "PlaylistDataProvider";

    private final Context context;

    private Executor executor;
    private Handler mainThreadHandler;
    private int itemCount = 0;

    private ArrayList<MediaMetadataCompat> trackList;

    private final AppContainer container;

    public PlaylistDataProvider(Context context) {
        this.context = context;

        trackList = new ArrayList<>();
        container = ((MusicPlayerApp) context.getApplicationContext()).appContainer;
    }

    public PlaylistDataProvider(Context context, Executor executor) {
        this(context);

        this.executor = executor;
    }

    public PlaylistDataProvider(Context context, Executor executor, Handler mainThreadHandler) {
        this(context, executor);
        this.mainThreadHandler = mainThreadHandler;
    }

    public Uri createPlaylist(String playlistName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);

        return context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
    }

    public Cursor queryAllPlaylists() {
        String[] projection = { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME };

        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
    }

    public Cursor queryTracksFromPlaylist(long playlistId) {
        String[] projection = { MediaStore.Audio.Playlists.Members.AUDIO_ID, MediaStore.Audio.Playlists.Members.PLAY_ORDER };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);

        return context.getContentResolver().query(uri, projection, null, null, null);
    }

    public void addTrackToPlaylist(long playlistId, ContentValues values, int requestCode) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);

        try {
            context.getContentResolver().insert(uri, values);
        } catch (SecurityException e) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RecoverableSecurityException rse;

                if (e instanceof RecoverableSecurityException) {

                    rse = (RecoverableSecurityException)e;
                    IntentSender intentSender = rse.getUserAction().getActionIntent()
                            .getIntentSender();
                    try {
                        ((AppCompatActivity) context).startIntentSenderForResult(intentSender, requestCode,
                                null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    public int deletePlaylist(long playlistId) {

        String localStringBuilder = "_id IN (" + (playlistId) + ")";

        return context.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                localStringBuilder, null);
    }

    public int deleteTrackFromPlaylist(long playlistId, MediaMetadataCompat metadata) {
        String trackId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        String selection = MediaStore.Audio.Playlists.Members._ID + "=?";
        String[] selectionArgs = { trackId };

        return context.getContentResolver().delete(uri, selection, selectionArgs);
    }

    public int getTrackPosition(MediaMetadataCompat metadata) {
        return trackList != null ? trackList.indexOf(metadata) : -1;
    }

    public void getTrackListAsynchronous(Playlist playlist, MusicDataProvider.GetTrackListCallback trackListCallback) {
        if (executor != null) {
            executor.execute(() -> {
                trackList = new ArrayList<>();
                getTrackListSynchronous(playlist, trackListCallback);
                if (mainThreadHandler != null) {
                    mainThreadHandler.post(() -> trackListCallback.onTrackListLoaded(trackList));
                }
            });
        }
    }

    public ArrayList<MediaMetadataCompat> getTrackListSynchronous(Playlist playlist, MusicDataProvider.GetTrackListCallback trackListCallback) {

        String[] projection = { MediaStore.Audio.Playlists.Members.TITLE, MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.ALBUM, MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.ALBUM_ID, MediaStore.Audio.Playlists.Members.DURATION };

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.getId());
        Cursor cursor = context.getContentResolver().query(uri,
                projection, null, null, null);

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

            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.TITLE);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ALBUM);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members._ID);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.ALBUM_ID);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.DURATION);

            while (cursor.moveToNext()) {
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String albumTitle = cursor.getString(albumColumn);
                long id = cursor.getLong(idColumn);
                long albumId = cursor.getLong(albumIdColumn);
                long duration = cursor.getLong(durationColumn);

                Uri mediaUri = ContentUris.withAppendedId(uri, id);
                Uri albumCoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                if (albumTitle == null || albumTitle.isEmpty()/* || !checkIfContentUriExists(albumCoverUri)*/) {
                    albumCoverUri = Uri.parse("android.resource://com.example.musicplayer/" + R.drawable.music_note_icon_light);
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

    @Override
    public int getItemCount() {
        return itemCount;
    }
}
