package com.example.SDA.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.R;

import java.util.HashMap;

public class CareEnrollmentActivity extends AppCompatActivity {
    Button careEnrollButton;
    EditText editTextCareId;
    String idToken;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_enrollment);
        mFirebaseAuth = FirebaseAuth.getInstance();
        careEnrollButton = (Button) findViewById(R.id.careEnrollButton);
        editTextCareId = (EditText) findViewById(R.id.editTextCareId);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("capstone");
        careEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCareEnrollButton();
            }
        });
    }

    private void onClickCareEnrollButton() {
        // 등록 버튼 클릭했을 때 이벤트 처리...
        String id = editTextCareId.getText().toString();
        mDatabaseRef.child("UserIdToken").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(CareEnrollmentActivity.this,"보호자 등록에 실패하셨습니다", Toast.LENGTH_SHORT).show();
                } else {
                    idToken = String.valueOf(task.getResult().getValue());
                    if (idToken == "null") {
                        Toast.makeText(CareEnrollmentActivity.this,"존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        return;
                    }
                    mFirebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("user").build())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("protector", id);
                                    hashMap.put("protectorToken", idToken);
                                    mDatabaseRef.child("UserAccount").child(currentUser.getUid()).updateChildren(hashMap);
                                    Toast.makeText(CareEnrollmentActivity.this,"보호자 등록에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
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
