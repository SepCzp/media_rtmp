package com.example.rtmplib.headshake;


import com.example.rtmplib.IBinary;

public class CS0 implements IBinary {

    private byte version = 0x03;

    public CS0() {
    }

    public CS0(byte version) {
        this.version = version;
    }

    @Override
    public byte[] toBinary() {
        return new byte[]{version};
    }
}
