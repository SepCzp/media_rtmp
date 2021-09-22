package com.example.rtmplib.parse;


import com.example.rtmplib.header.BasicHeader;

import java.io.IOException;
import java.io.InputStream;

public class BasicHeaderHandler implements ParseHandler {


    public BasicHeaderHandler() {
    }

    /*
     * rtmp basic header 有三种格式，根据 chunk stream id 来判断
     * 1. basic header 占1字节，csid=[2,63]
     * 2. basic header 占2字节，csid=[64,256+63=319]
     * 3. basic header 占3字节  csid=[64,65599]
     *
     * 解析方法，先读1字节，解析前6个bit，下列结果：
     * >1：第一种格式
     * =0：第二种格式
     * =1: 第三种格式
     * */
    @Override
    public RtmpResponse handle(InputStream is) {
        byte[] bytes = new byte[3];
        try {
            is.read(bytes, 0, 1);
            byte b = bytes[0];
            int streamType = b & BasicHeader.STREAM_ID_ONE_BYTE_MASK;
            int fmt = (b & BasicHeader.FMT_ONE_BYTE_MASK) >>> 6;
            RtmpResponse rtmpResponse = new RtmpResponse();
            switch (streamType) {
                case 0:
                    is.read(bytes, 1, 1);
                    rtmpResponse.setChunkStreamId((bytes[1] & 0x000000FF) + 64);
                    rtmpResponse.setBasicHeaderByteLen(2);
                    break;
                case 1:
                    is.read(bytes, 1, 2);
                    rtmpResponse.setChunkStreamId((bytes[1] & 0x000000FF) + 64 + ((bytes[2] & 0x000000FF) * 256));
                    rtmpResponse.setBasicHeaderByteLen(3);
                    break;
                case 3:
                    break;
                default:
                    rtmpResponse.setFmt2Bit(fmt);
                    rtmpResponse.setChunkStreamId(streamType);
                    rtmpResponse.setBasicHeaderByteLen(1);
                    break;
            }
            return rtmpResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
