package com.example.musicplayer.controlspanel;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.example.musicplayer.R;


public class PlayerScreenMotionLayout extends MotionLayout {

    private final GestureDetector gestureDetector;
    private boolean touchHasStarted = false;
    private final Rect viewRect = new Rect();

    public PlayerScreenMotionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                transitionToEnd();
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        View viewToDetectTouch = this.findViewById(R.id.player_background_view);
        gestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            touchHasStarted = false;
            return super.onTouchEvent(event);
        }

        if (!touchHasStarted) {
            viewToDetectTouch.getHitRect(viewRect);
            touchHasStarted = viewRect.contains((int)event.getX(), (int)event.getY());
        }
        return touchHasStarted && super.onTouchEvent(event);
    }
}
