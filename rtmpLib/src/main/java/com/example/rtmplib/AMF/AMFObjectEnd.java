package com.example.rtmplib.AMF;

import java.util.Arrays;

public class AMFObjectEnd extends AMF {

    public AMFObjectEnd() {
        super(AMF.AFM0_OBJECT_END);
    }

    @Override
    public byte[] toBinary() {
        if (binaryData == null) {
            binaryData = new byte[3];
            binaryData[0] = 0x00;
            binaryData[1] = 0x00;
            binaryData[2] = typeMarker;
        }
        return binaryData;
    }

    @Override
    public String toString() {
        return "AFMObjectEnd{" +
                "typeMarker=" + typeMarker +
                ", binaryData=" + Arrays.toString(binaryData) +
                '}';
    }
}
