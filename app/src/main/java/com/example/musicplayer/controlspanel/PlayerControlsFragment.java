package com.example.musicplayer.controlspanel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.musicplayer.MusicService;
import com.example.musicplayer.R;
import com.example.musicplayer.ServiceConnectionCallback;
import com.example.musicplayer.SlidingImageFragment;
import com.example.musicplayer.controlspanel.PlayerScreenMotionLayout;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerControlsFragment extends Fragment {

    private static final String TAG = "PlayerControlsFragment";
    public static final String FRAGMENT_TAG = "Playback controls fragment";

    private Fragment fragment;
    private ArrayList<MediaMetadataCompat> tracksMetadata;
    private int currentPosition;

    private ServiceConnection connection;
    private ServiceConnectionCallback connectionCallback;
    private boolean serviceConnected = false;

    private MusicService.LocalBinder binder;
    private MediaSessionCompat.Callback callback;
    private MediaControllerCompat controller;

    private Thread updateSeekBarThread;
    private boolean threadIsRunning = false;
    private boolean seekByUser;

    private PlayerScreenMotionLayout playerControlsLayout;
    private ViewPager2 viewPager;
    private SeekBar seekBar;
    private ImageButton togglePauseButton;
    private AnimatedVectorDrawable pauseToPlay;
    private AnimatedVectorDrawable playToPause;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private DragDownButton dragDown;
    private TextView elapsedTime;
    private TextView fullDuration;

    private TextView minTrackTitle;
    private TextView minArtistName;
    private ImageButton minNextButton;
    private ImageButton minTogglePauseButton;

    private TextView trackTitle;
    private TextView artistName;

    private boolean collapsed = true;

    private PlaybackStateCompat previousPlaybackState;

    public PlayerControlsFragment(ArrayList<MediaMetadataCompat> tracksMetadata, int position,
                                  ServiceConnectionCallback callback) {
        super();

        fragment = this;
        this.tracksMetadata = tracksMetadata;
        currentPosition = position;
        connectionCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (connectionCallback != null) {
                    connectionCallback.onServiceConnected(name, service);
                }
                serviceConnected = true;
                binder = (MusicService.LocalBinder) service;
                callback = binder.getMediaSessionCallback();
                controller = binder.getMediaController();

                controller.registerCallback(new MediaControllerCompat.Callback() {
                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {

                        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                            if (previousPlaybackState == null || previousPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
                                minTogglePauseButton.setImageResource(R.drawable.ic_round_pause_32);
                                togglePauseButton.setImageResource(R.drawable.play_to_pause);
                                playToPause = (AnimatedVectorDrawable) togglePauseButton.getDrawable();
                                playToPause.start();
                            }

                        } else if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                            minTogglePauseButton.setImageResource(R.drawable.ic_round_play_arrow_32);
                            togglePauseButton.setImageResource(R.drawable.pause_to_play);
                            pauseToPlay = (AnimatedVectorDrawable) togglePauseButton.getDrawable();
                            pauseToPlay.start();
                        }
                        if (state.getState() == PlaybackStateCompat.STATE_PAUSED ||
                                state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                            previousPlaybackState = state;
                        }
                    }

                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {

                        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                        trackTitle.setText(title);
                        artistName.setText(artist);
                        minTrackTitle.setText(title);
                        minArtistName.setText(artist);

                        if (viewPager.getScrollState() == ViewPager2.SCROLL_STATE_IDLE) {
                            viewPager.setCurrentItem(binder.getCurrentQueuePosition(), false);
                        }

                        seekBar.setProgress(0);
                        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                        seekBar.setMax((int) duration);
                        fullDuration.setText(convertMsToString((int)duration));

                        if (!updateSeekBarThread.isAlive()) {
                            updateSeekBarThread.start();
                        }
                    }
                });

                nextButton.setOnClickListener(v -> {
                    callback.onSkipToNext();
                });
                prevButton.setOnClickListener(v -> {
                    callback.onSkipToPrevious();
                });
                togglePauseButton.setOnClickListener(v -> {
                    callback.onPause();
                });
                minNextButton.setOnClickListener(v -> {
                    callback.onSkipToNext();
                });
                minTogglePauseButton.setOnClickListener(v -> {
                    callback.onPause();
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int seekProgress;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekProgress = progress;
                        elapsedTime.setText(convertMsToString(seekProgress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        seekByUser = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        callback.onSeekTo(seekProgress);
                        elapsedTime.setText(convertMsToString(seekProgress));
                        seekByUser = false;
                    }
                });

                callback.onPlay();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                serviceConnected = false;
            }
        };

        updateSeekBarThread = new Thread(() -> {
            threadIsRunning = true;

            while (threadIsRunning) {
                if (binder.isPlaying() && !seekByUser) {
                    int position = binder.getCurrentPosition();
                    seekBar.setProgress(position);
                    String finalElapsed = convertMsToString(position);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> elapsedTime.setText(finalElapsed));
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    threadIsRunning = false;
                    e.printStackTrace();
                }
            }
        });
    }

    private String convertMsToString(int ms) {
        String elapsed = ms / 60000 + ":";
        int seconds = (ms % 60000) / 1000;
        if (seconds < 10)
            elapsed += "0";
        elapsed += Integer.toString(seconds);
        return elapsed;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_player_motion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerControlsLayout = (PlayerScreenMotionLayout) view;
        playerControlsLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                seekBar.setEnabled(true);
                minNextButton.setEnabled(false);
                minTogglePauseButton.setEnabled(false);
                collapsed = false;

                if (controller != null) {
                    if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        togglePauseButton.setImageResource(R.drawable.ic_round_pause_24);
                    } else if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                        togglePauseButton.setImageResource(R.drawable.ic_round_play_arrow_24);
                    }
                }
            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) { }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if (i == R.id.collapsed) {
                    collapsed = true;
                    seekBar.setEnabled(false);
                    minNextButton.setEnabled(true);
                    minTogglePauseButton.setEnabled(true);

                } else if (i == R.id.gone) {
                    deletePlayerControls();

                    if (connectionCallback != null) {
                        connectionCallback.onUnbind();
                    }
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) { }
        });

        viewPager = view.findViewById(R.id.album_cover_picture);
        ImageSliderAdapter adapter = new ImageSliderAdapter(getActivity());
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            private boolean newPageSelected = false;
            private boolean userStartedDrag = false;
            private long bufferedPosition = 0;

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (newPageSelected) {
                        callback.onSkipToQueueItem(bufferedPosition);
                        newPageSelected = false;
                    }
                    userStartedDrag = false;
                } else if (state == ViewPager2.SCROLL_STATE_DRAGGING) {

                    userStartedDrag = true;
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (userStartedDrag) {
                    newPageSelected = true;
                    bufferedPosition = position;
                }
            }
        });
        viewPager.setPageTransformer(new MarginPageTransformer(100));

        seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setEnabled(false);
        togglePauseButton = view.findViewById(R.id.play_pause_button);
        nextButton = view.findViewById(R.id.next_track_button);
        prevButton = view.findViewById(R.id.prev_track_button);
        trackTitle = view.findViewById(R.id.track_title);
        artistName = view.findViewById(R.id.artist_name);
        elapsedTime = view.findViewById(R.id.elapsed_time);
        fullDuration = view.findViewById(R.id.full_duration);

        dragDown = view.findViewById(R.id.drag_down);
        dragDown.setMotionLayout(playerControlsLayout);

        minTogglePauseButton = view.findViewById(R.id.min_play_pause_button);
        minNextButton = view.findViewById(R.id.min_next_track_button);
        minTrackTitle = view.findViewById(R.id.min_track_title);
        minArtistName = view.findViewById(R.id.min_artist_name);

        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_CMD);
        intent.putExtra(MusicService.CMD_NAME, MusicService.PLAY_CMD);

        Bundle args = new Bundle();
        args.putSerializable(MusicService.ARG_QUEUE, tracksMetadata);
        args.putInt(MusicService.ARG_INDEX, currentPosition);
        intent.putExtras(args);
        Objects.requireNonNull(getContext()).bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && !collapsed) {
                playerControlsLayout.transitionToStart();
                return true;
            }
            return false;
        });
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return callback;
    }

    public void setTrackQueue(ArrayList<MediaMetadataCompat> trackQueue) {
        if (binder != null) {
            binder.setTracksMetadata(trackQueue);
        }
        tracksMetadata = trackQueue;
    }

    public void updateViewPager() {
        viewPager.setAdapter(new ImageSliderAdapter(Objects.requireNonNull(getActivity())));
    }

    public ArrayList<MediaMetadataCompat> getTrackQueue() {
        if (binder != null) {
            return binder.getTrackQueue();
        }
        return null;
    }

    public void deletePlayerControls() {
        Objects.requireNonNull(getContext()).unbindService(connection);

        FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(PlayerControlsFragment.this).commit();
    }

    private class ImageSliderAdapter extends FragmentStateAdapter {

        public ImageSliderAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new SlidingImageFragment(viewPager, playerControlsLayout,
                    Uri.parse(tracksMetadata.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)));
        }

        @Override
        public int getItemCount() {
            return tracksMetadata.size();
        }
    }
}
