package com.example.MyApplication.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MyApplication.MainActivity;
import com.example.MyApplication.R;
import com.example.MyApplication.View.CameraSurfaceView;

public class PreviewActivity extends AppCompatActivity {
    private CameraSurfaceView cameraSurfaceView;
    private Button closePreviewButton;

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        cameraSurfaceView = new CameraSurfaceView(PreviewActivity.this);
        closePreviewButton = (Button) findViewById(R.id.closePreviewButton);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout.addView(cameraSurfaceView);
        frameLayout.setVisibility(View.VISIBLE);

        closePreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraSurfaceView.destroy();
                finish();
            }
        });
    }
}