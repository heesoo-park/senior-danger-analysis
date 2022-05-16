package com.example.SDA.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.Class.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Future;

public class FirebaseDatabaseService {
    private DatabaseReference databaseReference; //실시간 데이터베이스 처리
    public static final String UserAccount = "UserAccount";
    public static final String UserIdToken = "UserIdToken";
    public static final String SeniorListForCare = "SeniorListForCare";

    public FirebaseDatabaseService() {
        databaseReference = FirebaseDatabase.getInstance().getReference("capstone");
    }

    public DatabaseReference getReference() {
        return databaseReference;
    }
}
