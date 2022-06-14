package com.example.SDA.Thread;

import android.util.Log;

import com.example.SDA.Activity.MainActivity;
import com.example.SDA.Model.My3DPoseModel;
import com.example.SDA.Model.STGCNModel;
import com.google.mlkit.vision.pose.Pose;

import java.io.IOException;
import java.util.Queue;

public class AnalysisThread extends Thread {
    private static final String TAG = "AnalysisThread";
    private Queue<Pose> queue;
    private Pose pose;
    private My3DPoseModel my3DPoseModel;
    private STGCNModel mySTGCNModel;
    private float[] my3DPoseOutput = new float[51];
    public static int result = 0;

    public AnalysisThread(Queue<Pose> queue) {
        this.queue = queue;
        init();
    }

    private void init() {
        my3DPoseModel = new My3DPoseModel(MainActivity.context);
        mySTGCNModel = new STGCNModel(MainActivity.context);
        try {
            my3DPoseModel.init();
            mySTGCNModel.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Log.e(TAG, "Analysis Thread Start...");
        while (!Thread.currentThread().isInterrupted()) {
            if (queue.isEmpty()) {
                continue;
            }

            pose = queue.poll();
            if (pose == null)
                continue;

            my3DPoseOutput = my3DPoseModel.run(pose);
            mySTGCNModel.push(my3DPoseOutput);

            if (mySTGCNModel.isRunnable()) {
                result = mySTGCNModel.run();
            }
        }
        Log.e(TAG, "Analysis Thread Finish...");
    }
}
