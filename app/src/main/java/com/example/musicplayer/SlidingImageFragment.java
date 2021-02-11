package com.example.musicplayer;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class SlidingImageFragment extends Fragment {
    public static final String TAG = "SlidingImageFragment";

    private SlidingImageView albumCover;
    private final ViewPager2 viewPager;
    private final PlayerScreenMotionLayout motionLayout;

    private final Uri albumCoverUri;

    public SlidingImageFragment(ViewPager2 viewPager, PlayerScreenMotionLayout motionLayout, Uri albumCoverUri) {
        super();

        this.viewPager = viewPager;
        this.motionLayout = motionLayout;
        this.albumCoverUri = albumCoverUri;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        albumCover = (SlidingImageView) LayoutInflater.from(getContext()).inflate(R.layout.sliding_image_view, container);
        return albumCover;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Glide.with(getContext()).load(albumCoverUri)
                .placeholder(R.drawable.music_note_icon_light)
                .apply(new RequestOptions().centerCrop().override(936, 936))
                .into(albumCover);

        albumCover.setViewPager(viewPager);
        albumCover.setMotionLayout(motionLayout);
    }
}
