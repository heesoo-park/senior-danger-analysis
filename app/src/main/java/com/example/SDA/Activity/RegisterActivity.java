package com.example.SDA.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SDA.MainActivity;
import com.example.SDA.R;
import com.example.SDA.Class.UserAccount;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("capstone");

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

    private boolean checkIdPattern(String id) {
        int len = id.length();
        if (len < 6 || len > 12) {
            Toast.makeText(RegisterActivity.this,"아이디는 6~12글자만 가능합니다.",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z0-9]*$", id)) {
            Toast.makeText(RegisterActivity.this,"아이디는 영문, 숫자만 사용할 수 있습니다.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPwdPattern(String pwd) {
        int len = pwd.length();
        if (len < 8 || len > 20) {
            Toast.makeText(RegisterActivity.this,"비밀번호는 8~20글자만 가능합니다.",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", pwd))
        {
            Toast.makeText(RegisterActivity.this,"대문자, 소문자, 특수문자, 숫자를 하나씩 사용해주세요.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPhonePattern(String phone) {
        int len = phone.length();
        if (len == 0) {
            Toast.makeText(RegisterActivity.this,"전화번호는 필수로 입력해주세요.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkAddressPattern(String address) {
        int len = address.length();
        if (len == 0) {
            Toast.makeText(RegisterActivity.this,"주소는 필수로 입력해주세요.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void onClickSubmitButton() {
        // submit button event...
        String id = editTextRegisterId.getText().toString();
        String pwd = editTextRegisterPwd.getText().toString();
        String name = editTextRegisterName.getText().toString();
        String phone = editTextRegisterPhone.getText().toString();
        String address = editTextRegisterAddress.getText().toString();

        if (!checkIdPattern(id))
            return;
        if (!checkPwdPattern(pwd))
            return;
        if (!checkPhonePattern(phone))
            return;
        if (!checkAddressPattern(phone))
            return;

        id += "@SDA.com";

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
                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                    Toast.makeText(RegisterActivity.this,"회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,"회원가입에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}