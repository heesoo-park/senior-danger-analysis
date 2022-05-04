package com.example.SDA.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SDA.R;
import com.example.SDA.Service.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스 처리
    Button registerSubmitButton;
    EditText editTextRegisterId;
    EditText editTextRegisterPwd;
    EditText editTextRegisterName;
    EditText editTextRegisterPhone;
    EditText editTextRegisterAddress;
    String registrationToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFirebaseAuth=FirebaseAuth.getInstance();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("capstone");


        editTextRegisterId = (EditText) findViewById(R.id.editTextRegisterId);
        editTextRegisterPwd = (EditText) findViewById(R.id.editTextRegisterPwd);
        editTextRegisterName = (EditText) findViewById(R.id.editTextRegisterName);
        editTextRegisterPhone = (EditText) findViewById(R.id.editTextRegisterPhone);
        editTextRegisterAddress = (EditText) findViewById(R.id.editTextRegisterAddress);

        registerSubmitButton = (Button) findViewById(R.id.registerSubmitButton);
        registerSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubmitButton();
            }
        });
    }

    private void onClickSubmitButton() {
        // submit button event...
        String id = editTextRegisterId.getText().toString();
        String pwd = editTextRegisterPwd.getText().toString();
        String name = editTextRegisterName.getText().toString();
        String phone = editTextRegisterPhone.getText().toString();
        String address = editTextRegisterAddress.getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(id).matches()) {
            Toast.makeText(RegisterActivity.this,"이메일 양식이 아님", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        registrationToken = getString(R.string.msg_token_fmt, token);
                    }
                });
        mFirebaseAuth.createUserWithEmailAndPassword(id,pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    UserAccount account = new UserAccount();
                    account.setIdToken(firebaseUser.getUid());
                    account.setEmailId(firebaseUser.getEmail());
                    account.setPassword(pwd);
                    account.setName(name);
                    account.setPhone(phone);
                    account.setAddress(address);
                    account.setRegistrationToken(registrationToken);
                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                    Toast.makeText(RegisterActivity.this,"회원가입에 성공하셨습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this,"회원가입에 실패하셨습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}