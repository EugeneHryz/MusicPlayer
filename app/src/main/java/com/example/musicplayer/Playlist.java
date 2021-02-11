package com.example.musicplayer;

public class Playlist {

    private final String playlistName;
    private final long playlistId;

    public Playlist(String playlistName, long playlistId) {
        this.playlistName = playlistName;
        this.playlistId = playlistId;
    }

    public String getName() {
        return playlistName;
    }

    public long getId() {
        return playlistId;
    }
}
