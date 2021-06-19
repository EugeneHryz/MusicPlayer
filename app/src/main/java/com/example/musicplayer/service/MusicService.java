package com.example.musicplayer.service;

import android.annotation.SuppressLint;
import android.app.Notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.view.KeyEvent;


import com.example.musicplayer.R;
import com.example.musicplayer.main.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements AudioFocusChangedCallback {

    public static final String TAG = "MusicService";

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private MediaSessionCompat.Callback callback;
    private AudioPlayer player;

    private PlaybackStateCompat playbackState;

    private NotificationManager notificationManager;
    private Notification notification;
    private BroadcastReceiver broadcastReceiver;

    private PendingIntent contentIntent;
    private PendingIntent deleteIntent;

    private ArrayList<MediaMetadataCompat> tracksMetadata;
    private int currentQueueIndex = 0;

    private static final int NOTIFICATION_ID = 1;

    private static final String NOTIFICATION_CHANNEL_ID = "com.example.musicplayer";
    private static final String CHANNEL_NAME = "Background service";

    public static final String LOAD_ACTION = "LOAD ACTIVITY";
    public static final String DELETE_ACTION = "DELETE_ACTION";

    public static final String ACTION_CMD = "ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String PLAY_CMD = "PLAY_CMD";
    public static final String PAUSE_CMD = "PAUSE_CMD";
    public static final String ARG_QUEUE = "ARG_QUEUE";
    public static final String ARG_INDEX = "ARG_INDEX";
    public static final String ACTION_TOGGLEPAUSE = "com.example.musicplayer.togglepause";
    public static final String ACTION_NEXT = "com.example.musicplayer.next";
    public static final String ACTION_PREV = "com.example.musicplayer.prev";

    public static final String MEDIA_SESSION_TAG = "MEDIA_SESSION";

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        player = new AudioPlayer(this, mp -> mediaSession
                .setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING)),
                mp -> callback.onSkipToNext(), this);

        stateBuilder = new PlaybackStateCompat.Builder();
        metadataBuilder = new MediaMetadataCompat.Builder();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleCommand(intent);
            }
        };

        MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state.getState() == PlaybackStateCompat.STATE_PAUSED ||
                        state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    notification = updateNotification();
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }
                if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                    stopForeground(false);
                } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    startForeground(NOTIFICATION_ID, notification);
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                notification = updateNotification();
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOGGLEPAUSE);
        filter.addAction(ACTION_PREV);
        filter.addAction(ACTION_NEXT);
        this.registerReceiver(broadcastReceiver, filter);

        callback = new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                mediaSession.setActive(true);
                if (!tracksMetadata.isEmpty()) {
                    player.playAudio(Uri.parse(tracksMetadata.get(currentQueueIndex).
                            getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
                    mediaSession.setMetadata(tracksMetadata.get(currentQueueIndex));
                }
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));
            }

            @Override
            public void onPause() {
                if (player.isPlaying()) {
                    player.pausePlayback();
                    mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));
                } else {
                    player.playbackNow();
                    mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));
                }
            }

            @Override
            public void onSkipToNext() {
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT));
                if (currentQueueIndex < tracksMetadata.size() - 1) {
                    currentQueueIndex++;
                } else {
                    currentQueueIndex = 0;
                }
                onPlay();
            }

            @Override
            public void onSkipToPrevious() {
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS));
                if (player.getCurrentPosition() < 7000) {
                    if (currentQueueIndex > 0) {
                        currentQueueIndex--;
                    } else {
                        currentQueueIndex = tracksMetadata.size() - 1;
                    }
                }
                onPlay();
            }

            @Override
            public void onStop() {
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_STOPPED));
                mediaSession.setActive(false);
                player.releaseMediaPlayer();
            }

            @Override
            public void onSkipToQueueItem(long id) {
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM));
                if (!tracksMetadata.isEmpty() && id >= 0 && id < tracksMetadata.size()) {
                    currentQueueIndex = (int)id;
                    onPlay();
                }
            }

            @Override
            public void onSeekTo(long pos) {
                player.seekTo((int) pos);
                mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_BUFFERING));
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent mediaEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (mediaEvent.getAction() == KeyEvent.ACTION_UP) {
                    int keyCode = mediaEvent.getKeyCode();
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            onSkipToNext();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            onSkipToPrevious();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            onPause();
                            break;
                    }
                }
                return true;
            }
        };

        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);
        mediaSession.setCallback(callback);
        mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_NONE));
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().build());

        mediaController = new MediaControllerCompat(this, mediaSession);
        mediaController.registerCallback(controllerCallback);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(LOAD_ACTION);
        contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this,  MusicService.class);
        deleteIntent.setAction(DELETE_ACTION);
        this.deleteIntent = PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = createNotification();
        } else
            notification = new Notification();

        startForeground(NOTIFICATION_ID, notification);
    }

    private PlaybackStateCompat createPlaybackState(int state) {
        long playbackPos = 0;
        if (state != PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM
                && state != PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
                && state != PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS
                && state != PlaybackStateCompat.STATE_STOPPED) {

            if (player.isPlaybackAuthorized()) {
                playbackPos = player.getCurrentPosition();
            }
        }
        playbackState = stateBuilder.setState(state, playbackPos, 1.0f).build();
        return playbackState;
    }

    @Override
    public void onFocusGained() {
        mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));
    }

    @Override
    public void onFocusLost() {
        mediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));
    }

    public class LocalBinder extends Binder {

        public MediaSessionCompat.Callback getMediaSessionCallback() {
            return callback;
        }

        public MediaControllerCompat getMediaController() { return mediaController; }

        public boolean isPlaying() { return player.isPlaying(); }

        public int getCurrentPosition() { return player.getCurrentPosition(); }

        public void setTracksMetadata(ArrayList<MediaMetadataCompat> tracksQueue) { tracksMetadata = tracksQueue; }

        public int getCurrentQueuePosition() {
            return currentQueueIndex;
        }

        public ArrayList<MediaMetadataCompat> getTrackQueue() { return tracksMetadata; }

        public MediaMetadataCompat getCurrentTrackMetadata() {
            return tracksMetadata.get(currentQueueIndex);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        IBinder binder = new LocalBinder();

        if (intent != null) {
            String action = intent.getAction();
            String command = intent.getStringExtra(CMD_NAME);
            if (action.equals(ACTION_CMD)) {
                if (command.equals(PAUSE_CMD)) {
                    callback.onPause();
                } else if (command.equals(PLAY_CMD)) {
                    Bundle args = intent.getExtras();
                    tracksMetadata = (ArrayList<MediaMetadataCompat>) args.getSerializable(ARG_QUEUE);
                    currentQueueIndex = args.getInt(ARG_INDEX);
                }
            }
        }
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void handleCommand(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_TOGGLEPAUSE:
                callback.onPause();
                break;
            case ACTION_NEXT:
                callback.onSkipToNext();
                break;
            case ACTION_PREV:
                callback.onSkipToPrevious();
                break;
        }
    }

    private boolean createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            return true;
        }
        return false;
    }

    private Notification createNotification() {
        createNotificationChannel();
        return updateNotification();
    }

    private Notification updateNotification() {
        Bitmap artwork = null;
        String title = null;
        String artist = null;
        if (tracksMetadata != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    artwork = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(),
                            Uri.parse(tracksMetadata.get(currentQueueIndex).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))));
                } else {
                    artwork = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            Uri.parse(tracksMetadata.get(currentQueueIndex).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            title = tracksMetadata.get(currentQueueIndex).getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            artist = tracksMetadata.get(currentQueueIndex).getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        }
        int togglePauseIcon = R.drawable.ic_round_pause_32;
        if (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            togglePauseIcon = R.drawable.ic_round_play_arrow_32;
        }
        int smallIcon = R.drawable.ic_music_note;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(artwork)
                .setContentTitle(title)
                .setContentText(artist)
                .setWhen(0)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(R.drawable.ic_round_skip_previous_32, "skip previous",
                        getPlaybackAction(ACTION_PREV))
                .addAction(togglePauseIcon, "toggle pause",
                        getPlaybackAction(ACTION_TOGGLEPAUSE))
                .addAction(R.drawable.ic_round_skip_next_32, "skip next",
                        getPlaybackAction(ACTION_NEXT));

        return builder.build();
    }

    private PendingIntent getPlaybackAction(String action) {
        Intent intent = new Intent();
        intent.setAction(action);

        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        callback.onStop();
        mediaSession.release();
        unregisterReceiver(broadcastReceiver);
    }
}
