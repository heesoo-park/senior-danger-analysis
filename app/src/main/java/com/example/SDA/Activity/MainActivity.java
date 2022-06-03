package com.example.SDA.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SDA.Class.AnalysisResult;
import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.R;
import com.example.SDA.Service.CameraService;
import com.example.SDA.Service.FCMService;
import com.example.SDA.Service.ForegroundService;
import com.example.SDA.Thread.StorageThread;
import com.example.SDA.View.CameraSurfaceView;
import com.example.SDA.View.PoseDrawView;

public class MainActivity extends AppCompatActivity {
    public static Context context;

    private Button serviceButton;
    private Button editInfoButton;
    private Button careCallButton;

    private ImageView serviceStateImageView;
    private TextView serviceStateTextView;

    private FCMService messageService;

    private FrameLayout frameLayout;
    private CameraSurfaceView cameraSurfaceView;
    private FrameLayout drawFrameLayout;
    private PoseDrawView drawView;
    private TextView resultTextView;

    private Thread updateThread;

    Handler handler = new Handler();

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
        if (!ForegroundService.isServiceRunning(getApplication())) {
            startService();
        } else {
            updateUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

            if (permissionGranted) {
                initLayout();
            } else {
                Toast.makeText(this,
                        "권한을 허용해야 SDA 앱을 이용하실 수 있습니다.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initLayout() {

        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        serviceButton = (Button) findViewById(R.id.serviceButton);
        editInfoButton = (Button) findViewById(R.id.editInfoButton);
        careCallButton = (Button) findViewById(R.id.careCallButton);

        serviceStateImageView = (ImageView) findViewById(R.id.serviceStateImageView);
        serviceStateTextView = (TextView) findViewById(R.id.serviceStateTextView);
        messageService = new FCMService();

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        drawFrameLayout = (FrameLayout) findViewById(R.id.drawFrameLayout);
        drawView = new PoseDrawView(this);
        drawFrameLayout.addView(drawView);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        serviceButton.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(MainActivity.this, EditInformationActivity.class);
                startActivity(intent);
            }
        });
        careCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String careIdToken = PreferenceManager.getString(MainActivity.this, PreferenceManager.CARE_ID_TOKEN);
                String seniorName = PreferenceManager.getString(MainActivity.this, PreferenceManager.NAME);
                if (careIdToken == null) {
                    Toast.makeText(getApplicationContext(), "보호자 정보에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String msg = seniorName + "님이 보호자님을 호출하였습니다!";
                messageService.sentPostToFCM(careIdToken, msg, "0");
                Toast.makeText(getApplicationContext(), "보호자에게 알림을 보냈습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startService() {
        Toast.makeText(getApplicationContext(), "서비스 시작", Toast.LENGTH_SHORT).show();
        startService(new Intent(getApplication(), ForegroundService.class));
        updateUI();
    }

    private void stopService() {
        Toast.makeText(getApplicationContext(), "서비스 중지", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getApplication(), ForegroundService.class));
        serviceButton.setText(R.string.btn_text_service_on);
        serviceStateImageView.setImageResource(R.drawable.service_state_off);
        serviceStateTextView.setText("정지");
        frameLayout.setVisibility(View.INVISIBLE);
        frameLayout.removeView(cameraSurfaceView);
        drawFrameLayout.setVisibility(View.INVISIBLE);
        resultTextView.setVisibility(View.INVISIBLE);
    }

    private void updateUI() {
        serviceButton.setText(R.string.btn_text_service_off);
        serviceStateImageView.setImageResource(R.drawable.service_state_on);
        serviceStateTextView.setText("동작");
        openPreview();
        if (updateThread == null || !updateThread.isAlive()) {
            runOnUiThread();
        }
    }

    private void runOnUiThread() {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(ForegroundService.isServiceRunning(context)) {
                    if (resultTextView == null || CameraService.analysisResult == null) {
                        continue;
                    }

                    AnalysisResult analysisResult;
                    float lastRatio, ratio;
                    int result;
                    String resultText = "";

                    analysisResult = CameraService.analysisResult;
                    lastRatio = analysisResult.getLastRatio();
                    ratio = analysisResult.getRatio();
                    result = analysisResult.getResult();

                    resultText += "lastRatio : " + lastRatio + "\n";
                    resultText += "currRatio : " + ratio + "\n";

                    if (result == AnalysisResult.RESULT_FALL_DOUBT) {
                        resultText += "낙상 의심 \n";
                    } else if (result == AnalysisResult.RESULT_NOTHING) {
                        resultText = "객체가 탐지 되지 않음\n";
                    } else if (result == AnalysisResult.RESULT_WAITING) {
                        resultText = "2차 Task 분석중...\n";
                    } else if (result == AnalysisResult.RESULT_FALL_RECOGNIZE) {
                        resultText = "낙상 인지! 보호자에게 전송\n";
                    } else if (result == AnalysisResult.RESULT_NOT_HAPPENED) {

                    }

                    String finalResultText = resultText;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText(finalResultText);
                        }
                    });

                    if (result == AnalysisResult.RESULT_FALL_RECOGNIZE) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        updateThread.start();
    }

    private void openPreview() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(CameraService.camera == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        frameLayout.removeView(cameraSurfaceView);
                        cameraSurfaceView = new CameraSurfaceView(MainActivity.this, CameraService.camera);
                        frameLayout.addView(cameraSurfaceView);
                        frameLayout.setVisibility(View.VISIBLE);
                        drawFrameLayout.setVisibility(View.VISIBLE);
                        resultTextView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        thread.start();
    }
}