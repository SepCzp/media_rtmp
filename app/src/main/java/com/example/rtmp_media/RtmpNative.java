package com.example.rtmp_media;

public class RtmpNative {

    static {
        System.loadLibrary("rtmp_media");
    }

    public static native void RtmpConnect(String path);

    public static native void RtmpConnect1();

    public static native void pushVideo(byte[] data,int type,int timestamp);

    public static native void pushAudio(byte[] data,int type,int timestamp);

}
