package com.example.musicplayer.controlspanel.slidingimage;

public enum Direction {
    Up,
    Down,
    Right,
    Left;

    public static Direction fromAngle(double angle) {
        if (inRange(angle, 45, 135)) {
            return Direction.Up;
        }
        if (inRange(angle, 225, 315)) {
            return Direction.Down;
        }
        if (inRange(angle, 315, 45)) {
            return Direction.Right;
        }
        if (inRange(angle, 135, 225)) {
            return Direction.Left;
        }
        return null;
    }

    private static boolean inRange(double angle, float start, float end) {
        return (angle >= start) && (angle <= end);
    }
}
