package com.example.rtmp_media.enums;

public enum VideoFrameType {

    UN_KNOWN(-1),SPS_PPS(0),SPS_PPS_I(1), I(2), P(3), B(4);

    public int value;

    VideoFrameType(int i) {
        value = i;
    }
}
