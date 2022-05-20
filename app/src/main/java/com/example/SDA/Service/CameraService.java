package com.example.SDA.Service;

import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.example.SDA.Class.PoseLandmarkInfo;
import com.example.SDA.Model.PoseDetectionModel;
import com.example.SDA.Thread.AnalysisThread;
import com.example.SDA.Thread.StorageThread;
import com.google.mlkit.vision.pose.Pose;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class CameraService {
    private static final String TAG = "CameraService";
    private Camera camera;
    private SurfaceTexture surfaceTexture;
    private static final int PICTURE_WIDTH = 480;
    private static final int PICTURE_HEIGHT = 320;
    private static final int FRAME_RATE = 10;
    private static final int CAMERA_INDEX = 0;
    private static final int CAPTURE_MODE_BURST = 2;
    private static final int CAPTURE_MODE_NORMAL = 12;
    private int captureMode;
    private int captureCount;
    private Queue<Pose> queue;
    private PoseDetectionModel poseDetectionModel;
    private PoseLandmarkInfo poseLandmarkInfo;
    private RectF rect;
    private float lastRatio, ratio, vr;
    private int analysisPushCount;
    private StorageThread storageThread;

    public CameraService(Queue<Pose> queue) {
        this.queue = queue;
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
        android.hardware.Camera.Parameters params = camera.getParameters();
        params.setPreviewSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        params.setPictureSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setPreviewFrameRate(FRAME_RATE);
        camera.setParameters(params);
        camera.startPreview();
        captureMode = CAPTURE_MODE_NORMAL;
    }

    public Camera getCamera() {
        return camera;
    }

    private void pushPoseDetector(byte[] data) {
        poseLandmarkInfo = poseDetectionModel.detect(data);
        if (poseLandmarkInfo == null) {
            return;
        }

        rect = poseLandmarkInfo.getRect();

        lastRatio = ratio;
        ratio = rect.height() / rect.width();
        vr = ratio - lastRatio;

        Log.e(TAG, "Image Capture! Size: (" + rect.width() + ", " + rect.height() + "), " + ratio + ", " + lastRatio + ", 변화량: " + vr + ", count: " + analysisPushCount);
        if (vr < -1.5 && captureMode == CAPTURE_MODE_NORMAL) {
            Log.e(TAG, "ratio 임계값 넘음 " + vr + ", BURST MODE START");
            queue.clear();
            captureMode = CAPTURE_MODE_BURST;
            analysisPushCount = 0;
            return;
        }

        if (captureMode == CAPTURE_MODE_BURST) {
            queue.add(poseLandmarkInfo.getPose());
            analysisPushCount++;
        }

        if (AnalysisThread.result == 0) {
            // 낙상 발생
            queue.clear();
            captureMode = CAPTURE_MODE_NORMAL;
            analysisPushCount = 0;
            AnalysisThread.result = 1;
            storageThread = new StorageThread(data, PICTURE_WIDTH, PICTURE_HEIGHT);
            storageThread.run();
        }

        if (analysisPushCount > 120) {
            captureMode = CAPTURE_MODE_NORMAL;
            analysisPushCount = 0;
        }
    }

    public void onCaptureRepeat() {
        captureCount = 0;
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    captureCount++;
                    // capture mode에 따라 사진을 찍는 타이밍이 다름.
                    if (captureCount != captureMode) {
                        return;
                    }
                    if (data == null)
                        return;
                    pushPoseDetector(data);
                    captureCount = 0;
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
