package com.example.SDA.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.FirebaseDatabaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.R;

import java.util.HashMap;

public class CareEnrollmentActivity extends AppCompatActivity {
    private FirebaseAuthService authService;
    private FirebaseDatabaseService dbService;
    private DatabaseReference databaseRef;

    private EditText editTextCareId;
    private Button careEnrollButton;
    private String careIdToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_enrollment);
        authService = new FirebaseAuthService();
        dbService = new FirebaseDatabaseService();
        databaseRef = dbService.getReference();

        careEnrollButton = (Button) findViewById(R.id.careEnrollButton);
        editTextCareId = (EditText) findViewById(R.id.editTextCareId);
        careEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCareEnrollButton();
            }
        });
    }

    private void onClickCareEnrollButton() {
        // 등록 버튼 클릭했을 때 이벤트 처리...
        String careId = editTextCareId.getText().toString();
        databaseRef.child(FirebaseDatabaseService.UserIdToken).child(careId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(CareEnrollmentActivity.this,"보호자 등록에 실패하셨습니다", Toast.LENGTH_SHORT).show();
                } else {
                    careIdToken = String.valueOf(task.getResult().getValue());
                    if (careIdToken == "null") {
                        Toast.makeText(CareEnrollmentActivity.this,"존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        return;
                    }
                    authService.getUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("user").build())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("careId", careId);
                                    //hashMap.put("protectorToken", idToken);
                                    databaseRef.child(FirebaseDatabaseService.UserAccount).child(authService.getUid()).updateChildren(hashMap);
                                    databaseRef.child(FirebaseDatabaseService.SeniorListForCare).child(careId).child(authService.getUid()).setValue(1);
                                    Toast.makeText(CareEnrollmentActivity.this,"보호자 등록에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CareEnrollmentActivity.this, SplashActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CareEnrollmentActivity.this,"보호자 등록에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
