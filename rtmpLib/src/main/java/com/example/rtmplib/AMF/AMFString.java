package com.example.rtmplib.AMF;


import com.example.rtmplib.util.BinaryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * type-len-value
 * 1B-2B-len B
 */
public class AMFString extends AMF {

    private String value;

    public AMFString() {
        super(AMF.AFM0_STRING);
        value = null;
    }

    public AMFString(String value) {
        super(AMF.AFM0_STRING);
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public byte[] toBinary() {
        if (value == null || value.isEmpty()) {
            return new AMFNull().toBinary();
        }
        byte[] val = value.getBytes(StandardCharsets.UTF_8);
        byte[] lenByte = BinaryUtil.getBytesFromIntValue(val.length, 2);
        if (binaryData == null) {
            binaryData = new byte[val.length + 3];
            binaryData[0] = typeMarker;
            System.arraycopy(lenByte, 0, binaryData, 1, lenByte.length);
            System.arraycopy(val, 0, binaryData, 3, val.length);
        }
        return binaryData;
    }

    public static ICreator.Stub<AMFString> CREATOR = new ICreator.Stub<AMFString>() {
        @Override
        public AMFString create(InputStream in) {
            try {
                byte[] valLen = new byte[2];
                read(in, valLen, 0, 2);

                int len = 0;
                len |= ((valLen[0] & 0xFF) << 8);
                len |= (valLen[1] & 0xFF);

                byte[] strBytes = new byte[len];
                read(in, strBytes, 0, len);
                return INSTANCE(strBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.create(in);
        }

        @Override
        public AMFString create(ByteBuffer buffer) {
            return INSTANCE(buffer);
        }
    };


    public static AMFString INSTANCE(byte[] bytes) {
        return new AMFString(new String(bytes, StandardCharsets.UTF_8));
    }

    public static AMFString INSTANCE(ByteBuffer buffer) {
        byte[] valLen = new byte[2];
        buffer.get(valLen);

        int len = 0;
        len |= ((valLen[0] & 0xFF) << 8);
        len |= (valLen[1] & 0xFF);

        byte[] strBytes = new byte[len];
        buffer.get(strBytes);
        String value = new String(strBytes);
        return new AMFString(value);
    }


    @Override
    public String toString() {
        return "AFMString{" +
                "typeMarker=" + typeMarker +
                ", value='" + value + '\'' +
                '}';
    }
}
