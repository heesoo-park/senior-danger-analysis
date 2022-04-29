package com.example.MyApplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.MyApplication.Activity.PreviewActivity;
import com.example.MyApplication.Activity.SplashActivity;
import com.example.MyApplication.Activity.TempActivity;
import com.example.MyApplication.Service.ForegroundService;
import com.example.MyApplication.View.CameraSurfaceView;


public class MainActivity extends AppCompatActivity {
    public static Context context;

    private static final String TAG = "MainActivity";

    private Button editInfoButton;
    private Button openPreviewButton;

    private ImageView mainImageView;

    private RelativeLayout relativeLayout;
    private AnimationDrawable animationDrawable;

    private Switch serviceToggleSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
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
    protected void onResume() {
        super.onResume();
        if (!ForegroundService.isServiceRunning(getApplication())) {
            startService();
        } else {
            startAnimation();
            serviceToggleSwitch.setChecked(true);
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
        openPreviewButton = (Button) findViewById(R.id.openPreviewButton);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);

        relativeLayout = (RelativeLayout) findViewById(R.id.background);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);

        serviceToggleSwitch = (Switch) findViewById(R.id.serviceToggleSwitch);
        serviceToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService();
                } else {
                    stopService();
                }
            }
        });
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TempActivity.class);
                startActivity(intent);
            }
        });
        openPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ForegroundService.isServiceRunning(getApplication())) {
                    stopService();
                }
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                startActivity(intent);
            }
        });
    }

    public void startService() {
        Toast.makeText(getApplicationContext(), getString(R.string.START_SERVICE), Toast.LENGTH_SHORT).show();
        startService(new Intent(getApplication(), ForegroundService.class));
        serviceToggleSwitch.setChecked(true);
        startAnimation();
    }

    private void stopService() {
        Toast.makeText(getApplicationContext(), getString(R.string.STOP_SERVICE), Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplication(), ForegroundService.class));
        serviceToggleSwitch.setChecked(false);
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