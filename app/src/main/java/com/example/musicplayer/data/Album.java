package com.example.musicplayer.data;

import java.io.Serializable;

public class Album implements Serializable {

    private final String title;
    private final String artist;
    private final String albumCoverUri;

    public Album(String title, String artist, String albumCoverUri) {

        this.title = title;
        this.artist = artist;
        this.albumCoverUri = albumCoverUri;
    }

    public String getTitle() { return title; }

    public String getArtist() { return artist; }

    public String getAlbumCoverUri() { return albumCoverUri; }
}
