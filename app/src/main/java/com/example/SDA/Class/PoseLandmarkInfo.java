package com.example.SDA.Class;

import android.graphics.PointF;
import android.graphics.RectF;

public class PoseLandmarkInfo {
    private RectF rect;
    private PointF[] allPoseLandmark;

    public PoseLandmarkInfo() {}

    public RectF getRect() {
        return this.rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public PointF[] getAllPoseLandmark() {
        return this.allPoseLandmark;
    }

    public void setAllPoseLandmark(PointF[] allPoseLandmark) {
        this.allPoseLandmark = allPoseLandmark;
    }
}
