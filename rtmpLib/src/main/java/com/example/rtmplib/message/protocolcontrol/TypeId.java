package com.example.rtmplib.message.protocolcontrol;

public class TypeId {
    /**PROTOCOL Control Message*/
    public static final int TYPE_ID_SET_CHUNK_SIZE = 1;
    public static final int TYPE_ID_ABORT_MESSAGE = 2;
    public static final int TYPE_ID_ACKNOWLEDGEMENT = 3;
    public static final int TYPE_ID_USER_CONTROL_MESSAGE = 4;
    public static final int TYPE_ID_WINDOW_ACKNOWLEDGEMENT_SIZE = 5;
    public static final int TYPE_ID_SET_PEER_BANDWIDTH = 6;

    /**for server use*/
    public static final int TYPE_ID_EDGE_ORIGIN_SERVER = 7;

    /**AV Message*/
    public static final int TYPE_ID_AUDIO_MESSAGE = 8;
    public static final int TYPE_ID_VIDEO_MESSAGE = 9;

    /**OTHER*/
    public static final int TYPE_ID_DATA_MESSAGE_AMF3 = 15;
    public static final int TYPE_ID_SHARED_MESSAGE_AMF3 = 16;
    public static final int TYPE_ID_COMMAND_MESSAGE_AMF3 = 17;
    public static final int TYPE_ID_DATA_MESSAGE_AMF0 = 18;
    public static final int TYPE_ID_SHARED_MESSAGE_AMF0 = 19;
    public static final int TYPE_ID_COMMAND_MESSAGE_AMF0 = 20;
    public static final int TYPE_ID_AGGREGATE_MESSAGE = 22;
}
