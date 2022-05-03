package com.example.MyApplication.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MyApplication.R;

public class RegisterActivity extends AppCompatActivity {
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

        registerSubmitButton = (Button) findViewById(R.id.registerSubmitButton);
        editTextRegisterId = (EditText) findViewById(R.id.editTextRegisterId);
        editTextRegisterPwd = (EditText) findViewById(R.id.editTextRegisterPwd);
        editTextRegisterName = (EditText) findViewById(R.id.editTextRegisterName);
        editTextRegisterPhone = (EditText) findViewById(R.id.editTextRegisterPhone);
        editTextRegisterAddress = (EditText) findViewById(R.id.editTextRegisterAddress);

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
        String category = null;
        Toast.makeText(getApplicationContext(), "Submit Button Event\nID: " + id + "\nPWD: "+ pwd + "\nCategory: "+ category
                + "\nName: "+ name + "\nPhone Number: "+ phone + "\nAddress: "+ address, Toast.LENGTH_SHORT).show();
    }

}