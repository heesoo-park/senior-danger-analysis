package com.example.SDA.Thread;

import android.graphics.RectF;
import android.util.Log;

import com.example.SDA.Class.PoseLandmarkInfo;
import com.example.SDA.Model.PoseDetectionModel;

import java.util.Queue;

public class PoseDetectionThread extends Thread {
    private static final String TAG = "ConsumerThread";
    private static final int PICTURE_WIDTH = 480;
    private static final int PICTURE_HEIGHT = 320;
    private Queue<byte[]> queue;
    private PoseDetectionModel poseDetectionModel;
    private float lastRatio, ratio, vr;
    private int count = 0;
    private byte[] data;
    private PoseLandmarkInfo poseLandmarkInfo;
    private RectF rect;

    public PoseDetectionThread(Queue<byte[]> queue) {
        this.queue = queue;
        this.poseDetectionModel = new PoseDetectionModel(PICTURE_WIDTH, PICTURE_HEIGHT);
    }


    @Override
    public void run() {
        Log.e(TAG, "Consumer Thread Start...");
        while (!Thread.currentThread().isInterrupted()) {
            if (queue.isEmpty()) {
                continue;
            }
            data = queue.poll();
            long beforeTime = System.currentTimeMillis();
            poseLandmarkInfo = poseDetectionModel.detect(data);
            if (poseLandmarkInfo == null) {
                Log.e(TAG, "검출된 사항 없음");
                continue;
            }
            long afterTime = System.currentTimeMillis();
            long diffTime = afterTime - beforeTime;
            Log.e(TAG, "분석시간 : " + diffTime);
            rect = poseLandmarkInfo.getRect();

            lastRatio = ratio;
            ratio = rect.height() / rect.width();
            vr = (ratio - lastRatio);

            Log.e(TAG, "" + rect.width() + ", " + rect.height() + ", " + ratio + ", " + lastRatio + ", " + vr);
            if (vr < -2) {
                Log.e(TAG, "낙상 의심");
            }
        }
    }
}
