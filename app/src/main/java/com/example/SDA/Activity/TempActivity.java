package com.example.SDA.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.SDA.R;

public class TempActivity extends AppCompatActivity {
    //private SharedPreferences pref;
    //private SharedPreferences.Editor editor;

    //private String uid;

    Button temp1;
    Button temp2;
    Button temp3;
    Button temp4;
    Button temp5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        //pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        //editor = pref.edit();

        //uid = pref.getString("uid", null);

        temp1 = (Button) findViewById(R.id.temp1);
        temp2 = (Button) findViewById(R.id.temp2);
        temp3 = (Button) findViewById(R.id.temp3);
        temp4 = (Button) findViewById(R.id.temp4);
        temp5 = (Button) findViewById(R.id.temp5);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            temp1.setVisibility(View.GONE);
            temp2.setVisibility(View.VISIBLE);
            temp3.setVisibility(View.VISIBLE);
            temp4.setVisibility(View.GONE);
            temp5.setVisibility(View.GONE);
        } else {
            temp1.setVisibility(View.VISIBLE);
            temp2.setVisibility(View.GONE);
            temp3.setVisibility(View.GONE);
            temp4.setVisibility(View.VISIBLE);
            temp5.setVisibility(View.VISIBLE);
        }

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

        temp5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                //if (pref != null) {
                    //editor.remove("uid");
                    //editor.commit();
                //}
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}