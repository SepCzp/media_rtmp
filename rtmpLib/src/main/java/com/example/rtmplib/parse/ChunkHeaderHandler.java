package com.example.rtmplib.parse;


import com.example.rtmplib.header.BasicHeader;
import com.example.rtmplib.header.ChunkHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ChunkHeaderHandler implements ParseHandler {

    private RtmpResponse rtmpResponse;

    public ChunkHeaderHandler(RtmpResponse rtmpResponse) {
        this.rtmpResponse = rtmpResponse;
    }


    private void parseType0(ByteBuffer buffer) {
        parseType1(buffer);
        int streamId = buffer.getInt();
        rtmpResponse.setMsgStreamId4Bytes(streamId);
    }

    private void parseType1(ByteBuffer buffer) {
        parseType2(buffer);
        byte[] bytes = new byte[3];
        buffer.get(bytes);
        int messageLen = 0;
        messageLen |= ((bytes[0] & 0xff) << 16);
        messageLen |= ((bytes[1] & 0xff) << 8);
        messageLen |= ((bytes[2] & 0xff));
        rtmpResponse.setMessageLength3Bytes(messageLen);

        int msgTypeID = buffer.get();
        rtmpResponse.setMessageTypeId1Bytes(msgTypeID);
    }

    private void parseType2(ByteBuffer buffer) {
        byte[] bytes = new byte[3];
        buffer.get(bytes);
        int timeStamp = 0;
        timeStamp |= ((bytes[0] & 0xff) << 16);
        timeStamp |= ((bytes[1] & 0xff) << 8);
        timeStamp |= ((bytes[2] & 0xff));
        if (timeStamp < 0x00ffffff) {
            rtmpResponse.setTimestamp3Bytes(timeStamp);
        }else {
            rtmpResponse.setTimestamp3Bytes(0x00ffffff);
        }
    }

    @Override
    public RtmpResponse handle(InputStream is) {
        if (rtmpResponse == null) {
            return null;
        }
        try {
            int fmt2Bit = rtmpResponse.getFmt2Bit();
            byte[] header = new byte[11];
            ByteBuffer buffer;
            switch (fmt2Bit) {
                case BasicHeader.FMT_TYPE_0://11字节
                    is.read(header, 0, 11);
                    buffer = ByteBuffer.wrap(header);
                    parseType0(buffer);
                    break;
                case BasicHeader.FMT_TYPE_1://7字节
                    is.read(header, 0, 7);
                    buffer = ByteBuffer.wrap(header);
                    parseType0(buffer);
                    break;
                case BasicHeader.FMT_TYPE_2://4字节
                    is.read(header, 0, 4);
                    buffer = ByteBuffer.wrap(header);
                    parseType0(buffer);
                    break;
                case BasicHeader.FMT_TYPE_3://1字节
                    break;
            }

            if (rtmpResponse.getTimestamp3Bytes() == ChunkHeader.MAX_VALUE_TIMESTAMP) {
                byte[] extendsTimeArray = new byte[4];
                RtmpParseHandler.read(is, extendsTimeArray, 0, 4);
                buffer = ByteBuffer.wrap(extendsTimeArray);
                rtmpResponse.setExtendedTimestamp(buffer.getInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rtmpResponse;
    }
}
