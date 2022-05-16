package com.example.SDA.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.SDA.Class.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMService {
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAz6URi6o:APA91bGfLNKEcZg8C_hZxx1zBCF_3BI6iiVyviNWGQSynbFewcTB3C9MNM7aVGmTSBaUKJYfI5SXly31rTAQW_PlGSHG3taSz33HHFB9RJZzZllbDKsqzUioqSKyldTVQBjtfydUI-Ww";

    private static final String TAG = "FCMMessageService";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public FCMService() {}

    public void sentPostToFCM(final String idToken, final String message, String router) {
        //String token = FirebaseMessaging.getInstance().getToken().getResult(); // 등록 토큰 확인용
        database.getReference("capstone/UserAccount")
                .child(idToken)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserAccount userAccount = new UserAccount();
                        userAccount.setIdToken(idToken);
                        userAccount.setRegistrationToken(snapshot.child("registrationToken").getValue(String.class));
                        Log.e(TAG,userAccount.getIdToken() + " / "+ userAccount.getRegistrationToken());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject root = new JSONObject();
                                    JSONObject notification = new JSONObject();
                                    JSONObject data = new JSONObject();
                                    data.put("title", "SDA");
                                    data.put("message", router);
                                    notification.put("body", message);
                                    notification.put("title", "SDA");
                                    root.put("data", data);
                                    root.put("notification",notification);
                                    root.put("to",userAccount.getRegistrationToken());

                                    URL Url = new URL(FCM_MESSAGE_URL);
                                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);
                                    conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                                    conn.setRequestProperty("Accept", "application/json");
                                    conn.setRequestProperty("Content-type", "application/json");
                                    OutputStream os = conn.getOutputStream();
                                    os.write(root.toString().getBytes("utf-8"));
                                    os.flush();
                                    conn.getResponseCode();

                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
