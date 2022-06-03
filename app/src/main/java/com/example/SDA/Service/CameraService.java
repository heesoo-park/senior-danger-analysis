package com.example.SDA.Service;

import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.example.SDA.Class.AnalysisResult;
import com.example.SDA.Class.PoseLandmarkInfo;
import com.example.SDA.Model.PoseDetectionModel;
import com.example.SDA.Model.STGCNModel;
import com.example.SDA.Thread.AnalysisThread;
import com.example.SDA.Thread.StorageThread;
import com.google.mlkit.vision.pose.Pose;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CameraService {
    private static final String TAG = "CameraService";

    public static Camera camera;
    public static SurfaceTexture surfaceTexture;
    public static Queue<Pose> drawQueue;
    public static AnalysisResult analysisResult;

    private static final int PICTURE_WIDTH = 480;
    private static final int PICTURE_HEIGHT = 320;
    private static final int FRAME_RATE = 10;
    private static final int CAMERA_INDEX = 0;
    private static final int CAPTURE_INTERVAL_NORMAL = 500;
    private static final int CAPTURE_INTERVAL_BURST = 100;
    
    private long beforeTime, afterTime, diffTime;
    private Queue<Pose> queue;
    private PoseDetectionModel poseDetectionModel;
    private PoseLandmarkInfo poseLandmarkInfo;
    private float lastLastRatio, lastRatio, ratio;
    private int captureInterval;
    private int analysisPushCount;
    private int nothingCount;
    private StorageThread storageThread;

    public CameraService(Queue<Pose> queue) {
        this.queue = queue;
        this.drawQueue = new LinkedList<>();
        this.analysisResult = new AnalysisResult();
        init();
    }

    public void init() {
        poseDetectionModel = new PoseDetectionModel(PICTURE_WIDTH, PICTURE_HEIGHT);
        camera = Camera.open(CAMERA_INDEX);
        surfaceTexture = new SurfaceTexture(10);
        surfaceTexture.setDefaultBufferSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        Log.e(TAG, "Create Camera");
        try {
            this.camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Parameters params = camera.getParameters();
        params.setPreviewSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        params.setPictureSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setPreviewFrameRate(FRAME_RATE);
        camera.setParameters(params);
        camera.startPreview();
        captureInterval = CAPTURE_INTERVAL_NORMAL;
        nothingCount = 0;
        lastLastRatio = 100; lastRatio = 100; ratio = 100;
    }

    private void analysis(byte[] data) {
        poseLandmarkInfo = poseDetectionModel.detect(data);
        if (poseLandmarkInfo == null) {
            Log.e(TAG, "CAPTURE! NOT OBJECT.");
            drawQueue.add(null);
            //lastLastRatio = 100; lastRatio = 100; ratio = 100;      // 객체가 탐지되지 않았으면 초기화.
            analysisResult.setResult(AnalysisResult.RESULT_NOTHING);
            nothingCount++;

            // 일정 횟수 이상 객체가 탐지되지 않으면 BURST MODE 종료.
            if (captureInterval == CAPTURE_INTERVAL_BURST && nothingCount > 20) {
                lastLastRatio = 100; lastRatio = 100; ratio = 100;
                queue.clear();
                captureInterval = CAPTURE_INTERVAL_NORMAL;
                nothingCount = 0;
                analysisPushCount = 0;
            }
            return;
        }

        // first task...
        nothingCount = 0;
        Pose pose = poseLandmarkInfo.getPose();
        RectF rect = poseLandmarkInfo.getRect();

        lastLastRatio = lastRatio;
        lastRatio = ratio;
        ratio = rect.width() / rect.height();
        Log.e(TAG, "CAPTURE! " + lastLastRatio + ", " + ratio + ", width : " + rect.width() + ", height: " + rect.height());

        drawQueue.add(pose);
        analysisResult.setLastRatio(lastLastRatio);
        analysisResult.setRatio(ratio);

        if (captureInterval == CAPTURE_INTERVAL_NORMAL) {
            analysisResult.setResult(AnalysisResult.RESULT_NOT_HAPPENED);
        }

        if (captureInterval == CAPTURE_INTERVAL_NORMAL && lastLastRatio < 0.45 && ratio > 1.2 && rect.width() * 1.2 > rect.height()) {
            Log.e(TAG, "FALL DOWN DOUBT! BURST MODE START.");
            analysisResult.setResult(AnalysisResult.RESULT_FALL_DOUBT);
            clearBuffers();
            captureInterval = CAPTURE_INTERVAL_BURST;
        }

        if (captureInterval == CAPTURE_INTERVAL_BURST) {
            queue.add(poseLandmarkInfo.getPose());
            analysisResult.setResult(AnalysisResult.RESULT_WAITING);
            analysisPushCount++;
        }

        // ST-GCN 분석 결과가 1이라면...
        if (captureInterval == CAPTURE_INTERVAL_BURST && AnalysisThread.result == 1) {
            Log.e(TAG, "FALL DOWN! BURST MODE TERMINATE, CREATE STORAGE THREAD.");
            analysisResult.setResult(AnalysisResult.RESULT_FALL_RECOGNIZE);

            // 전송 스레드 실행
            storageThread = new StorageThread(data, PICTURE_WIDTH, PICTURE_HEIGHT);
            storageThread.run();

            clearBuffers();
            captureInterval = CAPTURE_INTERVAL_NORMAL;
        }

        // Analysis Thread 에 120번 push(최대 4번 분석)했다면 BURST MODE 종료.
        if (analysisPushCount > 120) {
            clearBuffers();
            captureInterval = CAPTURE_INTERVAL_NORMAL;
        }
    }

    private void clearBuffers() {
        queue.clear();
        analysisPushCount = 0;
        STGCNModel.clearBuffer();
        AnalysisThread.result = 0;
    }

    public void onCaptureRepeat() {
        beforeTime = System.currentTimeMillis();
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    afterTime = System.currentTimeMillis();
                    diffTime = afterTime - beforeTime;
                    if (data == null)
                        return;
                    if (diffTime > captureInterval) {
                        analysis(data);
                        beforeTime = afterTime;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void finish() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
}
