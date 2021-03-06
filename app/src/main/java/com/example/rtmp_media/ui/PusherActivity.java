package com.example.rtmp_media.ui;

import android.Manifest;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.rtmp_media.R;
import com.example.rtmp_media.RtmpNative;
import com.example.rtmp_media.capture.AudioCapture;
import com.example.rtmp_media.capture.CameraCapture;
import com.example.rtmp_media.codec.AACEncode;
import com.example.rtmp_media.codec.H264Codec;
import com.example.rtmp_media.codec.H264DeCodec;
import com.example.rtmp_media.enums.VideoFrameType;
import com.example.rtmp_media.flv.FlvHelper;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PusherActivity extends AppCompatActivity {

    private static final String TAG = "camera";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray ORIENTATIONS_FACE = new SparseIntArray();


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
    private Button btnRecord;

    private ImageReader imageReader;
    private H264Codec h264Codec;
    private LinkedBlockingQueue<byte[]> videos = new LinkedBlockingQueue<byte[]>();
    private Size mSize = new Size(480, 640);
    private Size codeSize = new Size(640, 480);
    private AtomicBoolean isWriteFirst = new AtomicBoolean(false);

    private CameraCapture cameraCapture;
    private boolean isFront = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pusher);
        textureView = findViewById(R.id.textureView);
        textureView2 = findViewById(R.id.textureView2);
        Log.d("TAG", "onCreate: " + getWindowManager().getDefaultDisplay().getRotation());
        cameraCapture = new CameraCapture(this, isFront);
        cameraCapture.setCameraListener(new CameraCapture.CameraListener() {
            @Override
            public void open(int w, int h, int sensorOrientation) {
                int i = setCameraDisplayOrientation(isFront, sensorOrientation);
                Log.d(TAG, "onCreate: " + w + "   " + h + "    " + sensorOrientation+",modify angle :"+i);
                mSize = new Size(w, h);
                initEncode();
                createImage();
                configureTransform(textureView.getWidth(), textureView.getHeight());
            }

            @Override
            public void openSuccess() {

            }

            @Override
            public void error(int code) {

            }

            @Override
            public void onCameraDeviceClose() {

            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                RtmpNative.RtmpConnect("/mnt/sdcard/1.flv");
//            }
//        }).start();
        textureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RtmpNative.RtmpConnect1();
                        H264DeCodec codec = new H264DeCodec(new Surface(surface), codeSize.getWidth(), codeSize.getHeight());
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
                capturing();
            }
        });
    }

    /**
     * ??????yuv??????
     * @param isFront
     * @param sensor
     * @return
     */
    public int setCameraDisplayOrientation(
            boolean isFront, int sensor) {
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (isFront) {
            result = (sensor + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (sensor - degrees + 360) % 360;
        }
        return result;
    }

    private void initEncode() {
        h264Codec = new H264Codec(codeSize.getWidth(), codeSize.getHeight());
        h264Codec.setEncodeH264Listener(new H264Codec.EncodeH264Listener() {
            @Override
            public void onData(byte[] data, int type) {
                try {
                    if (type == VideoFrameType.SPS_PPS_I.value) {
                        byte[] head = h264Codec.getHead();
                        byte[] sps = new byte[h264Codec.getSpsLen() - 4];
                        byte[] pps = new byte[head.length - h264Codec.getSpsLen() - 4];

                        System.arraycopy(head, 4, sps, 0, sps.length);
                        System.arraycopy(head, h264Codec.getSpsLen() + 4, pps, 0, pps.length);

                        byte[] b1 = FlvHelper.warpFLVBodyOfVideoFirstData(1, 7, (byte) 0, sps, pps);
                        byte[] b2 = FlvHelper.warpFLVBodyOfFixAudioTag(true, 16);

                        RtmpNative.pushVideo(b1, 0, 0);
                        RtmpNative.pushAudio(b2, 0, 0);

                        byte[] video = FlvHelper.warpFLVBodyOfVideoTag(data, true);
                        RtmpNative.pushVideo(video, 0, 0);
                        isWriteFirst.set(true);
                    } else if (type == VideoFrameType.I.value) {
                        byte[] bytes = FlvHelper.warpFLVBodyOfVideoTag(data, true);
                        RtmpNative.pushVideo(bytes, 0, 0);
                    } else if (type == VideoFrameType.P.value) {
                        byte[] bytes = FlvHelper.warpFLVBodyOfVideoTag(data, false);
                        byte[] b = new byte[bytes.length - 4];
                        System.arraycopy(bytes, 4, b, 0, b.length);
                        RtmpNative.pushVideo(b, 0, 0);
                    }
                    videos.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void capturing() {
        try {
            cameraCapture.startCapture(CameraDevice.TEMPLATE_RECORD);
            AACEncode aacEncode = new AACEncode();
            aacEncode.setMediaCodecListener(new AACEncode.MediaCodecListener() {
                @Override
                public void codec(byte[] data) {
                    ByteBuffer buffer = ByteBuffer.allocate(data.length + 2);
                    buffer.put(FlvHelper.warpFLVBodyOfFixAudioTag(false, 16));
                    buffer.put(data);
                    RtmpNative.pushAudio(buffer.array(), 0, 0);
                }
            });

            AudioCapture audioCapture = new AudioCapture();
            audioCapture.setAudioCaptureListener(new AudioCapture.AudioCaptureListener() {
                @Override
                public void onCapture(byte[] data) {
                    if (!isWriteFirst.get()) {
                        return;
                    }
                    new Random().nextBytes(data);
                    aacEncode.encodePCMToAAC(data);
                }
            });
            audioCapture.capture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createImage() {
        HandlerThread backgroundThread = new HandlerThread("CameraCapture");
        backgroundThread.start();
        Handler cameraHandler = new Handler(backgroundThread.getLooper());
        imageReader = ImageReader.newInstance(480, 640, ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //????????????yuv_420????????????n21
                Image image = reader.acquireNextImage();
                int imageFormat = image.getFormat();

                Image.Plane yPlane = image.getPlanes()[0];
                Image.Plane uPlane = image.getPlanes()[1];
                Image.Plane vPlane = image.getPlanes()[2];

                ByteBuffer yBuffer = yPlane.getBuffer();
                ByteBuffer uBuffer = uPlane.getBuffer();
                ByteBuffer vBuffer = vPlane.getBuffer();

                int ySize = yBuffer.remaining();
                int uSize = uBuffer.remaining() / uPlane.getPixelStride();
                int vSize = vBuffer.remaining() / uPlane.getPixelStride();
//                Log.d(TAG, "onImageAvailable: " + ySize + "     " + uSize + "    " + uSize);
//                Log.d(TAG, "onImageAvailable: " + yPlane.getPixelStride() + "     " + uPlane.getPixelStride() + "    " + vPlane.getPixelStride());
//                Log.d(TAG, "onImageAvailable: " + yPlane.getRowStride() + "     " + uPlane.getRowStride() + "    " + vPlane.getRowStride());

                byte[] videoData = new byte[ySize + uSize + vSize];
                yBuffer.get(videoData, 0, ySize);
                uBuffer.get(videoData, ySize, uSize);
                vBuffer.get(videoData, ySize + uSize, vSize);

                //??????????????????????????????????????????width???height????????????
//                    byte[] data = YUVUtil.rotateYUVDegree270(videoData, image.getWidth(), image.getHeight());
//                byte[] data = YUVUtil.rotateNV21(videoData, image.getWidth(), image.getHeight(), 90);
//                    Log.d(TAG, "onImageAvailable: nv21 buffer size :" + data.length);
                h264Codec.encode(videoData);
                image.close();

            }
        }, cameraHandler);
        cameraCapture.setSurfaceOut(imageReader.getSurface());
    }

    /**
     * ??????????????????
     * @param viewWidth
     * @param viewHeight
     */
    private void configureTransform(int viewWidth, int viewHeight) {
//        //??????????????????
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
            if (isFront) {
                matrix.postRotate(90 * (rotation - 2), centerX, centerY);//90*(1/3-2)
            } else {
                matrix.postRotate(90*rotation, centerX, centerY);//90*(1/3-2)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        } else {
            //??????????????????
            if (isFront) {
                matrix.postScale(-1, 1, centerX, centerY);
            }
        }
        textureView.setTransform(matrix);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            Log.i(TAG, "onResume: textureView.isAvailable() true");
            cameraCapture.setInSurface(new Surface(textureView.getSurfaceTexture()));
            cameraCapture.setSize(textureView.getWidth(), textureView.getHeight());
            cameraCapture.openCamera();
        } else {
            Log.i(TAG, "onResume: textureView.isAvailable() false");
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }


    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable-->width???" + width + ",height???" + height);
            cameraCapture.setInSurface(new Surface(surface));
            cameraCapture.setSize(width, height);
            cameraCapture.openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged-->width???" + width + ",height???" + height);
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