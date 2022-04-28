package com.example.MyApplication.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.MyApplication.MainActivity;
import com.example.MyApplication.R;
import com.example.MyApplication.Thread.CaptureThread;
import com.example.MyApplication.Thread.ConsumerThread;

import java.util.LinkedList;
import java.util.Queue;

public class ForegroundService extends Service {
    private BackgroundTask task;

    private static final String TAG = "ForegroundService";
    private Queue<byte[]> queue = new LinkedList<>();
    private CaptureThread captureThread;
    private ConsumerThread consumerThread;
    private static boolean running = true;
    int value = 0;

    public ForegroundService() { }

    @Override
    public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        startThreads();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestory");
        captureThread.interrupt();
        consumerThread.interrupt();
        captureThread.onPause();
        super.onDestroy();
        task.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isServiceRunning(getApplication())) {
            startThreads();
        }

        task = new BackgroundTask();
        task.execute();

        initializeNotification(); // generate foreground
        return START_NOT_STICKY;
    }

    public static boolean isRunning() {
        return running;
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(rsi.service.getClassName()))
                return true;
        }
        return false;
    }

    public void initializeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("현재 SDA 어플리케이션이 동작 중입니다.");
        style.setBigContentTitle(null);
        style.setSummaryText("카메라 동작 중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "포그라운드 서비스", NotificationManager.IMPORTANCE_NONE));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void startThreads() {
        consumerThread = new ConsumerThread(queue);
        consumerThread.start();
        captureThread = new CaptureThread(queue);
        captureThread.start();
    }

    class BackgroundTask extends AsyncTask<Integer, String, Integer> {
        String result = "";
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        @Override
        protected Integer doInBackground(Integer... values) {
            while(isCancelled() == false) {
                try {
                    Log.e(TAG, value + "s");
                    Thread.sleep(10000);
                    value++;
                } catch (InterruptedException ex) { }
            }
            return value;
        }

        //상태확인
        @Override
        protected void onProgressUpdate(String... String) {
            Log.e(TAG, "onProgressUpdate() 업데이트");
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Log.e(TAG, "onPostExecute()");
            value = 0;
        }

        @Override
        protected void onCancelled() {
            value = 0; //정지로 초기화
        }
    }
}
