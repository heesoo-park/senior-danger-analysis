package com.example.SDA;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.SDA.Activity.InformationActivity;
import com.example.SDA.Activity.PreviewActivity;
import com.example.SDA.Activity.TempActivity;
import com.example.SDA.Service.ForegroundService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Button editInfoButton;
    private Button openPreviewButton;
    private Button appInfoButton;

    private ImageView mainImageView;
    private TextView serviceGuideTextView;

    private RelativeLayout relativeLayout;
    private AnimationDrawable animationDrawable;

    private ImageView serviceStateImageView;
    private TextView serviceStateTextView;

    private Button colorChangeYellowButton;
    private Button colorChangeRedButton;
    private Button colorChangeBlackButton;
    private Button colorChangeRainbowButton;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private int backgroundColor = -1;
    private int buttonColor;

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
    protected void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        refreshBackground();
        if (!ForegroundService.isServiceRunning(getApplication())) {
            startService();
        } else {
            startAnimation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==200 && grantResults.length > 0){
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

        editInfoButton =  (Button) findViewById(R.id.editInfoButton);
        openPreviewButton = (Button) findViewById(R.id.openPreviewButton);
        appInfoButton = (Button) findViewById(R.id.appInfoButton);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);
        serviceGuideTextView = (TextView) findViewById(R.id.serviceGuideTextView);

        relativeLayout = (RelativeLayout) findViewById(R.id.background);

        serviceStateImageView = (ImageView) findViewById(R.id.serviceStateImageView);
        serviceStateTextView = (TextView) findViewById(R.id.serviceStateTextView);

        colorChangeYellowButton = (Button) findViewById(R.id.colorChangeYellowButton);
        colorChangeRedButton = (Button) findViewById(R.id.colorChangeRedButton);
        colorChangeBlackButton = (Button) findViewById(R.id.colorChangeBlackButton);
        colorChangeRainbowButton = (Button) findViewById(R.id.colorChangeRainbowButton);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        if ((pref != null) && !pref.contains("theme_color")) {
            editor.putInt("theme_color", R.drawable.background_yellow);
            editor.putInt("button_color", R.drawable.button_yellow);
            editor.commit();
        }

        mainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ForegroundService.isServiceRunning(getApplication())) {
                    startService();
                } else {
                    stopService();
                }
            }
        });
        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ForegroundService.isServiceRunning(getApplication())) {
                    stopService();
                }
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
        appInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });
        colorChangeRedButton.setOnClickListener(view -> colorChangeRed());
        colorChangeYellowButton.setOnClickListener(view -> colorChangeYellow());
        colorChangeBlackButton.setOnClickListener(view -> colorChangeBlack());
        colorChangeRainbowButton.setOnClickListener(view -> colorChangeRainbow());
    }

    private void startService() {
        Toast.makeText(getApplicationContext(), "서비스 시작", Toast.LENGTH_SHORT).show();
        startService(new Intent(getApplication(), ForegroundService.class));
        startAnimation();
    }

    private void stopService() {
        Toast.makeText(getApplicationContext(), "서비스 중지", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplication(), ForegroundService.class));
        stopAnimation();
    }

    private void startAnimation() {
        Glide.with(this).load(R.drawable.service_running).circleCrop().into(mainImageView);
        serviceStateImageView.setImageResource(R.drawable.service_state_on);
        serviceStateTextView.setText("동작");
        animationDrawable.start();
        serviceGuideTextView.setText("그림을 누르면 서비스가 정지합니다.");
    }

    private void stopAnimation() {
        Glide.with(this).load(R.drawable.service_stop).circleCrop().into(mainImageView);
        serviceStateImageView.setImageResource(R.drawable.service_state_off);
        serviceStateTextView.setText("정지");
        animationDrawable.stop();
        serviceGuideTextView.setText("그림을 누르면 서비스가 동작합니다.");
    }

    // theme changes
    private void colorChangeRed() {
        if (backgroundColor == R.drawable.background_red)
            return;

        backgroundColor = R.drawable.background_red;
        buttonColor = R.drawable.button_red;
        refreshBackground();
    }

    private void colorChangeYellow() {
        if (backgroundColor == R.drawable.background_yellow)
            return;

        backgroundColor = R.drawable.background_yellow;
        buttonColor = R.drawable.button_yellow;
        refreshBackground();
    }

    private void colorChangeBlack() {
        if (backgroundColor == R.drawable.background_black)
            return;

        backgroundColor = R.drawable.background_black;
        buttonColor = R.drawable.button_white;
        refreshBackground();
    }

    private void colorChangeRainbow() {
        if (backgroundColor == R.drawable.background_rainbow)
            return;

        backgroundColor = R.drawable.background_rainbow;
        buttonColor = R.drawable.button_mix;
        refreshBackground();
    }

    private void refreshBackground() {
        if (backgroundColor == -1 || buttonColor == -1) {
            backgroundColor = pref.getInt("theme_color", R.drawable.background_yellow);
            buttonColor = pref.getInt("button_color", R.drawable.button_yellow);
        }

        if (pref != null) {
            editor.putInt("theme_color", backgroundColor);
            editor.putInt("button_color", buttonColor);
            editor.commit();
        }

        editInfoButton.setBackground(getDrawable(buttonColor));
        openPreviewButton.setBackground(getDrawable(buttonColor));
        appInfoButton.setBackground(getDrawable(buttonColor));
        relativeLayout.setBackground(getDrawable(backgroundColor));
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(0);
        animationDrawable.setExitFadeDuration(4000);
        if (ForegroundService.isServiceRunning(getApplication())) {
            animationDrawable.start();
        }
    }
}