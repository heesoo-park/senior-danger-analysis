package com.example.SDA.Class;

import android.graphics.RectF;

public class AnalysisResult {
    public static int RESULT_NOT_HAPPENED = 0;
    public static int RESULT_FALL_DOUBT = 1;
    public static int RESULT_FALL_RECOGNIZE = 2;
    public static int RESULT_NOTHING = 3;
    public static int RESULT_WAITING = 4;

    private float lastRatio, ratio;
    private int result;
    public AnalysisResult() {}

    public float getLastRatio() {
        return this.lastRatio;
    }

    public void setLastRatio(float lastRatio) {
        this.lastRatio = lastRatio;
    }

    public float getRatio() {
        return this.ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
