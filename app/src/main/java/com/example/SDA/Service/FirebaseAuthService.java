package com.example.SDA.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthService {
    private FirebaseAuth firebaseAuth;

    public FirebaseAuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getUser() {
        return firebaseAuth.getCurrentUser();
    }

    public FirebaseAuth getAuth() {
        return this.firebaseAuth;
    }

    public String getUid() {
        FirebaseUser firebaseUser = getUser();
        return firebaseUser.getUid();
    }

    public boolean isLogin() {
        FirebaseUser firebaseUser = getUser();
        return firebaseUser != null;
    }
}
