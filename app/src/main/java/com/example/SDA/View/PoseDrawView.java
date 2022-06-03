package com.example.SDA.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.SDA.Service.CameraService;
import com.google.mlkit.vision.pose.Pose;

import java.util.Arrays;

public class PoseDrawView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder holder;
    DrawThread drawThread;

    public PoseDrawView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        drawThread = new DrawThread(holder);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!drawThread.isAlive())
            drawThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    class DrawThread extends Thread {
        private Canvas canvas;
        private SurfaceHolder mHolder;
        private Paint paint;
        private PointF[] points;

        public DrawThread(SurfaceHolder holder) {
            mHolder = holder;
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2);
        }

        public void run() {
            while (true) {
                if (CameraService.drawQueue != null && CameraService.drawQueue.isEmpty()) {
                    continue;
                }

                canvas = holder.lockCanvas();

                synchronized (mHolder) {
                    drawPose();
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

        private void drawPose() {
            if (canvas == null) {
                return;
            }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(0xFF222222);

            Pose pose = CameraService.drawQueue.poll();
            points = new PointF[33];
            if (pose == null)
                return;
            float INF = 10000;
            float xMax = -INF, xMin = INF, yMax = -INF, yMin = INF;

            for (int i = 0; i < 33; i++) {
                PointF point = pose.getPoseLandmark(i).getPosition();
                points[i] = new PointF(point.x, 320 - point.y);

                if (i >= 13 && i <= 22)
                    continue;
                xMax = Math.max(xMax, point.x);
                xMin = Math.min(xMin, point.x);
                yMax = Math.max(yMax, 320 - point.y);
                yMin = Math.min(yMin, 320 - point.y);
            }

            //Log.e("", "" + Arrays.toString(points));
            // draw bounding box
            paint.setColor(Color.BLUE);
            canvas.drawRect(xMin, yMin, xMax, yMax, paint);

            // draw skeleton points
            paint.setColor(Color.RED);
            for (int i = 0; i < 33; i++) {
                PointF point = points[i];
                canvas.drawCircle(point.x, point.y, 5, paint);
            }

            // draw skeleton lines
            drawLine(0,1);
            drawLine(1,2);
            drawLine(2,3);
            drawLine(3,7);
            drawLine(0,4);
            drawLine(4,5);
            drawLine(5,6);
            drawLine(6,8);
            drawLine(9,10);
            drawLine(11,12);
            drawLine(11,13);
            drawLine(13,15);
            drawLine(15,21);
            drawLine(15,17);
            drawLine(17,19);
            drawLine(19,15);
            drawLine(12,14);
            drawLine(14,16);
            drawLine(16,22);
            drawLine(16,20);
            drawLine(16,18);
            drawLine(18,20);
            drawLine(12,24);
            drawLine(11,23);
            drawLine(25,23);
            drawLine(24,26);
            drawLine(26,28);
            drawLine(28,30);
            drawLine(30,32);
            drawLine(32,28);
            drawLine(25,27);
            drawLine(29,27);
            drawLine(29,31);
            drawLine(27,31);
            drawLine(23,24);
        }

        private void drawLine(int a, int b) {
            canvas.drawLine(points[a].x, points[a].y, points[b].x, points[b].y, paint);
        }
    }
}