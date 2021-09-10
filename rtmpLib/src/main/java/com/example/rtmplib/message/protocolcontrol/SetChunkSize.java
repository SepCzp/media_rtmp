package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/***
 * of 4 bytes
 * message type id 1
 * The maximum chunk size defaults to 128 bytes
 */
public class SetChunkSize implements IBinary {

    private int size;

    public SetChunkSize(int size) {
        this.size = size;
    }

    @Override
    public byte[] toBinary() {
        return BinaryUtil.getBytesFromIntValue(size, 4);
    }
}
