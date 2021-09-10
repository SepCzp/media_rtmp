package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * 5 bytes
 * message tyoe id  6
 */
public class SetPeerWidth implements IBinary {

    public static final int TYPE_0_HARD = 0x00;
    public static final int TYPE_1_SOFT = 0x01;
    public static final int TYPE_2_DYNAMIC = 0x02;

    private int brandWidth;
    private byte limitType;
    private byte[] binary;

    public SetPeerWidth(int brandWidth) {
        this(brandWidth, (byte) TYPE_2_DYNAMIC);
    }

    public SetPeerWidth(int brandWidth, byte limitType) {
        if (limitType < TYPE_0_HARD || limitType > TYPE_2_DYNAMIC) {
            //todo
        }
        this.brandWidth = brandWidth;
        this.limitType = limitType;
    }


    @Override
    public byte[] toBinary() {
        if (binary == null) {
            binary = new byte[5];
            byte[] width = BinaryUtil.getBytesFromIntValue(brandWidth, 4);
            System.arraycopy(width, 0, binary, 0, 4);
            binary[4] = limitType;
        }
        return binary;
    }
}
