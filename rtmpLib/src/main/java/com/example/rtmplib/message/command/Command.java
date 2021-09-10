package com.example.rtmplib.message.command;

import com.example.rtmplib.AMF.AMF;
import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.IBinary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Command implements IBinary {

    protected List<AMF> amfs;

    public Command() {
    }


    public void addAMF(AMF amf) {
        if (amfs == null) {
            amfs = new ArrayList<>();
        }
        amfs.add(amf);
    }

    @Override
    public byte[] toBinary() {
        if (amfs == null) {
            return new AMFNull().toBinary();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            for (AMF amf : amfs) {
                byteArrayOutputStream.write(amf.toBinary());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String toString() {
        return "Command{" +
                "amfs=" + amfs +
                '}';
    }
}
