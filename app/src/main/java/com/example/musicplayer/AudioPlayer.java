package com.example.musicplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    private final static String TAG = "AudioPlayer";
    private Context context;
    private MediaPlayer mediaPlayer;

    private AudioManager audioManager;
    private AudioAttributes playbackAttributes;
    private AudioFocusRequest focusRequest;
    private final Object focusLock = new Object();

    private MediaPlayer.OnSeekCompleteListener seekCompleteListener;
    private MediaPlayer.OnCompletionListener audioCompletedListener;

    private Uri currentAudioId;

    private boolean playBackDelayed = false;
    private boolean playbackNowAuthorized = false;
    private boolean resumeOnFocusGain = false;

    public AudioPlayer(Context context, MediaPlayer.OnSeekCompleteListener seekCompleteListener,
                        MediaPlayer.OnCompletionListener audioCompletedListener) {
        this.context = context;

        mediaPlayer = null;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        this.seekCompleteListener = seekCompleteListener;
        this.audioCompletedListener = audioCompletedListener;
    }

    private void createMediaPlayer() {

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(audioCompletedListener);
            mediaPlayer.setOnSeekCompleteListener(seekCompleteListener);
            mediaPlayer.setOnErrorListener(this);
        } else {
            mediaPlayer.reset();
        }
    }

    public void playAudio(Uri audioId) {

        if (mediaPlayer == null || audioId != currentAudioId) {

            releaseMediaPlayer();
            createMediaPlayer();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                mediaPlayer.setAudioAttributes(playbackAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this)
                        .build();

                int res = audioManager.requestAudioFocus(focusRequest);
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    playbackNowAuthorized = false;
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playbackNowAuthorized = true;
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    playbackNowAuthorized = false;
                    playBackDelayed = true;
                }
            }

            try {
                mediaPlayer.setDataSource(context, audioId);
                currentAudioId = audioId;

                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                Log.e(TAG, "Exception playing song");
            }
        }
    }

    public void seekTo(int mSeconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mSeconds);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        playbackNow();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer = null;
        }
    }

    public void playbackNow() {
        if (mediaPlayer != null && playbackNowAuthorized && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pausePlayback() {
        if (mediaPlayer != null && playbackNowAuthorized && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() { return mediaPlayer.getDuration(); }

    public int getCurrentPosition() { return mediaPlayer.getCurrentPosition(); }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                if (playBackDelayed || resumeOnFocusGain) {
                    synchronized (focusLock) {
                        playBackDelayed = false;
                        resumeOnFocusGain = false;
                    }
                    playbackNow();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized (focusLock) {
                    playBackDelayed = false;
                    resumeOnFocusGain = false;
                }
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                synchronized (focusLock) {
                    playBackDelayed = false;
                    resumeOnFocusGain = true;
                }
                pausePlayback();
                break;
        }
    }
}
