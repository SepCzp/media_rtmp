package com.example.rtmplib.AMF;

public class AMFBoolean extends AMF {

    public static final byte FALSE = (byte) 0x00;
    public static final byte TRUE = (byte) 0x01;
    public static final byte NULL = (byte) 0x02;

    private byte value;

    public AMFBoolean(byte b) {
        super(AMF.AFM0_BOOLEAN);
        value = (byte) Math.abs(b);
    }

    @Override
    public byte[] toBinary() {
        if (value == NULL) {
            return new AMFNull().toBinary();
        }
        byte[] bytes = new byte[2];
        bytes[0] = typeMarker;
        bytes[1] = value;
        return bytes;
    }
}
