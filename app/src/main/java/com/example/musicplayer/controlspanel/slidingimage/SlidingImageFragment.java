package com.example.musicplayer.controlspanel.slidingimage;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer.AppContainer;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.R;
import com.example.musicplayer.controlspanel.PlayerControlsFragment;
import com.example.musicplayer.controlspanel.PlayerScreenMotionLayout;

import java.util.Objects;

public class SlidingImageFragment extends Fragment {

    public static final String TAG = "SlidingImageFragment";

    public static final String ALBUM_COVER_KEY = "album_cover_key";

    private static final String STATE_SAVED_KEY = "state_saved_key";

    private SlidingImageView albumCover;
    private ViewPager2 viewPager;
    private PlayerScreenMotionLayout motionLayout;

    private String albumCoverUri;

    public SlidingImageFragment() {
    }

    public SlidingImageFragment(ViewPager2 viewPager, PlayerScreenMotionLayout motionLayout) {
        super();

        this.viewPager = viewPager;
        this.motionLayout = motionLayout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            albumCoverUri = args.getString(ALBUM_COVER_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        albumCover = (SlidingImageView) LayoutInflater.from(getContext()).inflate(R.layout.sliding_image_view, container);
        return albumCover;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Glide.with(Objects.requireNonNull(getContext())).load(albumCoverUri)
                .placeholder(R.drawable.music_note_icon_light)
                .apply(new RequestOptions().centerCrop().override(936, 936))
                .into(albumCover);

        albumCover.setViewPager(viewPager);
        albumCover.setMotionLayout(motionLayout);
    }

    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    public void setMotionLayout(PlayerScreenMotionLayout motionLayout) {
        this.motionLayout = motionLayout;
    }
}
