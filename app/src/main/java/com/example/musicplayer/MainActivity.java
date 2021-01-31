package com.example.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.albumtracklist.AlbumTrackListFragment;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    private static final String TAG = "MainActivity1";
    public static final String defaultSongCollectionTilte = "Download";
    private Fragment mainFragment;

    private ExecutorService executorService;
    private Handler mainThreadHandler;

    private long backPressedElapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newFixedThreadPool(10);
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED) {
            addInitialFragment();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    private void addInitialFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        TabViewFragment tabViewFragment = new TabViewFragment(executorService, mainThreadHandler);
        mainFragment = tabViewFragment;
        fragmentTransaction.add(R.id.fragment_container, tabViewFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addInitialFragment();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (action != null && action.equals(SearchableActivity.PLAY_ACTION)) {
            Bundle args = intent.getExtras();

            ArrayList<MediaMetadataCompat> trackQueue = (ArrayList<MediaMetadataCompat>) args.getSerializable(SearchableActivity.QUEUE_KEY);
            int position = args.getInt(SearchableActivity.POSITION_KEY);

            FragmentManager manager = getSupportFragmentManager();
            PlayerControlsFragment fragment = (PlayerControlsFragment)
                    manager.findFragmentByTag(PlayerControlsFragment.FRAGMENT_TAG);

            if (fragment == null) {
                FragmentTransaction transaction = manager.beginTransaction();
                fragment = new PlayerControlsFragment(trackQueue, position, null);
                transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom);
                transaction.replace(R.id.container, fragment, PlayerControlsFragment.FRAGMENT_TAG);
                transaction.commit();
            } else {
                MediaSessionCompat.Callback callback = fragment.getMediaSessionCallback();
                fragment.setTrackQueue(trackQueue);
                fragment.updateViewPager();
                callback.onSkipToQueueItem(position);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag(AlbumTrackListFragment.TAG) == null) {
            if (backPressedElapsedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getApplicationContext(), "Press again to quit", Toast.LENGTH_SHORT).show();
            }
            backPressedElapsedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}