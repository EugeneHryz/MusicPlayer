package com.example.musicplayer.controlspanel.slidingimage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.musicplayer.controlspanel.PlayerScreenMotionLayout;

public class SlidingImageView extends androidx.appcompat.widget.AppCompatImageView {

    public static final String TAG = "SlidingImageView";

    private final GestureDetector gestureDetector;

    private ViewPager2 viewPager;
    private PlayerScreenMotionLayout motionLayout;

    public SlidingImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        gestureDetector = new GestureDetector(context, new DragGestureListener());
    }

    public class DragGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Direction dir = getDirection(e1.getX(), e1.getY(), e2.getX(), e2.getY());
            if (dir == Direction.Down) {
                if (viewPager != null) {
                    viewPager.setUserInputEnabled(false);
                }
            }
            return true;
        }

        private Direction getDirection(float x1, float y1, float x2, float y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
            angle *= 180 / Math.PI;
            angle += 360;
            angle %= 360;
            return Direction.fromAngle(angle);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP && viewPager != null) {
            viewPager.setUserInputEnabled(true);
        }
        if (motionLayout != null) {
            motionLayout.onTouchEvent(event);
        }

        return true;
    }

    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    public void setMotionLayout(PlayerScreenMotionLayout motionLayout) {
        this.motionLayout = motionLayout;
    }
}