package com.owiseman.document.model;

public record Rect(float x, float y, float width, float height) {

    public static Rect of(float x, float y, float width, float height) {
        return new Rect(x, y, width, height);
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= right() && py >= y && py <= bottom();
    }

    public boolean intersects(Rect other) {
        return x < other.right() && right() > other.x
                && y < other.bottom() && bottom() > other.y;
    }
}
