package com.example.SDA.Model;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class STGCNModel {
    public static float[][][] input = new float[3][30][17];
    public static int pushCount;

    private static final String MODEL_NAME = "mystgcn0530.tflite";
    private static final String TAG = "STGCNModel";
    private Model model;
    private Context context;
    private TensorBuffer outputBuffer;

    private long beforeTime;
    private long afterTime;
    private long diffTime;
    private int result;

    public STGCNModel(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        pushCount = 0;
        model = Model.createModel(context, MODEL_NAME);
        initModelShape();
    }

    public static void clearBuffer() {
        input = new float[3][30][17];
        pushCount = 0;
    }

    private void initModelShape() {
        int outputShape[] = {1, 2};
        Tensor outputTensor = model.getOutputTensor(0);
        outputBuffer = TensorBuffer.createFixedSize(outputShape, outputTensor.dataType());
    }

    public void push(float[] my3DPoseOutput) {
        for (int i = 0; i < 51; i++) {
            input[i%3][pushCount][i/3] = my3DPoseOutput[i];
        }
        pushCount++;
    }

    public boolean isRunnable() {
        return pushCount == 30;
    }

    public int run() {
        beforeTime = System.currentTimeMillis();
        Object[] inputs = new Object[]{input};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        model.run(inputs, outputs);
        afterTime = System.currentTimeMillis();
        diffTime = afterTime - beforeTime;

        float[] resultArray = outputBuffer.getFloatArray();
        pushCount = 0;

        result = resultArray[0] > resultArray[1] ? 0 : 1;
        Log.e(TAG, "ST-GCN : " + Arrays.toString(resultArray) + ", " + result + ", Time : " + diffTime + "ms");
        return result;
    }

    public void finish() {
        if(model != null) {
            model.close();
        }
    }
}