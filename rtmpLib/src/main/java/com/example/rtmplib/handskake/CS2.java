package com.example.rtmplib.handskake;


import static com.example.rtmplib.handskake.CS1.HANDSHAKE;

import com.example.rtmplib.IBinary;

import java.util.Random;

public class CS2 implements IBinary {

    //对端的时间戳
    private int time;
    //c1/s1 send timestamp
    private int time1;

    public CS2(int time, int time1) {
        this.time = time;
        this.time1 = time1;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime1() {
        return time1;
    }

    public void setTime1(int time1) {
        this.time1 = time1;
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
        data[3] = (byte) time;
        data[4] = (byte) (time1 >>> 24);
        data[5] = (byte) (time1 >>> 16);
        data[6] = (byte) (time1 >>> 8);
        data[7] = (byte) time1;
        byte[] randomBytes = getRandomBytes();
        System.arraycopy(randomBytes, 0, data, 8, randomBytes.length);
        return data;
    }

    @Override
    public String toString() {
        return "CS2{" +
                "time=" + time +
                ", time1=" + time1 +
                '}';
    }
}
