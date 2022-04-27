package com.example.MyApplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.MyApplication.Thread.CaptureThread;
import com.example.MyApplication.Thread.ConsumerThread;

import java.util.LinkedList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {
    private MyThread thread;
    private CaptureThread captureThread;
    private ConsumerThread consumerThread;
    private Queue<byte[]> queue = new LinkedList<>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureThread = new CaptureThread(queue);
        captureThread.start();
        consumerThread = new ConsumerThread(queue);
        consumerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "" + queue.size());
    }
}