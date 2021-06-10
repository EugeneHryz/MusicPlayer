package com.example.musicplayer.controlspanel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

public class DragDownButton extends AppCompatImageButton {

    public static final String TAG = "DragDownButton";

    private PlayerScreenMotionLayout motionLayout;

    private final GestureDetector gestureDetector;
    private boolean singleTap = false;

    public DragDownButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                singleTap = true;
                if (motionLayout != null) {
                    motionLayout.transitionToStart();
                    motionLayout.setTouchHasStarted(false);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (motionLayout != null && !singleTap) {
            motionLayout.onTouchEvent(event);
        }

        singleTap = false;
        return super.onTouchEvent(event);
    }

    public void setMotionLayout(PlayerScreenMotionLayout motionLayout) {
        this.motionLayout = motionLayout;
    }
}
