package com.example.SDA.Class;

import android.graphics.RectF;

import com.google.mlkit.vision.pose.Pose;

public class PoseLandmarkInfo {
    private RectF rect;
    private Pose pose;
    private float hipHeight;
    private float range;

    public PoseLandmarkInfo() {}

    public RectF getRect() {
        return this.rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public Pose getPose() {
        return this.pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public float getHipHeight() {
        return this.hipHeight;
    }

    public void setHipHeight(float hipHeight) {
        this.hipHeight = hipHeight;
    }

    public float getRange() {
        return this.range;
    }

    public void setRange(float range) {
        this.range = range;
    }
}
