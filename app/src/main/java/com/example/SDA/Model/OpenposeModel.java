package com.example.SDA.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod.NEAREST_NEIGHBOR;

import androidx.appcompat.resources.Compatibility;

public class OpenposeModel {
    private static final String MODEL_NAME = "openpose.tflite";
    private static final String TAG = "OpenposeModel";
    private Context context;
    private int modelInputBatch, modelInputWidth, modelInputHeight, modelInputChannel;
    private Model model;
    private TensorImage inputImage;
    private TensorBuffer outputBuffer;
    private boolean isInitialized = false;

    private long beforeTime;
    private long afterTime;
    private long diffTime;

    public OpenposeModel(Context context) {
        this.context = context;
    }

    private Model createGPUModel() throws IOException {
        Model.Options.Builder optionsBuilder = new Model.Options.Builder();
        CompatibilityList compatList = new CompatibilityList();

        if(compatList.isDelegateSupportedOnThisDevice()) {
            optionsBuilder.setDevice(Model.Device.GPU);
        }
        return Model.createModel(context, MODEL_NAME, optionsBuilder.build());
    }

    public void init() throws IOException {
        //model = Model.createModel(context, MODEL_NAME);
        model = createGPUModel();
        initModelShape();
        isInitialized = true;
    }

    private void initModelShape() {
        Tensor inputTensor = model.getInputTensor(0);
        int[] shape = inputTensor.shape();
        modelInputBatch = shape[0];
        modelInputHeight = shape[1];
        modelInputWidth = shape[2];
        modelInputChannel = shape[3];
        inputImage = new TensorImage(inputTensor.dataType());
        Log.e(TAG, "input shape : " + modelInputBatch + ", " + modelInputHeight + ", " + modelInputWidth + ", " + modelInputChannel + ", Type : " + inputTensor.dataType());

        Tensor outputTensor = model.getOutputTensor(11);
        Log.e(TAG, "output shape : " + outputTensor.shape()[0] + ", " + outputTensor.shape()[1] + ", " + outputTensor.shape()[2] + ", " + outputTensor.shape()[3] + ", Type : " + outputTensor.dataType());
        int[] temp = {1,46,46,38};
        outputBuffer = TensorBuffer.createFixedSize(temp, outputTensor.dataType());
    }

    private Bitmap convertBitmapToARGB8888(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888,true);
    }

    public Size getModelInputSize() {
        if(!isInitialized)
            return new Size(0, 0);
        return new Size(modelInputWidth, modelInputHeight);
    }

    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        if(bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            inputImage.load(convertBitmapToARGB8888(bitmap));
        } else {
            inputImage.load(bitmap);
        }

        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int numRotation = sensorOrientation / 90;

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                .add(new ResizeOp(modelInputWidth, modelInputHeight, NEAREST_NEIGHBOR))
                .add(new Rot90Op(numRotation))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();

        return imageProcessor.process(inputImage);
    }

    public void classify(Bitmap image, int sensorOrientation) {
        inputImage = loadImage(image, sensorOrientation);

        Object[] inputs = new Object[]{inputImage.getBuffer()};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());

        beforeTime = System.currentTimeMillis();
        model.run(inputs, outputs);
        afterTime = System.currentTimeMillis();
        diffTime = afterTime - beforeTime;
        Log.i(TAG, "시간 : " + diffTime/1000);
    }

    public void classify(Bitmap image) {
        classify(image, 0);
        model.close();
    }

    public void finish() {
        if(model != null) {
            model.close();
        }
    }
}
