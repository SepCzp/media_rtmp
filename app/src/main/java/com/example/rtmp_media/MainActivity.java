package com.example.rtmp_media;

import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.rtmp_media.codec.H264Codec;
import com.example.rtmp_media.codec.H264DeCodec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "camera";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray ORIENTATIONS_FACE = new SparseIntArray();
    private static final String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
    };

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

        ORIENTATIONS_FACE.append(Surface.ROTATION_0, 270);
        ORIENTATIONS_FACE.append(Surface.ROTATION_90, 0);
        ORIENTATIONS_FACE.append(Surface.ROTATION_180, 90);
        ORIENTATIONS_FACE.append(Surface.ROTATION_270, 180);
    }

    private TextureView textureView;
    private TextureView textureView2;
    private SurfaceTexture previewSurfaceTexture;
    private Button btnRecord;

    private Handler workHandler;
    private String cameraFrontId;
    private String cameraBackId;
    private String extensionCameraId;
    private int mSensorOrientation;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder previewCaptureRequestBuilder;
    private ImageReader imageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private HandlerThread handlerThread;
    private H264Codec h264Codec;
    private LinkedBlockingQueue<byte[]> videos = new LinkedBlockingQueue<byte[]>();
    private Size mSize = new Size(640, 480);

    private Executor executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, permissions, 0x1);
        textureView = findViewById(R.id.textureView);
        textureView2 = findViewById(R.id.textureView2);
        textureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        H264DeCodec codec = new H264DeCodec(new Surface(surface), mSize.getWidth(), mSize.getHeight());
                        while (true) {
                            try {
                                byte[] take = videos.take();
                                codec.decode(take);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });

        btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mCameraDevice == null) {
                        return;
                    }
                    mCameraCaptureSession.stopRepeating();

                    CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                    request.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(15, 15));
                    request.addTarget(new Surface(previewSurfaceTexture));
                    request.addTarget(imageReader.getSurface());
                    //当前输出的视频为传感器的方向，需要进行调整
                    mCameraCaptureSession.setRepeatingRequest(request.build(), captureCallback, workHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        cameraHandleThread();
    }

    private void configureTransform(int viewWidth, int viewHeight) {
//        //屏幕旋转角度
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mSize.getWidth(), mSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mSize.getHeight(),
                    (float) viewWidth / mSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);//90*(1/3-2)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        } else {
            //解决预览镜像
            matrix.postScale(-1, 1, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            Log.i(TAG, "onResume: textureView.isAvailable() true");
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            Log.i(TAG, "onResume: textureView.isAvailable() false");
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    @SuppressLint("MissingPermission")
    private void openCamera(int w, int h) {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraBackId);
            Range<Integer>[] ranges = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            for (Range<Integer> r : ranges) {
                Log.i(TAG, "camera available frame number :" + r.getLower() + " " + r.getUpper());
            }

            //获取屏幕旋转方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.i(TAG, "openCamera: screen rotation :" + rotation);
            //传感器方向
            mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Log.i(TAG, "openCamera: sensor orientation :" + mSensorOrientation);

            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap.isOutputSupportedFor(ImageFormat.YUV_420_888)) {
                Size[] outputSizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888);
                for (Size size : outputSizes) {
                    Log.i(TAG, "openCamera: yuv420 w*h:" + size.getWidth() + "*" + size.getHeight());
                }
            } else {
                Log.w(TAG, "openCamera: camera is not support yuv420");
            }

            configureTransform(w, h);
            cameraManager.openCamera(cameraFrontId, stateCallback, workHandler);

            imageReader = ImageReader.newInstance(mSize.getWidth(), mSize.getHeight(), ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    //获取的是yuv_420，默认是n21
                    Image image = reader.acquireNextImage();

                    Image.Plane yPlane = image.getPlanes()[0];
                    Image.Plane uPlane = image.getPlanes()[1];
                    Image.Plane vPlane = image.getPlanes()[2];

                    ByteBuffer yBuffer = yPlane.getBuffer();
                    ByteBuffer uBuffer = uPlane.getBuffer();
                    ByteBuffer vBuffer = vPlane.getBuffer();

                    int ySize = yBuffer.remaining();
                    int uSize = uBuffer.remaining() / uPlane.getPixelStride();
                    int vSize = vBuffer.remaining() / uPlane.getPixelStride();

                    byte[] videoData = new byte[ySize + uSize + vSize];
                    yBuffer.get(videoData, 0, ySize);
                    uBuffer.get(videoData, ySize, uSize);
                    vBuffer.get(videoData, ySize + uSize, vSize);
                    Log.i(TAG, "onImageAvailable: buffer size " + videoData.length);
                    //进行数据旋转后需要对编解码的width和height进行修改
                    byte[] data = YUVUtil.rotateYUVDegree90(videoData, image.getWidth(), image.getHeight());
                    h264Codec.encode(data);
                    image.close();
                }
            }, workHandler);

            h264Codec = new H264Codec(mSize.getWidth(), mSize.getHeight());
            h264Codec.setContext(this);
            h264Codec.setEncodeH264Listener(new H264Codec.EncodeH264Listener() {
                @Override
                public void onData(byte[] data, int type) {
                    try {
                        videos.put(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takePreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            Surface surface = new Surface(surfaceTexture);
            previewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //todo 设置自动对焦曝光等
            previewCaptureRequestBuilder.addTarget(surface);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ArrayList<OutputConfiguration> outputConfigurations = new ArrayList<>();
                outputConfigurations.add(new OutputConfiguration(surface));
                outputConfigurations.add(new OutputConfiguration(imageReader.getSurface()));
                SessionConfiguration configuration = new SessionConfiguration(SESSION_REGULAR, outputConfigurations, executor, previewSessionStateCallback);
                mCameraDevice.createCaptureSession(configuration);
            } else {
                mCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), previewSessionStateCallback, workHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void cameraHandleThread() {
        handlerThread = new HandlerThread("camera");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper());
    }

    private void exitWorkHandler() {
        workHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();
    }

    private void closeCameraCaptureSession() {
        try {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
                Log.i(TAG, "closeCameraCaptureSession");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCameraDevice() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCameraCaptureSession();
        closeCameraDevice();
        exitWorkHandler();
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
//            Log.i(TAG, "onCaptureStarted-->timestamp：" + timestamp + ",frameNumber：" + frameNumber);
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
            Log.w(TAG, "onCaptureFailed: " + failure.getReason());
        }
    };

    private CameraCaptureSession.StateCallback previewSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                Log.i(TAG, "onConfigured: ");
                if (mCameraDevice == null) {
                    return;
                }
                mCameraCaptureSession = session;
                mCameraCaptureSession.setRepeatingRequest(previewCaptureRequestBuilder.build(), captureCallback, workHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "onConfigureFailed: ");
        }
    };


    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened: ");
            mCameraDevice = camera;
            takePreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "onDisconnected: ");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "onError: ");
            camera.close();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            mCameraDevice = null;
        }
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable-->width：" + width + ",height：" + height);
            previewSurfaceTexture = surface;
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged-->width：" + width + ",height：" + height);
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            Log.i(TAG, "onSurfaceTextureDestroyed: ");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };
}