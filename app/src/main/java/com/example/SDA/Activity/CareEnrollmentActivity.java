package com.example.SDA.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.SDA.R;

public class CareEnrollmentActivity extends AppCompatActivity {
    Button careEnrollButton;
    EditText editTextCareId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_enrollment);

        careEnrollButton = (Button) findViewById(R.id.careEnrollButton);
        editTextCareId = (EditText) findViewById(R.id.editTextCareId);

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
        Toast.makeText(getApplicationContext(), "Care Enroll Button Event\nID: " + id, Toast.LENGTH_SHORT).show();
    }
}