package com.example.rtmplib.handskake;


import com.example.rtmplib.IBinary;

import java.util.Random;

public class CS1 implements IBinary {

    public static final int HANDSHAKE = 1536;
    public static final int RANDOM_BYTES_LENGTH = 1528;
    public static final int ZERO = 0x00;
    private int time;
    private byte[] bytes;

    public CS1() {
        time = (int) (System.currentTimeMillis() / 1000);
    }

    /*
     * 解析
     * */
    public CS1(byte[] data) {
        if (data.length == 1536) {
            time |= ((data[0] & 0xFF) << 24);
            time |= ((data[1] & 0xFF) << 16);
            time |= ((data[2] & 0xFF) << 8);
            time |= (data[3] & 0xFF);

            bytes = new byte[RANDOM_BYTES_LENGTH];
            System.arraycopy(data, 8, bytes, 0, RANDOM_BYTES_LENGTH);
        } else {
            //this data length is inconsistent
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    private byte[] getRandomBytes() {
        byte[] bytes = new byte[RANDOM_BYTES_LENGTH];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public static int getHANDSHAKE() {
        return HANDSHAKE;
    }

    public static int getZERO() {
        return ZERO;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public byte[] toBinary() {

        byte[] data = new byte[HANDSHAKE];
        data[0] = (byte) (time >>> 24);
        data[1] = (byte) (time >>> 16);
        data[2] = (byte) (time >>> 8);
        data[3] = (byte) time;
        data[4] = ZERO;
        data[5] = ZERO;
        data[6] = ZERO;
        data[7] = ZERO;
        byte[] randomBytes = getRandomBytes();
        System.arraycopy(randomBytes, 0, data, 8, randomBytes.length);

        return data;
    }
}
