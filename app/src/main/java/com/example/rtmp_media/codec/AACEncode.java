package com.example.rtmp_media.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AACEncode {

    private MediaCodec mediaCodec;
    private long frameTime;
    private MediaCodecListener mediaCodecListener;

    public AACEncode() {
        try {
//            MediaCodecInfo mediaCodecInfo = MediaCodecHelper.selectCodec(MediaFormat.MIMETYPE_AUDIO_AAC);
//            if (mediaCodecInfo == null) {
//                Log.e("TAG", "AACEncode: this mediacodec is no support aac audio encode");
//                return;
//            }
            MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,8192);

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
            frameTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void encodePCMToAAC(byte[] data) {
        if (data == null || mediaCodec == null) {
            Log.d("TAG", "run: null");
            return;
        }
        int len = data.length;
        int inIndex = mediaCodec.dequeueInputBuffer(1000 * 1000);
        if (inIndex >= 0) {
            ByteBuffer buffer = mediaCodec.getInputBuffer(inIndex);
            buffer.clear();
            buffer.position(0);
            buffer.limit(len);
            buffer.put(data);
            mediaCodec.queueInputBuffer(inIndex, 0, len, calculateTime(), 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000 * 1000);
        while (outIndex >= 0) {
            byte[] aac = new byte[bufferInfo.size];
            ByteBuffer buffer = mediaCodec.getOutputBuffer(outIndex);
            buffer.position(bufferInfo.offset);
            buffer.limit(bufferInfo.offset + bufferInfo.size);
            buffer.get(aac);
            buffer.clear();
            if (mediaCodecListener != null) {
                mediaCodecListener.codec(aac);
            }
            mediaCodec.releaseOutputBuffer(outIndex, false);
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000 * 1000);
        }
    }

    private long calculateTime() {
        long millis = System.currentTimeMillis();
        long time = millis - frameTime;
        frameTime = millis;
        return time;
    }

    public void setMediaCodecListener(MediaCodecListener mediaCodecListener) {
        this.mediaCodecListener = mediaCodecListener;
    }

    public interface MediaCodecListener {
        void codec(byte[] data);
    }

}
