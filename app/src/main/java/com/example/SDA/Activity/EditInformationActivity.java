package com.example.SDA.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.FirebaseDatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.SDA.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditInformationActivity extends AppCompatActivity{
    private FirebaseAuthService authService;
    private FirebaseDatabaseService dbService;
    private DatabaseReference databaseRef;

    private Button updateButton;
    private Button logoutButton;

    private EditText editTextUpdateName;
    private EditText editTextUpdateCareId;
    private EditText editTextUpdateAddress;
    private EditText editTextUpdatePhone;

    private String name;
    private String careId;
    private String address;
    private String phone;
    private String prevCareId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);
        authService = new FirebaseAuthService();
        dbService = new FirebaseDatabaseService();
        databaseRef = dbService.getReference();

        updateButton = (Button) findViewById(R.id.updateButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        editTextUpdateName = (EditText)findViewById(R.id.editTextUpdateName);
        editTextUpdateCareId = (EditText)findViewById(R.id.editTextUpdateCareId);
        editTextUpdateAddress = (EditText)findViewById(R.id.editTextUpdateAddress);
        editTextUpdatePhone = (EditText)findViewById(R.id.editTextUpdatePhone);
        editTextUpdateAddress.setFocusable(false);
        editTextUpdateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 주소 검색 웹뷰 화면으로 이동
                Intent intent = new Intent(EditInformationActivity.this, AddressActivity.class);
                getSearchResult.launch(intent);
            }
        });

        name = PreferenceManager.getString(this, PreferenceManager.NAME);
        careId = PreferenceManager.getString(this, PreferenceManager.CARE_ID);
        address = PreferenceManager.getString(this, PreferenceManager.ADDRESS);
        phone = PreferenceManager.getString(this, PreferenceManager.PHONE);
        prevCareId = careId;

        editTextUpdateName.setText(name);
        editTextUpdateCareId.setText(careId);
        editTextUpdateAddress.setText(address);
        editTextUpdatePhone.setText(phone);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EditInformationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = editTextUpdateName.getText().toString();
                careId = editTextUpdateCareId.getText().toString();
                phone = editTextUpdatePhone.getText().toString();
                address = editTextUpdateAddress.getText().toString();

                authService.getUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("user").build())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                databaseRef.child(FirebaseDatabaseService.UserIdToken).child(careId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Object obj = snapshot.getValue();
                                        HashMap<String,Object> hashMap = new HashMap<>();
                                        hashMap.put("name", name);
                                        hashMap.put("phone", phone);
                                        hashMap.put("address", address);
                                        hashMap.put("careId", careId);
                                        // 보호자 수정 기능 추가해야함
                                        databaseRef.child(FirebaseDatabaseService.SeniorListForCare).child(prevCareId).removeValue();
                                        databaseRef.child(FirebaseDatabaseService.UserAccount).child(authService.getUid()).updateChildren(hashMap);
                                        Toast.makeText(EditInformationActivity.this,"정보수정에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                                        ActivityCompat.finishAffinity(EditInformationActivity.this);
                                        Intent intent = new Intent(EditInformationActivity.this, SplashActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditInformationActivity.this,"정보수정에 실패하셨습니다", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Search Activity 로부터의 결과 값이 이곳으로 전달 된다.
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        String data = result.getData().getStringExtra("data");
                        editTextUpdateAddress.setText(data);
                    }
                }
            });
}
