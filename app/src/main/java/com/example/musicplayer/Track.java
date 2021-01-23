package com.example.musicplayer;

import android.net.Uri;

public class Track {

    private final String title;
    private final String artist;
    private final String album;
    private final long duration;
    private final Uri mediaUri;
    private final Uri albumArtUri;

    public Track(String title, String artist, String album, long duration, Uri mediaUri, Uri albumArtUri) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.mediaUri = mediaUri;
        this.albumArtUri = albumArtUri;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }
}
