package com.example.MyApplication.Thread;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_PICTURES;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.MyApplication.Library.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class AnalysisThread extends Thread {
    private static final String TAG = "AnalysisThread";
    Vector<byte[]> frames;
    private int num;

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

    private File getAnimatedImage() {
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

    @Override
    public void run() {
        Log.e(TAG, "Analysis Thread " + num + " Start...");
        File savedFile = getOutputFile();
/*
        if (savedFile == null) {
            return;
        } else {
            FileOutputStream fos = null;
            BufferedWriter writer = null;
            try {
                fos = new FileOutputStream(savedFile);
                writer = new BufferedWriter(new OutputStreamWriter(fos));

                for (byte[] frame: frames) {
                    for (byte b: frame) {
                        writer.write(Integer.toString((int) b) + " ");
                    }
                    writer.write("\r\n");
                    writer.flush();
                    Log.i(TAG, "Frame Length: "+ frame.length);
                }
                writer.close();
                fos.close();
                Log.e(TAG, "New: " + savedFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        getAnimatedImage();

        Log.e(TAG, "Analysis Thread " + num + " Finish...");
    }
}
