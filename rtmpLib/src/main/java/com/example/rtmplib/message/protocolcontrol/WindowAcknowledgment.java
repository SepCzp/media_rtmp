package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * 设置窗口确认大小
 * 实现流量控制
 * of 4 bytes
 * message type id 5
 */
public class WindowAcknowledgment implements IBinary {

    private int size;

    public WindowAcknowledgment(int size) {
        this.size = size;
    }

    @Override
    public byte[] toBinary() {
        return BinaryUtil.getBytesFromIntValue(size, 4);
    }
}
