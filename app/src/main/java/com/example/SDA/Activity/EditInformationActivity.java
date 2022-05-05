package com.example.SDA.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.Service.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.SDA.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditInformationActivity extends AppCompatActivity{
    DatabaseReference mDatabaseRef;
    Button updateButton;
    FirebaseAuth firebaseAuth;
    EditText name;
    EditText protector;
    EditText address;
    EditText phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);
        updateButton=findViewById(R.id.registerSubmitButton);
        name=(EditText)findViewById(R.id.name);
        protector=(EditText)findViewById(R.id.protector);
        address=(EditText)findViewById(R.id.address);
        phone=(EditText)findViewById(R.id.phone);
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("capstone");
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Name = name.getText().toString();
                String Protector=protector.getText().toString();
                String Phone = phone.getText().toString();
                String Address = address.getText().toString();


                firebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("user").build())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                  @Override
                                                  public void onSuccess(Void unused) {
                                                      FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                                      HashMap<String,Object>hashMap=new HashMap<>();
                                                      hashMap.put("name",Name);
                                                      hashMap.put("phone",Phone);
                                                      hashMap.put("address",Address);
                                                      mDatabaseRef.child("UserAccount").child(currentUser.getUid()).updateChildren(hashMap);
                                                      Toast.makeText(EditInformationActivity.this,"정보수정에 성공하셨습니다", Toast.LENGTH_SHORT).show();
                                                  }
                                              }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditInformationActivity.this,"정보수정에 실패하셨습니다", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
