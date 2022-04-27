package com.example.MyApplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MyApplication.R;

public class TempActivity extends AppCompatActivity {
    Button temp1;
    Button temp2;
    Button temp3;
    Button temp4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        temp1 = (Button) findViewById(R.id.temp1);
        temp2 = (Button) findViewById(R.id.temp2);
        temp3 = (Button) findViewById(R.id.temp3);
        temp4 = (Button) findViewById(R.id.temp4);

        temp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditInformationActivity.class);
                startActivity(intent);
            }
        });

        temp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        temp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        temp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CareEnrollmentActivity.class);
                startActivity(intent);
            }
        });
    }
}