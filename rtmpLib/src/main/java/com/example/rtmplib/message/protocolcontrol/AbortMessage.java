package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * of 4 bytes
 * message type id is 2
 */
public class AbortMessage implements IBinary {

    private int chunkStreamId;

    public AbortMessage(int chunkStreamId) {
        this.chunkStreamId = chunkStreamId;
    }

    @Override
    public byte[] toBinary() {
        return BinaryUtil.getBytesFromIntValue(chunkStreamId, 4);
    }

    @Override
    public String toString() {
        return "AbortMessage{" +
                "chunkStreamId=" + chunkStreamId +
                '}';
    }
}
