package com.example.musicplayer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

public class SlidingImageFragment extends Fragment {
    public static final String TAG = "SlidingImageFragment";

    private SlidingImageView albumCover;
    private ViewPager2 viewPager;
    private PlayerScreenMotionLayout motionLayout;

    private Uri albumCoverUri;

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
                .placeholder(R.drawable.music_note_icon)
                .apply(new RequestOptions().centerCrop().override(936, 936))
                .into(albumCover);

        albumCover.setViewPager(viewPager);
        albumCover.setMotionLayout(motionLayout);
    }
}
