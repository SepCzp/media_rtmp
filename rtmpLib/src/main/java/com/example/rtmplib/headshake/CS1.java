package com.example.rtmplib.headshake;


import com.example.rtmplib.IBinary;

import java.util.Random;

public class CS1 implements IBinary {

    public static final int HANDSHAKE = 1536;
    public static final int ZERO = 0x00;
    private int time;

    public CS1() {
        time = (int) (System.currentTimeMillis() / 1000);
    }

    private byte[] getRandomBytes() {
        byte[] bytes = new byte[1528];
        new Random().nextBytes(bytes);
        return bytes;
    }

    @Override
    public byte[] toBinary() {

        byte[] data = new byte[HANDSHAKE];
        data[0] = (byte) (time >>> 24);
        data[1] = (byte) (time >>> 16);
        data[2] = (byte) (time >>> 8);
        data[3] = (byte) ((time & 0xFF));
        data[4] = ZERO;
        data[5] = ZERO;
        data[6] = ZERO;
        data[7] = ZERO;
        byte[] randomBytes = getRandomBytes();
        System.arraycopy(randomBytes, 0, data, 8, randomBytes.length);

        return data;
    }
}
