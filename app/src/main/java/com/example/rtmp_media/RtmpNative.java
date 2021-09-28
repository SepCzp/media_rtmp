package com.example.rtmp_media;

import android.util.Log;

public class RtmpNative {

    static {
        System.loadLibrary("rtmp_media");
    }

    public static native void RtmpConnect(String path);

    public static native void RtmpConnect1();

    public static native void pushVideo(byte[] data, int type, int timestamp);

    public static native void pushAudio(byte[] data, int type, int timestamp);

    /*
     * 开始连接时回调
     * */
    public void onConnect() {
    }

    /**
     * 连接成功
     */
    public void onConnectSuccess() {
        Log.d("asdasdasd", Thread.currentThread().getName());
    }

    /**
     * 连接失败
     */
    public void onError(int code) {
    }

}
