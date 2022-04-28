package com.example.MyApplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.MyApplication.Activity.TempActivity;
import com.example.MyApplication.Service.ForegroundService;
import com.example.MyApplication.View.CameraSurfaceView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CONTENT_LOGO = 0;
    private static final int CONTENT_PREVIEW = 1;
    private int content = CONTENT_LOGO;

    private Button editInfoButton;
    private Button serviceToggleButton;
    private Button previewToggleButton;

    private ImageView mainImageView;
    private FrameLayout frameLayout;
    private CameraSurfaceView cameraSurfaceView;
    private TextView stateText;

    private RelativeLayout relativeLayout;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            initLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==200 && grantResults.length>0){
            boolean permissionGranted=true;
            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    // 사용자가 권한을 거절했다.
                    permissionGranted=false;
                    break;
                }
            }

            if(permissionGranted){
                // 권한 요청을 수락한 경우에 layout을 전개한다.
                initLayout();
            }else{
                Toast.makeText(this,
                        "권한을 허용해야 SDA 앱을 이용하실 수 있습니다.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initLayout() {
        setContentView(R.layout.activity_main);

        editInfoButton = (Button) findViewById(R.id.editInfoButton);
        serviceToggleButton = (Button) findViewById(R.id.serviceToggleButton);
        previewToggleButton = (Button) findViewById(R.id.previewToggleButton);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout.setVisibility(View.GONE);
        stateText = (TextView) findViewById(R.id.stateText);

        relativeLayout = (RelativeLayout) findViewById(R.id.background);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
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
        previewToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (content == CONTENT_PREVIEW) {
                    startService();
                    closePreview();
                } else {
                    stopService();
                    openPreview();
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
        while (!ForegroundService.isServiceRunning(getApplication()));
    }

    private void stopService() {
        Toast.makeText(getApplicationContext(), getString(R.string.STOP_SERVICE), Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplication(), ForegroundService.class));
        stateText.setText(getString(R.string.STATE_STOP));
        serviceToggleButton.setText(getString(R.string.START_SERVICE));
        stopAnimation();
        while (ForegroundService.isServiceRunning(getApplication()));
    }

    private void openPreview() {
        hideAllButtons();
        content = CONTENT_PREVIEW;
        mainImageView.setVisibility(View.GONE);
        cameraSurfaceView = new CameraSurfaceView(MainActivity.this);
        frameLayout.addView(cameraSurfaceView);
        frameLayout.setVisibility(View.VISIBLE);
        previewToggleButton.setVisibility(View.VISIBLE);
        previewToggleButton.setText(getString(R.string.CLOSE_PREVIEW));
        stateText.setVisibility(View.VISIBLE);
    }

    private void closePreview() {
        showAllButtons();
        content = CONTENT_LOGO;
        frameLayout.setVisibility(View.GONE);
        frameLayout.removeView(cameraSurfaceView);
        mainImageView.setVisibility(View.VISIBLE);
        previewToggleButton.setText(getString(R.string.OPEN_PREVIEW));
    }

    private void startAnimation() {
        Glide.with(this).load(R.raw.running).circleCrop().into(mainImageView);
        animationDrawable.start();
    }

    private void stopAnimation() {
        Glide.with(this).load(R.raw.stop).circleCrop().into(mainImageView);
        animationDrawable.stop();
    }

    private void showAllButtons() {
        editInfoButton.setVisibility(View.VISIBLE);
        serviceToggleButton.setVisibility(View.VISIBLE);
        previewToggleButton.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
    }

    private void hideAllButtons() {
        editInfoButton.setVisibility(View.GONE);
        serviceToggleButton.setVisibility(View.GONE);
        previewToggleButton.setVisibility(View.GONE);
        stateText.setVisibility(View.INVISIBLE);
    }
}