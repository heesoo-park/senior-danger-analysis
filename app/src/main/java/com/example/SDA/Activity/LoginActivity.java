package com.example.SDA.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SDA.Class.UserAccount;
import com.example.SDA.R;
import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.FirebaseDatabaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuthService authService;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabaseService dbService;
    private DatabaseReference databaseRef;

    private Button loginButton;
    private Button registerButton;
    private EditText editTextLoginId;
    private EditText editTextLoginPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new FirebaseAuthService();
        firebaseAuth = authService.getAuth();
        dbService = new FirebaseDatabaseService();
        databaseRef = dbService.getReference();

        editTextLoginId = (EditText) findViewById(R.id.editTextLoginId);
        editTextLoginPwd = (EditText) findViewById(R.id.editTextLoginPwd);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(view -> onClickLoginButton());
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> onClickRegisterButton());
    }

    private void onClickLoginButton() {
        String id = editTextLoginId.getText().toString();
        String pwd = editTextLoginPwd.getText().toString();

        if (id.length() == 0) {
            Toast.makeText(LoginActivity.this,"아이디를 입력하세요!",Toast.LENGTH_SHORT).show();
            return;
        } else if (pwd.length() == 0) {
            Toast.makeText(LoginActivity.this,"비밀번호를 입력하세요!",Toast.LENGTH_SHORT).show();
            return;
        }
        id += "@sda.com";

        firebaseAuth.signInWithEmailAndPassword(id, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    moveNextActivity();
                } else {
                    Toast.makeText(LoginActivity.this,"로그인 실패",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void moveNextActivity() {
        databaseRef.child(FirebaseDatabaseService.UserAccount).child(authService.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent;
                UserAccount userAccount = snapshot.getValue(UserAccount.class);
                if (userAccount.getCareId().equals("not_enroll")) {
                    intent = new Intent(LoginActivity.this, CareEnrollmentActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onClickRegisterButton() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}