package com.example.rtmp_media;

public class RtmpNative {

    static {
        System.loadLibrary("rtmp_media");
    }

    public static native void RtmpConnect(String path);

}
