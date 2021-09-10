package com.example.rtmplib.parse;


import com.example.rtmplib.AMF.AMF;
import com.example.rtmplib.message.protocolcontrol.TypeId;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class BodyHandler implements ParseHandler {

    private RtmpResponse response;

    public BodyHandler(RtmpResponse response) {
        this.response = response;
    }

    @Override
    public RtmpResponse handle(InputStream is) {
        int bodyLen = response.getMessageLength3Bytes();
        if (bodyLen == 0) {
            System.out.println("chunk header parse failure");
        }
        byte[] bytes = new byte[bodyLen];
        RtmpParseHandler.read(is, bytes, 0, bodyLen);
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        parse(wrap);
        return response;
    }


    private void parse(ByteBuffer buffer) {
        switch (response.getMessageTypeId1Bytes()) {
            case TypeId.TYPE_ID_SET_CHUNK_SIZE:
            case TypeId.TYPE_ID_WINDOW_ACKNOWLEDGEMENT_SIZE:
            case TypeId.TYPE_ID_ABORT_MESSAGE:
                response.setIntBody(buffer.getInt());
                break;
            case TypeId.TYPE_ID_SET_PEER_BANDWIDTH:
                response.setIntBody(buffer.getInt());
                response.setbBody(buffer.get());
                break;
            case TypeId.TYPE_ID_COMMAND_MESSAGE_AMF0:
                int remaining = buffer.remaining();
                while (remaining > 0) {
                    byte b = buffer.get();
                    AMF amf = AMF.propertyParser(buffer, b);
                    if (amf == null) {
                        return;
                    }
                    response.addAfm(amf);
                    remaining = buffer.remaining();
                }
                break;
        }
    }

    @Override
    public String toString() {
        return "BodyHandler{" +
                "response=" + response +
                '}';
    }
}
