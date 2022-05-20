package com.example.SDA.Thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.example.SDA.Activity.MainActivity;
import com.example.SDA.Class.PreferenceManager;
import com.example.SDA.Model.My3DPoseModel;
import com.example.SDA.Model.STGCNModel;
import com.example.SDA.Service.FCMService;
import com.example.SDA.Service.FirebaseStorageService;
import com.google.mlkit.vision.pose.Pose;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;

public class StorageThread extends Thread {
    private static final String TAG = "ConsumerThread";
    private byte[] data;
    private FirebaseStorageService storageService;
    private FCMService messageService;
    private int width, height;

    public StorageThread(byte[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
        storageService = new FirebaseStorageService();
        messageService = new FCMService();
    }

    private File convertBitmapToFile(Bitmap bitmap) {
        File imageFile = null;
        OutputStream os = null;
        try {
            imageFile = File.createTempFile("img_", ".jpg");
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageFile;
    }

    private Bitmap convertYUV2Bitmap(byte[] data, Rect crop) {
        Bitmap bitmap;
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(180);
        if (crop == null) {
            crop = new Rect(0, 0, width, height);
        }
        bitmap = Bitmap.createBitmap(crop.width(), crop.height(), Bitmap.Config.ARGB_8888);
        int yv = 0, uv = 0, vv = 0;

        for (int y = crop.top; y < crop.bottom; y += 1) {
            for (int x = crop.left; x < crop.right; x += 1) {
                yv = data[y * width + x] & 0xff;
                uv = (data[width * height + (x / 2) * 2 + (y / 2) * width + 1] & 0xff) - 128;
                vv = (data[width * height + (x / 2) * 2 + (y / 2) * width] & 0xff) - 128;
                bitmap.setPixel(x, y, convertPixel(yv, uv, vv));
            }
        }

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);
        return bitmap;
    }

    private int convertPixel(int y, int u, int v) {
        int r = (int) (y + 1.13983f * v);
        int g = (int) (y - .39485f * u - .58060f * v);
        int b = (int) (y + 2.03211f * u);
        r = (r > 255) ? 255 : (r < 0) ? 0 : r;
        g = (g > 255) ? 255 : (g < 0) ? 0 : g;
        b = (b > 255) ? 255 : (b < 0) ? 0 : b;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public void run() {
        Log.e(TAG, "Storage Thread Start...");
        Bitmap bitmap = convertYUV2Bitmap(data, null);
        File imageFile = convertBitmapToFile(bitmap);
        storageService.saveImageToStorage(imageFile);

        String careIdToken = PreferenceManager.getString(MainActivity.context, PreferenceManager.CARE_ID_TOKEN);
        String seniorName = PreferenceManager.getString(MainActivity.context, PreferenceManager.NAME);
        if (careIdToken == null) {
            return;
        }
        String msg = seniorName + "님에게 위험 상황이 발생하였습니다";
        messageService.sentPostToFCM(careIdToken, msg, imageFile.getName());
        Log.e(TAG, "Storage Thread Finish...");
    }
}
