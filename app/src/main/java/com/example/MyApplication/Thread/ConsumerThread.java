package com.example.MyApplication.Thread;

import android.util.Log;

import java.util.Queue;
import java.util.Vector;

public class ConsumerThread extends Thread {
    private static final String TAG = "ConsumerThread";
    private static final int INPUT_FRAME_UNIT = 30;

    private AnalysisThread analysisThread;
    private Queue<byte[]> queue;
    private Vector<byte[]> tempBuffer = new Vector<>();
    private Vector<byte[]> frames = new Vector<>();

    private int count = 0;
    private int analysisCount = 0;

    public ConsumerThread(Queue<byte[]> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Log.e(TAG, "Consumer Thread Start...");
        while (!Thread.currentThread().isInterrupted()) {
            if (queue.isEmpty()) {
                continue;
            }
            Log.e(TAG, "remove queue image! " + count + ", Queue Size: " + queue.size());
            count++;
            tempBuffer.add(queue.poll());
            if (tempBuffer.size() == INPUT_FRAME_UNIT) {
                analysisCount++;
                frames = tempBuffer;
                analysisThread = new AnalysisThread(analysisCount, frames);
                analysisThread.start();
                tempBuffer = new Vector<>();
            }
        }
    }
}
