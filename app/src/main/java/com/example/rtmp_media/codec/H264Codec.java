package com.example.rtmp_media.codec;

import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.example.rtmp_media.enums.VideoFrameType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class H264Codec {

    private static final String TAG = "H264Codec";
    private static final long TIMEOUTUS = 1000 * 100;
    private EncodeH264Listener encodeH264Listener;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private int frameNumber = 15;
    private long frameCount = 0;
    private VideoFrameType videoFrameType = VideoFrameType.UN_KNOWN;
    private Context context;
    private int remaining;

    public H264Codec(int width, int height) {
        try {
            frameCount = 0;
            mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            //接收的数据类型为yuv任何格式
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 10);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameNumber);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);//关键帧i帧间隔

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(mediaFormat, null, null, CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private byte[] head;//pps和sps,只有开头有

    public void encode(byte[] bytes) {
        if (mediaCodec == null) {
            return;
        }
        long start = System.currentTimeMillis();
        int inputIndex = mediaCodec.dequeueInputBuffer(TIMEOUTUS);
        if (inputIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputIndex);
            Log.i(TAG, "encode: byte buffer capacity :" + inputBuffer.capacity());
            long pts = computePresentationTime(frameCount);
            inputBuffer.clear();
            inputBuffer.position(0);
            inputBuffer.put(bytes);
            mediaCodec.queueInputBuffer(inputIndex, 0, bytes.length, pts, 0);
            frameCount += 1;
            Log.i(TAG, "queueInputBuffer data");
        } else {
            Log.w(TAG, "inputIndex < = 0");
        }

        byte[] outBytes = null;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUTUS);
        while (outputBufferIndex >= 0) {
            ByteBuffer buffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            outBytes = new byte[bufferInfo.size];
            buffer.get(outBytes);
            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                //保存pps和sps 只有刚开始第一帧里面有
                Log.d(TAG, "encode: MediaCodec.BUFFER_FLAG_CODEC_CONFIG");
                head = new byte[bufferInfo.size];
                System.arraycopy(outBytes, 0, head, 0, outBytes.length);
                videoFrameType = VideoFrameType.SPS_PPS;
                //
                ByteBuffer spsB = mediaCodec.getOutputFormat().getByteBuffer("csd-0");
                remaining = spsB.remaining();
//                ByteBuffer ppsB = mediaCodec.getOutputFormat().getByteBuffer("csd-1");
            } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                //关键帧- 都要加上pps和sps  所以此处得到的是pps+sps+I帧
                if (videoFrameType.value == VideoFrameType.SPS_PPS.value&&head!=null) {
                    byte[] keyData = new byte[bufferInfo.size + head.length];
                    System.arraycopy(head, 0, keyData, 0, head.length);
                    System.arraycopy(outBytes, 0, keyData, head.length, outBytes.length);
                    outBytes = keyData;
                    videoFrameType = VideoFrameType.SPS_PPS_I;
                } else {
                    videoFrameType = VideoFrameType.I;
                }
                Log.d(TAG, "encode: MediaCodec.BUFFER_FLAG_KEY_FRAME");
            } else {
                //非关键帧--->此处得到的是P帧
                Log.d(TAG, "encode: video p frame");
                videoFrameType = VideoFrameType.P;
            }
            if (encodeH264Listener != null) {
                encodeH264Listener.onData(outBytes, videoFrameType.value);
            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUTUS);
        }
        Log.d(TAG, "encode take time : " + (System.currentTimeMillis() - start));
    }

    public int getSpsLen() {
        return remaining;
    }

    public byte[] getHead() {
        return head;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return frameIndex * 1000000 / frameNumber;//转微秒
    }

    public void setEncodeH264Listener(EncodeH264Listener encodeH264Listener) {
        this.encodeH264Listener = encodeH264Listener;
    }

    public interface EncodeH264Listener {
        void onData(byte[] data, int type);
    }
}
