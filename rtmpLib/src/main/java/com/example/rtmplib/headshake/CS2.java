package com.example.rtmplib.headshake;


import com.example.rtmplib.IBinary;

import java.util.Random;

public class CS2 implements IBinary {

    // Time (四个字节)：这个字段必须包含终端在 S1 (给 C2) 或者 C1 (给 S2) 发的 timestamp。
    private int time;
    // Time2 (四个字节)：这个字段必须包含终端先前发出数据包 (s1 或者 c1) timestamp。
    private int time1;

    public CS2() {
        this(0);
    }

    public CS2(int time1) {
        time = (int) (System.currentTimeMillis() / 1000);
        this.time1 = time1;
    }

    private byte[] getRandomBytes() {
        byte[] bytes = new byte[1528];
        new Random().nextBytes(bytes);
        return bytes;
    }

    @Override
    public byte[] toBinary() {
        byte[] data = new byte[CS1.HANDSHAKE];
        data[0] = (byte) ((time & 0xFF) >>> 24);
        data[1] = (byte) ((time & 0xFF) >>> 16);
        data[2] = (byte) ((time & 0xFF) >>> 8);
        data[3] = (byte) ((time & 0xFF));
        data[4] = (byte) ((time1 & 0xFF) >>> 24);
        data[5] = (byte) ((time1 & 0xFF) >>> 16);
        data[6] = (byte) ((time1 & 0xFF) >>> 8);
        data[7] = (byte) ((time1 & 0xFF));
        byte[] randomBytes = getRandomBytes();
        System.arraycopy(randomBytes, 0, data, 8, randomBytes.length);
        return new byte[0];
    }
}
