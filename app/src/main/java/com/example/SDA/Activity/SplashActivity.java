package com.example.SDA.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.Class.UserAccount;
import com.example.SDA.R;
import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.FirebaseDatabaseService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private Context context;
    private FirebaseAuthService authService;
    private FirebaseDatabaseService dbService;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashImageView = (ImageView) findViewById(R.id.splashImageView);
        splashImageView.setImageResource(R.drawable.splash);

        context = this;
        authService = new FirebaseAuthService();
        dbService = new FirebaseDatabaseService();
        databaseRef = dbService.getReference();

        if (!authService.isLogin()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } else {
            databaseRef.child(FirebaseDatabaseService.UserAccount).child(authService.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserAccount userAccount = snapshot.getValue(UserAccount.class);
                    PreferenceManager.setString(context, PreferenceManager.NAME, userAccount.getName());
                    PreferenceManager.setString(context, PreferenceManager.ID_TOKEN, userAccount.getIdToken());
                    PreferenceManager.setString(context, PreferenceManager.ADDRESS, userAccount.getAddress());
                    PreferenceManager.setString(context, PreferenceManager.PHONE, userAccount.getPhone());
                    PreferenceManager.setString(context, PreferenceManager.CARE_ID, userAccount.getCareId());

                    // 보호자 정보가 등록되어 있지 않으면 CareEnrollmentActivity 이동
                    if (userAccount.getCareId().equals("not_enroll")) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(SplashActivity.this, CareEnrollmentActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 2000);
                        return;
                    }

                    PreferenceManager.setString(context, PreferenceManager.CARE_ID, userAccount.getCareId());
                    databaseRef.child(FirebaseDatabaseService.UserIdToken).child(userAccount.getCareId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Object obj = snapshot.getValue();
                            PreferenceManager.setString(context, PreferenceManager.CARE_ID_TOKEN, obj.toString());
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}