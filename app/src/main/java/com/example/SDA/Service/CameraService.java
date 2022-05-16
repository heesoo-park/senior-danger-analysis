package com.example.SDA.Service;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.Queue;

public class CameraService {
    private static final String TAG = "CameraService";
    private Camera camera;
    private SurfaceTexture surfaceTexture;
    private static final int PICTURE_WIDTH = 480;
    private static final int PICTURE_HEIGHT = 320;
    private static final int FRAME_RATE = 10;
    private static final int CAMERA_INDEX = 0;
    private int captureCount;
    private Queue<byte[]> queue;

    public CameraService(Queue<byte[]> queue) {
        this.queue = queue;
        init();
    }

    public void init() {
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
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setPreviewFrameRate(FRAME_RATE);
        camera.setParameters(params);
        camera.startPreview();
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public Camera getCamera() {
        return camera;
    }

    public void onCaptureRepeat() {
        captureCount = 0;
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    captureCount++;
                    if (captureCount % 7 != 0) {
                        return;
                    }
                    //Log.e(TAG, "image capture! " + captureCount / 10 + ", Bytes: " + data);
                    queue.add(data);
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
