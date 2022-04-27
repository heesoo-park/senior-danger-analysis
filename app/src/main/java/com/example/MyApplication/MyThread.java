package com.example.MyApplication;

import android.util.Log;

public class MyThread extends Thread{
    private static final String TAG = "MyThread";
    int num;

    public MyThread(int num) {
        this.num = num;
    }

    @Override
    public void run() {
        Log.e(TAG, this.num + " thread start");
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        Log.e(TAG, this.num + " thread end");
    }
}
