package com.example.rtmplib.AMF;


import com.example.rtmplib.util.BinaryUtil;

import java.nio.ByteBuffer;

/**
 * type-value
 * 1B-8B
 */
public class AMFNumber extends AMF {

    private Double value;

    public AMFNumber(Double d) {
        super(AMF.AFM0_NUMBER);
        value = d;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public byte[] toBinary() {
        if (value == null) {
            return new AMFNull().toBinary();
        }
        if (binaryData == null) {
            binaryData = new byte[9];
            binaryData[0] = typeMarker;
            byte[] doubleValue = BinaryUtil.getBytesFromDoubleValue(value, 8);
            System.arraycopy(doubleValue, 0, binaryData, 1, 8);
        }
        return binaryData;
    }

    public static ICreator.Stub<AMFNumber> CREATOR = new ICreator.Stub<AMFNumber>() {
        @Override
        public AMFNumber create(byte[] binary) {
            byte[] num = new byte[8];
            long l = 0L;
            l |= ((num[0] & 0x00FFL) << 56);
            l |= ((num[1] & 0x00FFL) << 48);
            l |= ((num[2] & 0x00FFL) << 40);
            l |= ((num[3] & 0x00FFL) << 32);
            l |= ((num[4] & 0x00FFL) << 24);
            l |= ((num[5] & 0x00FF) << 16);
            l |= ((num[6] & 0x00FF) << 8);
            l |= ((num[7] & 0x00FF));
            return new AMFNumber((double) l);
        }

        @Override
        public AMFNumber create(ByteBuffer buffer) {
            return new AMFNumber(Double.longBitsToDouble(buffer.getLong()));
        }
    };

    @Override
    public String toString() {
        return "AFMNumber{" +
                "typeMarker=" + typeMarker +
                ", value=" + value +
                '}';
    }
}
