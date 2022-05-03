package com.example.SDA.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.R;

public class LoginActivity extends AppCompatActivity {
    Button loginButton;
    Button registerButton;
    EditText editTextLoginId;
    EditText editTextLoginPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);
        editTextLoginId = (EditText) findViewById(R.id.editTextLoginId);
        editTextLoginPwd = (EditText) findViewById(R.id.editTextLoginPwd);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLoginButton();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRegisterButton();
            }
        });
    }

    private void onClickLoginButton() {
        // 로그인 버튼 클릭했을 때 이벤트 처리...
        String id = editTextLoginId.getText().toString();
        String pwd = editTextLoginPwd.getText().toString();
        Toast.makeText(getApplicationContext(), "Login Button Event\nID: " + id + "\nPWD: "+ pwd, Toast.LENGTH_SHORT).show();
    }

    private void onClickRegisterButton() {
        // 회원가입 버튼 클릭했을 때 이벤트 처리...
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }
}
