package com.example.rtmp_media.capture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.util.Log;

public class AudioCapture {

    //采样率
    private int sampleRate = 44100;
    //声道数
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    //采样位数
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    //最小buffersize
    private int minBufferSize;
    //状态
    private int state = AudioRecord.STATE_UNINITIALIZED;
    private int recordState = AudioRecord.RECORDSTATE_STOPPED;
    private AudioCaptureListener audioCaptureListener;

    public AudioCapture(int sampleRate, int channelConfig, int audioFormat) {
        this.sampleRate = sampleRate;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
    }

    public AudioCapture() {

    }

    private void init() {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize);
        state = audioRecord.getState();
    }

    public void capture() {
        if (state == AudioRecord.STATE_UNINITIALIZED) {
            init();
        }

        audioRecord.startRecording();
        recordState = AudioRecord.RECORDSTATE_RECORDING;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[minBufferSize];
                while (recordState == AudioRecord.RECORDSTATE_RECORDING) {
                    int read = audioRecord.read(buffer, 0, minBufferSize);
                    if (audioCaptureListener != null) {
                        audioCaptureListener.onCapture(buffer);
                    }
                }
            }
        }).start();
    }

    public void setAudioCaptureListener(AudioCaptureListener audioCaptureListener) {
        this.audioCaptureListener = audioCaptureListener;
    }

    public void release() {
        if (recordState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
            recordState = AudioRecord.RECORDSTATE_STOPPED;
        }
        if (state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.release();
            state = AudioRecord.STATE_UNINITIALIZED;
        }
    }

    public interface AudioCaptureListener {
        void onCapture(byte[] data);
    }
}
