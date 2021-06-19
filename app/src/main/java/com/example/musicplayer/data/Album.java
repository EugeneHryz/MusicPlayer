package com.example.musicplayer.data;

import android.net.Uri;

import java.io.Serializable;

public class Album implements Serializable {

    private final String title;
    private final String artist;
    private final Uri albumCoverUri;

    public Album(String title, String artist, Uri albumCoverUri) {

        this.title = title;
        this.artist = artist;
        this.albumCoverUri = albumCoverUri;
    }

    public String getTitle() { return title; }

    public String getArtist() { return artist; }

    public Uri getAlbumCoverUri() { return albumCoverUri; }
}
