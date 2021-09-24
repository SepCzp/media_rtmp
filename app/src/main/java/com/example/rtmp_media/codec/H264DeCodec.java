package com.example.rtmp_media.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H264DeCodec {

    private static final long TIMEOUTUS = 1000 * 100;
    private MediaCodec mediaCodec;
    private Surface surface;
    private int frames = 15;
    private long totalCount;

    public H264DeCodec(Surface surface, int w, int h) {
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, h, w);
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(mediaFormat, surface, null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode(byte[] data) {
        if (mediaCodec == null) {
            return;
        }
        long start = System.currentTimeMillis();

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUTUS);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(data);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, computePresentationTime(totalCount), 0);
            totalCount++;
        }

        byte[] render;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUTUS);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            render = new byte[outputBuffer.remaining()];
            outputBuffer.get(render);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUTUS);
        }
        Log.d("decode", "decode take time : " + (System.currentTimeMillis() - start));
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return frameIndex * 1000000 / frames;//转微秒
    }

}
