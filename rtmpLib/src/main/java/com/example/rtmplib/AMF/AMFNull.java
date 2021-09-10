package com.example.rtmplib.AMF;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AMFNull extends AMF {

    public AMFNull() {
        super(AMF.AFM0_NULL);
    }

    @Override
    public byte[] toBinary() {
        if (binaryData == null) {
            binaryData = new byte[1];
            binaryData[0] = typeMarker;
        }
        return binaryData;
    }


    public static final ICreator.Stub<AMFNull> CREATOR = new ICreator.Stub<AMFNull>() {
        @Override
        public AMFNull create(InputStream in) {
            return new AMFNull();
        }

        @Override
        public AMFNull create(ByteBuffer buffer) {
            return new AMFNull();
        }
    };

    @Override
    public String toString() {
        return "AFMNull{" +
                "typeMarker=" + typeMarker +
                '}';
    }
}
