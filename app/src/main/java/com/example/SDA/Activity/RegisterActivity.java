package com.example.SDA.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.SDA.R;
import com.example.SDA.Class.UserAccount;
import com.example.SDA.Service.FirebaseAuthService;
import com.example.SDA.Service.FirebaseDatabaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private FirebaseAuthService authService;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabaseService dbService;
    private DatabaseReference databaseRef;

    private EditText editTextRegisterId;
    private EditText editTextRegisterPwd;
    private EditText editTextRegisterName;
    private EditText editTextRegisterPhone;
    private EditText editTextRegisterAddress;
    private Button registerSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        authService = new FirebaseAuthService();
        firebaseAuth = authService.getAuth();
        dbService = new FirebaseDatabaseService();
        databaseRef = dbService.getReference();

        editTextRegisterId = (EditText) findViewById(R.id.editTextRegisterId);
        editTextRegisterPwd = (EditText) findViewById(R.id.editTextRegisterPwd);
        editTextRegisterName = (EditText) findViewById(R.id.editTextRegisterName);
        editTextRegisterPhone = (EditText) findViewById(R.id.editTextRegisterPhone);
        editTextRegisterAddress = (EditText) findViewById(R.id.editTextRegisterAddress);
        editTextRegisterAddress.setFocusable(false);
        editTextRegisterAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 주소 검색 웹뷰 화면으로 이동
                Intent intent = new Intent(RegisterActivity.this, AddressActivity.class);
                getSearchResult.launch(intent);
            }
        });

        registerSubmitButton = (Button) findViewById(R.id.registerSubmitButton);
        registerSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubmitButton();
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
                        editTextRegisterAddress.setText(data);
                    }
                }
            });

    private boolean checkIdPattern(String id) {
        int len = id.length();
        if (len < 4 || len > 12) {
            Toast.makeText(RegisterActivity.this,"아이디는 4~12글자만 가능합니다.",Toast.LENGTH_SHORT).show();
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
        if (len < 6 || len > 20) {
            Toast.makeText(RegisterActivity.this,"비밀번호는 6~20글자만 가능합니다.",Toast.LENGTH_SHORT).show();
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
        String _id = editTextRegisterId.getText().toString();
        String id = _id + "@sda.com";
        String pwd = editTextRegisterPwd.getText().toString();
        String name = editTextRegisterName.getText().toString();
        String phone = editTextRegisterPhone.getText().toString();
        String address = editTextRegisterAddress.getText().toString();

        if (!checkIdPattern(_id))
            return;
        if (!checkPwdPattern(pwd))
            return;
        if (!checkPhonePattern(phone))
            return;
        if (!checkAddressPattern(address))
            return;

        firebaseAuth.createUserWithEmailAndPassword(id,pwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    firebaseUser = authService.getUser();
                    UserAccount userAccount = new UserAccount();
                    userAccount.setIdToken(firebaseUser.getUid());
                    userAccount.setEmailId(firebaseUser.getEmail());
                    userAccount.setName(name);
                    userAccount.setPhone(phone);
                    userAccount.setAddress(address);
                    userAccount.setCareId("not_enroll");
                    databaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(userAccount);
                    databaseRef.child("UserIdToken").child(_id).setValue(firebaseUser.getUid());
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