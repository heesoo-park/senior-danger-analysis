package com.example.MyApplication.Thread;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.widget.FrameLayout;

import com.example.MyApplication.MainActivity;
import com.example.MyApplication.Service.ForegroundService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;

public class CaptureThread extends Thread {
    private static final String TAG = "CaptureThread";
    private Camera camera;
    private int count = 0;
    private SurfaceTexture surfaceTexture;
    private static final int PICTURE_WIDTH = 480;
    private static final int PICTURE_HEIGHT = 320;
    private static final int MODEL_INPUT_PICTURE_WIDTH = 480;
    private static final int MODEL_INPUT_PICTURE_HEIGHT = 320;
    private static final int FRAME_RATE = 10;
    private Queue<byte[]> queue;

    private long beforeTime;
    private long afterTime;
    private long diffTime;

    public CaptureThread(Queue<byte[]> queue) {
        this.camera = Camera.open(0);
        this.queue = queue;
        surfaceTexture = new SurfaceTexture(10);
        surfaceTexture.setDefaultBufferSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        Log.e(TAG, "캡쳐 스레드 생성자");
        try {
            this.camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = camera.getParameters();
        params.setPreviewSize(PICTURE_WIDTH, PICTURE_HEIGHT);
        params.setPictureSize(MODEL_INPUT_PICTURE_WIDTH, MODEL_INPUT_PICTURE_HEIGHT);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setPreviewFrameRate(FRAME_RATE);
        camera.setParameters(params);
        camera.startPreview();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void run() {
        Log.e(TAG, "Capture Thread Start...");
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    byte[] baos = convertYuvToJpeg(data, camera);
                    if (baos == null) {
                        return;
                    }
                    Log.e(TAG, "image capture! " + count + ", Queue Size: " + queue.size() + ", Bytes: " + baos + ", Data Length: " + data.length);
                    if (count % 30 == 0) {
                        if (count == 0) {
                            beforeTime = System.currentTimeMillis();
                        } else {
                            afterTime = System.currentTimeMillis();
                            diffTime = (afterTime - beforeTime) / 1000;
                            Log.i(TAG, "시간" + diffTime);
                            beforeTime = afterTime;
                        }
                    }
                    count++;
                    queue.add(baos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Log.e(TAG, "Capture Thread Finish...");
    }

    public byte[] convertYuvToJpeg(byte[] data, Camera camera) {
        if (camera == null)
            return null;
        YuvImage image = new YuvImage(data, ImageFormat.NV21,
                camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 20; //set quality
        image.compressToJpeg(new Rect(0, 0, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height), quality, baos);//this line decreases the image quality
        return baos.toByteArray();
    }
}
