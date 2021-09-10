package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * 窗口确认消息
 * of 4 bytes
 * message type id is 3
 */
public class Acknowledgement implements IBinary {


    private int number;

    public Acknowledgement(int number) {
        this.number = number;
    }

    @Override
    public byte[] toBinary() {
        return BinaryUtil.getBytesFromIntValue(number, 4);
    }
}
