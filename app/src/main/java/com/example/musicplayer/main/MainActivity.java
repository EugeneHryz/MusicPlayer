package com.example.musicplayer.main;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musicplayer.AppContainer;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.data.PlaylistDataProvider;
import com.example.musicplayer.playlistdialog.PlaylistsBottomSheetFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.albumtracklist.AlbumTrackListFragment;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.data.DataProvider;
import com.example.musicplayer.playlisttracklist.PlaylistTrackListFragment;
import com.example.musicplayer.search.SearchableActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    public static final String TAG = "MainActivity1";

    private long backPressedElapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            addInitialFragment();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_CODE);
            }
        }
        updateAppContainer();
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
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (action != null && action.equals(SearchableActivity.PLAY_ACTION)) {
            Bundle args = intent.getExtras();

            ArrayList<MediaMetadataCompat> trackQueue = (ArrayList<MediaMetadataCompat>)
                    args.getSerializable(SearchableActivity.QUEUE_KEY);
            int position = args.getInt(SearchableActivity.POSITION_KEY);

            FragmentManager manager = getSupportFragmentManager();
            PlayerControlsFragment fragment = (PlayerControlsFragment)
                    manager.findFragmentByTag(PlayerControlsFragment.TAG);

            if (fragment == null) {
                FragmentTransaction transaction = manager.beginTransaction();
                fragment = new PlayerControlsFragment(trackQueue, position, null);
                transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.slide_to_bottom);
                transaction.replace(R.id.container, fragment, PlayerControlsFragment.TAG);
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
        if (getSupportFragmentManager().findFragmentByTag(AlbumTrackListFragment.TAG) == null &&
                getSupportFragmentManager().findFragmentByTag(PlaylistTrackListFragment.TAG) == null) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode < 0 && requestCode == PlaylistsBottomSheetFragment.REQUEST_CODE) {

            AppContainer container = ((MusicPlayerApp) getApplication()).appContainer;

            if (container.savedValues != null) {
                PlaylistDataProvider dataProvider = container.playlistDataProvider;

                if (container.savedValues.size() == container.valuesToInsert) {

                    ArrayList<ContentValues> valuesList = new ArrayList<>(container.savedValues);

                    for (ContentValues values : valuesList) {
                        dataProvider.addTrackToPlaylist(container.playListId, values,
                                PlaylistsBottomSheetFragment.REQUEST_CODE);
                    }

                    synchronized (AppContainer.class) {
                        container.savedValues.clear();
                        container.valuesToInsert = 0;
                    }
                }
            }
        }
    }

    private void updateAppContainer() {
        AppContainer container = ((MusicPlayerApp) getApplicationContext()).appContainer;
        container.dataProvider = new DataProvider(getApplicationContext(),
                container.executorService, container.mainThreadHandler);
        container.playlistDataProvider = new PlaylistDataProvider(getApplicationContext(),
                container.executorService, container.mainThreadHandler);
    }

    private void addInitialFragment() {
        TabViewFragment fragment = (TabViewFragment) getSupportFragmentManager()
                .findFragmentByTag(TabViewFragment.TAG);

        if (fragment == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            fragment = new TabViewFragment();
            transaction.add(R.id.fragment_container, fragment, TabViewFragment.TAG);
            transaction.commit();
        }
    }
}