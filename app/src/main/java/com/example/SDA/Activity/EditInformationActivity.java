package com.example.SDA.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.Class.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.SDA.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditInformationActivity extends AppCompatActivity{
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private Button updateButton;
    private Button logoutButton;

    private EditText editTextUpdateName;
    private EditText editTextUpdateProtector;
    private EditText editTextUpdateAddress;
    private EditText editTextUpdatePhone;
    private EditText editTextCurrentPassword;
    private EditText editTextChangePassword;
    private EditText editTextChangePassword2;

    private String name;
    private String protector;
    private String address;
    private String phone;
    private String currentPassword;
    private String changePassword;
    private String changePassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        updateButton = (Button) findViewById(R.id.updateButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        editTextUpdateName = (EditText)findViewById(R.id.editTextUpdateName);
        editTextUpdateProtector = (EditText)findViewById(R.id.editTextUpdateProtector);
        editTextUpdateAddress = (EditText)findViewById(R.id.editTextUpdateAddress);
        editTextUpdatePhone = (EditText)findViewById(R.id.editTextUpdatePhone);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("capstone");

        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserAccount userAccount = snapshot.getValue(UserAccount.class);
                editTextUpdateName.setText(userAccount.getName());
                editTextUpdateProtector.setText(userAccount.getProtector());
                editTextUpdateAddress.setText(userAccount.getAddress());
                editTextUpdatePhone.setText(userAccount.getPhone());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//                } else {
//                    Object obj = task.getResult();
//                    Log.e("firebase", "" + obj);
//                }
//            }
//        });

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
                protector = editTextUpdateProtector.getText().toString();
                // password 변경 추가
                phone = editTextUpdatePhone.getText().toString();
                address = editTextUpdateAddress.getText().toString();

                firebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("user").build())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void unused) {
                                                      FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                                      HashMap<String,Object> hashMap = new HashMap<>();
                                                      hashMap.put("name", name);
                                                      hashMap.put("phone", phone);
                                                      hashMap.put("address", address);
                                                      mDatabaseRef.child("UserAccount").child(currentUser.getUid()).updateChildren(hashMap);
                                                      Toast.makeText(EditInformationActivity.this,"정보수정에 성공하셨습니다", Toast.LENGTH_SHORT).show();
                                                      finish();
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
}
