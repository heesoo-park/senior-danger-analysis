package com.example.SDA.Model;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.example.SDA.Class.PoseLandmarkInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

public class PoseDetectionModel {
    int rotationDegree = 0;
    private PoseDetectorOptions options;
    private PoseDetector poseDetector;
    private InputImage image;
    private PointF[] allPoseLandmark;
    private RectF rect;
    private PoseLandmarkInfo poseLandmarkInfo;
    private static final int SKELETON_BODY_POINT = 33;
    private float xMax, xMin, yMax, yMin;
    private static final float POS_INF = Float.POSITIVE_INFINITY;
    private static final float NEG_INF = Float.NEGATIVE_INFINITY;
    private int width, height;

    public PoseDetectionModel(int width, int height) {
        this.width = width;
        this.height = height;
        options = new PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);
    }

    public PoseLandmarkInfo detect(byte[] data) {
        image = InputImage.fromByteArray(data, width, height, rotationDegree, InputImage.IMAGE_FORMAT_NV21);
        poseDetector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Pose>() {
                            @Override
                            public void onSuccess(Pose pose) {
                                poseLandmarkInfo = new PoseLandmarkInfo();
                                allPoseLandmark = new PointF[SKELETON_BODY_POINT];
                                xMax = NEG_INF;
                                xMin = POS_INF;
                                yMax = NEG_INF;
                                yMin = POS_INF;
                                for (int i = 0; i < SKELETON_BODY_POINT; i++) {
                                    PoseLandmark poseLandmark = pose.getPoseLandmark(i);
                                    if (poseLandmark == null) {
                                        poseLandmarkInfo = null;
                                        return;
                                    } else {
                                        allPoseLandmark[i] = poseLandmark.getPosition();
                                        // rect를 생성할 때 팔 부분(elbow, wrist, pinky, index, thumb)는 사용 안함
                                        if (i >= 13 && i <= 22)
                                            continue;
                                        xMax = Math.max(xMax, allPoseLandmark[i].x);
                                        xMin = Math.min(xMin, allPoseLandmark[i].x);
                                        yMax = Math.max(yMax, height - allPoseLandmark[i].y);
                                        yMin = Math.min(yMin, height - allPoseLandmark[i].y);
                                    }
                                }
                                rect = new RectF(xMin, yMin, xMax, yMax);
                                poseLandmarkInfo.setAllPoseLandmark(allPoseLandmark);
                                poseLandmarkInfo.setRect(rect);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
        return poseLandmarkInfo;
    }
}
