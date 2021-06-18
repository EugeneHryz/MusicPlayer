package com.example.musicplayer.controlspanel.slidingimage;

import android.net.Uri;

import androidx.viewpager2.widget.ViewPager2;

import com.example.musicplayer.controlspanel.PlayerScreenMotionLayout;

public class SlidingImageFragmentStateSaver {

    private final ViewPager2 viewPager;

    private final PlayerScreenMotionLayout motionLayout;

    private final Uri albumCover;

    public SlidingImageFragmentStateSaver(ViewPager2 viewPager, PlayerScreenMotionLayout motionLayout,
                         Uri albumCover) {

        this.viewPager = viewPager;
        this.motionLayout = motionLayout;
        this.albumCover = albumCover;
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public PlayerScreenMotionLayout getMotionLayout() {
        return motionLayout;
    }

    public Uri getAlbumCover() {
        return albumCover;
    }
}
