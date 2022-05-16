package com.example.SDA.Service;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_PICTURES;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.SDA.Library.AnimatedGifEncoder;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class FirebaseStorageService {
    private static final String TAG = "FirebaseStorageService";

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference targetRef;

    public static String idToken;
    public static String careIdToken;
    public static String name;
    private FCMService messageService;

    public FirebaseStorageService() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        messageService = new FCMService();
    }

    public Bitmap byteArrayToBitmap( byte[] byteArray ) {
        Matrix matrix = new Matrix();
        matrix.preRotate(180, 0, 0);
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length) ;
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return bitmap;
    }

    private File saveAnimatedImage(Vector<byte[]> frames) {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
//        Date curDate = new Date(System.currentTimeMillis());
        String filename = idToken;

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

    public void deleteImageFromStorage(String idToken) {
        targetRef = storageRef.child(idToken + ".gif");
        targetRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    public void sendAnimatedImageToStorage(Vector<byte[]> frames) {
        File animatedImageFile = saveAnimatedImage(frames);
        UploadTask uploadTask;
        Uri file = Uri.fromFile(new File(animatedImageFile.getPath()));
        StorageReference GIFRef = storageRef.child(file.getLastPathSegment());

        uploadTask = GIFRef.putFile(file);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return GIFRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            //messageService.sentPostToFCM(careIdToken, name + "님에게 위험 상황이 발생하였습니다.");
                            Log.e(TAG, downloadUri.toString());
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });
            }
        });
    }
}
