package com.example.rtmp_media.capture;

import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraCapture {

    private static final String TAG = "CameraCapture";
    private Context mContext;

    private HandlerThread backgroundThread;
    private Handler cameraHandler;
    private Executor executor = Executors.newCachedThreadPool();

    private String cameraBackId, cameraFrontId, extensionCameraId;
    private String userCameraId;
    private boolean isFront;
    private Range<Integer>[] availableRanges;
    private Boolean flashAvailable;
    private CameraDevice cameraDevice;
    //用于输出数据
    private Surface out;
    //用于输入画面
    private Surface in;
    private CameraCaptureSession cameraCaptureSession;
    private static Size DEFAULT_SIZE = new Size(480, 640);
    private Size size = DEFAULT_SIZE;
    private CameraListener cameraListener;


    public CameraCapture(Context mContext, boolean isFront) {
        this.mContext = mContext;
        this.isFront = isFront;
        backgroundThread = new HandlerThread("CameraCapture");
        backgroundThread.start();
        cameraHandler = new Handler(backgroundThread.getLooper());
    }

    public void setSurfaceOut(Surface out) {
        this.out = out;
    }

    public void setInSurface(Surface in) {
        this.in = in;
    }

    public void setSize(int w, int h) {
        size = new Size(w, h);
    }

    public boolean openCamera() {
        if (mContext == null) {
            Log.e(TAG, "openCamera: context is null");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "openCamera: camera permission not allowed");
            return false;
        }
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            CameraCharacteristics cameraCharacteristics = null;
            for (String cameraId : cameraIdList) {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer integer = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (integer == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraBackId = cameraId;
                    Log.i(TAG, "openCamera-->cameraFrontId：" + cameraBackId);
                } else if (integer == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraFrontId = cameraId;
                    Log.i(TAG, "openCamera-->cameraBackId：" + cameraFrontId);
                } else {
                    extensionCameraId = cameraId;
                }
            }
            if (isFront) {
                userCameraId = cameraFrontId;
            } else {
                userCameraId = cameraBackId;
            }
            cameraCharacteristics = cameraManager.getCameraCharacteristics(userCameraId);
            //可用帧数
            availableRanges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            //闪光灯可用性
            flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (!streamConfigurationMap.isOutputSupportedFor(ImageFormat.YUV_420_888)) {
                return false;
            }

            Size size = chooseOptimalSize(streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888));

            Integer integer = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (cameraListener != null) {
                cameraListener.open(size.getWidth(), size.getHeight(), integer);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cameraManager.openCamera(userCameraId, executor, stateCallback);
            } else {
                cameraManager.openCamera(userCameraId, stateCallback, cameraHandler);
            }
            Log.i(TAG, "openCamera: camera opened successfully");
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Size chooseOptimalSize(Size[] outputSizes) {
        List<Size> sizes = new ArrayList<>();

        for (Size s : outputSizes) {
            //如果有相同宽高直接返回
            if (s.getWidth() == size.getWidth() && s.getHeight() == size.getHeight()) {
                return s;
            }
            //否则 寻求相同比例的宽高
            if (s.getWidth() == s.getHeight() * size.getWidth() / size.getHeight()) {
                sizes.add(s);
            }
        }
        if (sizes.size() == 0) {
            return DEFAULT_SIZE;
        }
        //返回最小宽高
        return Collections.min(sizes, new CompareSizesByArea());
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void createCaptureSession() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ArrayList<OutputConfiguration> outputConfigurations = new ArrayList<>();
            outputConfigurations.add(new OutputConfiguration(in));
            outputConfigurations.add(new OutputConfiguration(out));
            SessionConfiguration configuration = new SessionConfiguration(SESSION_REGULAR, outputConfigurations, executor, cameraCaptureSessionCallback);
            cameraDevice.createCaptureSession(configuration);
        } else {
            cameraDevice.createCaptureSession(Arrays.asList(in,out), cameraCaptureSessionCallback, cameraHandler);
        }
    }

    public Handler getCameraHandler() {
        return cameraHandler;
    }

    public void startCapture(int templateType) {
        if (cameraDevice == null) {
            return;
        }
        CaptureRequest.Builder request = null;
        try {
            request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            request.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(15, 15));
            request.addTarget(in);
            request.addTarget(out);
            //当前输出的视频为传感器的方向，需要进行调整
            cameraCaptureSession.setRepeatingRequest(request.build(), captureCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takePreview() {
        if (cameraCaptureSession == null) {
            return;
        }
        try {
            CaptureRequest.Builder captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //输出画面
            captureRequest.addTarget(in);
            //todo 自动对焦，打开闪光灯
            cameraCaptureSession.setRepeatingRequest(captureRequest.build(), captureCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void switchCamera(boolean isFront) {
        if (this.isFront = isFront) {
            return;
        }
        this.isFront = isFront;
        release();
        openCamera();

    }

    public void setCameraListener(CameraListener cameraListener) {
        this.cameraListener = cameraListener;
    }

    public void release() {
        try {
            if (cameraCaptureSession != null) {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (backgroundThread != null) {
                backgroundThread.quitSafely();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    };


    private CameraCaptureSession.StateCallback cameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "cameraCaptureSessionCallback: onConfigured");
            cameraCaptureSession = session;
            takePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "cameraCaptureSessionCallback: onConfigureFailed");
            if (cameraCaptureSession != null) {
                try {
                    cameraCaptureSession.stopRepeating();
                    cameraCaptureSession.close();
                    cameraCaptureSession = null;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            try {
                cameraDevice = camera;
                createCaptureSession();
                Log.i(TAG, "CameraDevice.StateCallback: onOpened");
                if (cameraListener != null) {
                    cameraListener.openSuccess();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(TAG, "CameraDevice.StateCallback: onDisconnected");
            if (cameraDevice != null) {
                cameraDevice.close();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.i(TAG, "CameraDevice.StateCallback: onError " + error);
            if (cameraListener != null) {
                cameraListener.error(error);
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            Log.i(TAG, "CameraDevice.StateCallback: onClosed");
            cameraDevice = null;
            if (cameraListener != null) {
                cameraListener.onCameraDeviceClose();
            }
        }
    };


    public interface CameraListener {

        void open(int w, int h, int sensorOrientation);

        void openSuccess();

        void error(int code);

        void onCameraDeviceClose();
    }
}
