package com.example.SDA.Class;

import android.graphics.PointF;
import android.graphics.RectF;

import com.google.mlkit.vision.pose.Pose;

public class PoseLandmarkInfo {
    private RectF rect;
    private float[][] _3dPoseInput;
    private PointF[] allPoseLandmark;
    private Pose pose;

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

}
