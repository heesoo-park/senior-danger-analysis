package com.example.SDA.Model;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class My3DPoseModel {
    private static final String MODEL_NAME = "3dpose.tflite";
    private static final String TAG = "My3DPoseModel";
    private Model model;
    private Context context;
    private TensorBuffer outputBuffer;
    private float[][] _3DPoseInput;

    private long beforeTime;
    private long afterTime;
    private long diffTime;

    public My3DPoseModel(Context context) {
        this.context = context;
    }

    private Model createGPUModel(int nThreads) throws IOException {
        Model.Options.Builder optionsBuilder = new Model.Options.Builder();
        optionsBuilder.setNumThreads(nThreads);
        CompatibilityList compatList = new CompatibilityList();

        //model = createGPUModel(4);
        if(compatList.isDelegateSupportedOnThisDevice()) {
            optionsBuilder.setDevice(Model.Device.GPU);
        }
        return Model.createModel(context, MODEL_NAME, optionsBuilder.build());
    }

    public void init() throws IOException {
        model = Model.createModel(context, MODEL_NAME);
        initModelShape();
    }

    private void initModelShape() {
        Tensor outputTensor = model.getOutputTensor(0);
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType());
    }

    private float[][] convertPoseTo3DPoseBaselineInput(Pose pose) {
        float[][] _3DPoseInput = new float[16][2];
        PointF leftShoulderPoint = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition();
        PointF rightShoulderPoint = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition();
        PointF leftElbowPoint = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition();
        PointF rightElbowPoint = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition();
        PointF leftWristPoint = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition();
        PointF rightWristPoint = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition();
        PointF leftHipPoint = pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition();
        PointF rightHipPoint = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition();
        PointF leftKneePoint = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition();
        PointF rightKneePoint = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition();
        PointF leftAnklePoint = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition();
        PointF rightAnklePoint = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition();
        PointF nosePoint = pose.getPoseLandmark(PoseLandmark.NOSE).getPosition();
        PointF spinePoint = new PointF((rightShoulderPoint.x + leftShoulderPoint.x)/2, (rightShoulderPoint.y + leftShoulderPoint.y)/2);
        PointF thoraxPoint = new PointF(2 * spinePoint.x - (nosePoint.x + spinePoint.x) / 2, 2 * spinePoint.y - (nosePoint.y + spinePoint.y) / 2);

        _3DPoseInput[0] = new float[]{(rightHipPoint.x + leftHipPoint.x)/2,(rightHipPoint.y + leftHipPoint.y)/2};
        _3DPoseInput[1] = new float[]{rightHipPoint.x, rightHipPoint.y};
        _3DPoseInput[2] = new float[]{rightKneePoint.x, rightKneePoint.y};
        _3DPoseInput[3] = new float[]{rightAnklePoint.x, rightAnklePoint.y};
        _3DPoseInput[4] = new float[]{leftHipPoint.x, leftHipPoint.y};
        _3DPoseInput[5] = new float[]{leftKneePoint.x, leftKneePoint.y};
        _3DPoseInput[6] = new float[]{leftAnklePoint.x, leftAnklePoint.y};
        _3DPoseInput[7] = new float[]{spinePoint.x, spinePoint.y};
        _3DPoseInput[8] = new float[]{thoraxPoint.x, thoraxPoint.y};
        _3DPoseInput[9] = new float[]{nosePoint.x, nosePoint.y};
        _3DPoseInput[10] = new float[]{leftShoulderPoint.x, leftShoulderPoint.y};
        _3DPoseInput[11] = new float[]{leftElbowPoint.x, leftElbowPoint.y};
        _3DPoseInput[12] = new float[]{leftWristPoint.x, leftWristPoint.y};
        _3DPoseInput[13] = new float[]{rightShoulderPoint.x, rightShoulderPoint.y};
        _3DPoseInput[14] = new float[]{rightElbowPoint.x, rightElbowPoint.y};
        _3DPoseInput[15] = new float[]{rightWristPoint.x, rightWristPoint.y};

        return _3DPoseInput;
    }

    public float[] run(Pose pose) {
        // 3D pose baseline 시간 측정
        beforeTime = System.currentTimeMillis();

        _3DPoseInput = convertPoseTo3DPoseBaselineInput(pose);
        Object[] inputs = new Object[]{_3DPoseInput};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputBuffer.getBuffer().rewind());
        model.run(inputs, outputs);

        // 측정 종료
        afterTime = System.currentTimeMillis();
        diffTime = afterTime - beforeTime;
        //Log.e(TAG, "" + Arrays.toString(outputBuffer.getFloatArray()));
        Log.e(TAG, "3D Pose Baseline Mapping! Time : " + diffTime + "ms");
        return outputBuffer.getFloatArray();
    }

    public void finish() {
        if(model != null) {
            model.close();
        }
    }
}