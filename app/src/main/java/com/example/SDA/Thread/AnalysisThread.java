package com.example.SDA.Thread;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_PICTURES;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.SDA.Library.AnimatedGifEncoder;
import com.example.SDA.Service.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class AnalysisThread extends Thread {
    private static final String TAG = "AnalysisThread";
    Vector<byte[]> frames;
    private final int num;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public AnalysisThread(int num, Vector<byte[]> frames) {
        this.num = num;
        this.frames = frames;
    }

    private File getOutputFile() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS) + File.separator + "SDA" + File.separator;
            File folder = new File(strFolderName);

            if(!folder.exists()) {
                folder.mkdirs();
            }
            File outputFile = new File(strFolderName + "/" + filename + ".txt");
            return outputFile;
        }
    }

    public Bitmap byteArrayToBitmap( byte[] byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
        return bitmap ;
    }

    private File saveAnimatedImage() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "SDA" + File.separator;
            File folder = new File(strFolderName);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();

            encoder.start(bos);
            // Convert to bitmap...
            for (byte[] frame: frames) {
                Bitmap bitmap = byteArrayToBitmap(frame);
                encoder.addFrame(bitmap);
            }
            encoder.finish();

            byte[] data = bos.toByteArray();
            File animatedImageFile = new File(strFolderName + "/" + filename + ".gif");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(animatedImageFile);
                fos.write(data);
                fos.close();
                Log.e(TAG, "Make Animated Image: " + animatedImageFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return animatedImageFile;
        }
    }

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    // 서버키는 자기걸로 채워넣기
    private static final String SERVER_KEY = "AAAAz6URi6o:APA91bGfLNKEcZg8C_hZxx1zBCF_3BI6iiVyviNWGQSynbFewcTB3C9MNM7aVGmTSBaUKJYfI5SXly31rTAQW_PlGSHG3taSz33HHFB9RJZzZllbDKsqzUioqSKyldTVQBjtfydUI-Ww";
    private void sentPostToFCM(final String idToken, final String message) {
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
                                    notification.put("body", message);
                                    notification.put("title", "SDA");
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

    @Override
    public void run() {
        Log.e(TAG, "Analysis Thread " + num + " Start...");
        //saveAnimatedImage();
        Log.e(TAG, "모델 처리 대략 5초 걸린다고 가정...");

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sentPostToFCM("QnGj2eKuSnVjl9nZo6r2ZDRzEHx1", "hi!");
        Log.e(TAG, "5초 지남 : " + num + " Thread 모델 분석이 끝났음");
        Log.e(TAG, "Analysis Thread " + num + " Finish...");
    }
}
