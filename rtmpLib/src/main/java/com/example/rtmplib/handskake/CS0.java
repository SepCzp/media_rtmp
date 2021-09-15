package com.example.rtmplib.handskake;

import com.example.networktest.rtmp.IBinary;

public class CS0 implements IBinary {

    private byte version = 0x03;

    public CS0() {
    }

    public CS0(byte version) {
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    @Override
    public byte[] toBinary() {
        return new byte[]{version};
    }
}
