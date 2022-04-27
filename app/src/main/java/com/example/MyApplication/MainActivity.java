package com.example.MyApplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.MyApplication.Activity.TempActivity;
import com.example.MyApplication.Service.ForegroundService;
import com.example.MyApplication.Thread.CaptureThread;
import com.example.MyApplication.Thread.ConsumerThread;

import java.util.LinkedList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Button editInfoButton;
    private Button serviceToggleButton;
    private Button previewToggleButton;

    private ImageView mainImageView;
    private FrameLayout frameLayout;
    //private CameraSurfaceView cameraSurfaceView;
    private TextView stateText;

    LinearLayout linearLayout;
    AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editInfoButton = (Button) findViewById(R.id.editInfoButton);
        serviceToggleButton = (Button) findViewById(R.id.serviceToggleButton);
        previewToggleButton = (Button) findViewById(R.id.previewToggleButton);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout.setVisibility(View.GONE);
        stateText = (TextView) findViewById(R.id.stateText);

        linearLayout = (LinearLayout) findViewById(R.id.background);
        animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);

        if (!ForegroundService.isServiceRunning(this)) {
            startService();
        }
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TempActivity.class);
                startActivity(intent);
            }
        });
        serviceToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ForegroundService.isServiceRunning(getApplication())) {
                    startService();
                } else {
                    stopService();
                }
            }
        });
    }

    private void startService() {
        Toast.makeText(getApplicationContext(), getString(R.string.START_SERVICE), Toast.LENGTH_SHORT).show();
        startService(new Intent(getApplication(), ForegroundService.class));
        stateText.setText(getString(R.string.STATE_RUNNING));
        serviceToggleButton.setText(getString(R.string.STOP_SERVICE));
        startAnimation();
    }

    private void stopService() {
        Toast.makeText(getApplicationContext(), getString(R.string.STOP_SERVICE), Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplication(), ForegroundService.class));
        stateText.setText(getString(R.string.STATE_STOP));
        serviceToggleButton.setText(getString(R.string.START_SERVICE));
        stopAnimation();
    }

    private void startAnimation() {
        Glide.with(this).load(R.raw.running).circleCrop().into(mainImageView);
        animationDrawable.start();
    }

    private void stopAnimation() {
        Glide.with(this).load(R.raw.stop).circleCrop().into(mainImageView);
        animationDrawable.stop();
    }
}