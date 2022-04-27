package com.example.MyApplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.MyApplication.Service.ForegroundService;
import com.example.MyApplication.Thread.CaptureThread;
import com.example.MyApplication.Thread.ConsumerThread;

import java.util.LinkedList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (!CaptureService.isServiceRunning(this)) {
//            consumerThread = new ConsumerThread(queue);
//            consumerThread.start();
        //}
        startService(new Intent(getApplication(), ForegroundService.class));
        //captureThread = new CaptureThread(queue);
        //captureThread.start();
    }
}