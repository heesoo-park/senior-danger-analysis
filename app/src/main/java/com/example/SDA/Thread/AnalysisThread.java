package com.example.SDA.Thread;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.SDA.Library.AnimatedGifEncoder;
import com.example.SDA.Class.UserAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
public class AnalysisThread extends Thread {
    private static final String TAG = "AnalysisThread";
    private static final int BATCH_SIZE = 30;
    private static final int IMAGE_HEIGHT = 128;
    private static final int IMAGE_WIDTH = 128;
    private static final int IMAGE_CHANNEL = 3;
    Vector<byte[]> frames;
    private int[][][][] input = new int[BATCH_SIZE][IMAGE_HEIGHT][IMAGE_WIDTH][IMAGE_CHANNEL];;
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
            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "SDA" + File.separator;
            File folder = new File(strFolderName);

            if(!folder.exists()) {
                folder.mkdirs();
            }
            File outputFile = new File(strFolderName + "/" + filename + ".jpg");
            return outputFile;
        }
    }

    public Bitmap byteArrayToBitmap( byte[] byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
        return bitmap;
    }

    private void saveJpegImage(byte[] data) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            return;
        } else {
            String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "SDA" + File.separator;
            File folder = new File(strFolderName);

            if (!folder.exists()) {
                folder.mkdirs();
            }
            File picture_file = getOutputFile();

            if (picture_file == null) {
                return;
            } else {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();
                    //Log.e(TAG, "File Write!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    private File getOutputTextFile() {
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

    private int[][][] convertBitmapToRgb(Bitmap bitmap) {
        int width = 128;
        int height = 128;
        bitmap = Bitmap.createScaledBitmap(bitmap, height, width, true);
        int[][][] image = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = bitmap.getPixel(x, y);
                image[y][x][0] = Color.red(px);
                image[y][x][1] = Color.green(px);
                image[y][x][2] = Color.blue(px);
            }
        }
        return image;
    }

    private void saveTemp(int[][][] image) {
        File outputFile = getOutputTextFile();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write("[ ");
            for (int[][] h: image) {
                writer.write("[ ");
                for (int[] w: h) {
                    writer.write("[ ");
                    for (int c: w) {
                        writer.write(Integer.toString(c) + ", ");
                    }
                    writer.write("], ");
                }
                writer.write("], ");
            }
            writer.write("]");
            writer.close();
            Log.e(TAG, "SAVE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTemp2() {
        File outputFile = getOutputTextFile();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write("[ ");
            for (int[][][] batch: input) {
                writer.write("[ ");
                for (int[][] h: batch) {
                    writer.write("[ ");
                    for (int[] w: h) {
                        writer.write("[ ");
                        for (int c: w) {
                            writer.write(Integer.toString(c) + ", ");
                        }
                        writer.write("], ");
                    }
                    writer.write("], ");
                }
                writer.write("], \n\n\n\n\n\n");
            }
            writer.write("]");

            writer.close();
            Log.e(TAG, "SAVE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeInputTensor(int i) {
        Bitmap bitmap;
        bitmap = byteArrayToBitmap(frames.get(i));
        bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_HEIGHT, IMAGE_WIDTH, true);
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                int px = bitmap.getPixel(x, y);
                input[i][y][x][0] = Color.red(px);
                input[i][y][x][1] = Color.green(px);
                input[i][y][x][2] = Color.blue(px);
            }
        }
    }

    private void func() {
        for (int i = 0; i < BATCH_SIZE; i++) {
            makeInputTensor(i);
        }
    }

    @Override
    public void run() {
        Log.e(TAG, "Analysis Thread " + num + " Start...");
        //saveAnimatedImage();
        //func();
        //saveTemp2();

        //Log.e(TAG, "모델 처리 대략 5초 걸린다고 가정...");

//        try {
//            sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        sentPostToFCM("4oDpCWV3plUwPb0hOUZfiDoyeq72", "위험 상황이 발생하였습니다.");
        //Log.e(TAG, "5초 지남 : " + num + " Thread 모델 분석이 끝났음");
        Log.e(TAG, "Analysis Thread " + num + " Finish...");
    }
}
