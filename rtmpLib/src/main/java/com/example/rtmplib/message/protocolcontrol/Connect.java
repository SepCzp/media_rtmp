package com.example.rtmplib.message.protocolcontrol;


import com.example.rtmplib.AMF.AMFBoolean;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFObject;
import com.example.rtmplib.AMF.AMFString;
import com.example.rtmplib.IBinary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Connect implements IBinary {


    private AMFString connect;
    private AMFNumber transactionId;
    private AMFObject afmObject;
    private byte[] binary;

    //publish
    public Connect(double tIds, String app, String tcUrl) {

        connect = new AMFString("connect");
        transactionId = new AMFNumber(tIds);
        afmObject = new AMFObject();
        afmObject.add(new AMFString("flashVer"), new AMFString("FMLE/3.0 (compatible; FMSc/1.0)"));
        afmObject.add(new AMFString("type"), new AMFString("nonprivate"));
        afmObject.add(new AMFString("app"), new AMFString("live"));
        afmObject.add(new AMFString("swfUrl"), new AMFString("rtmp://192.168.88.182/live"));
        afmObject.add(new AMFString("tcUrl"), new AMFString("rtmp://192.168.88.182/live"));
    }

    //pull
    public Connect(double tIds, String app, String tcUrl, boolean b) {
        connect = new AMFString("connect");
        transactionId = new AMFNumber(tIds);
        afmObject = new AMFObject();
        afmObject.add(new AMFString("flashVer"), new AMFString("LNX 9,0,124,2"));
        afmObject.add(new AMFString("'capabilities'"), new AMFNumber(15.0));
        afmObject.add(new AMFString("app"), new AMFString("live"));
        afmObject.add(new AMFString("fpad"), new AMFBoolean(AMFBoolean.FALSE));
        afmObject.add(new AMFString("tcUrl"), new AMFString("rtmp://192.168.88.182:1935/live"));
        afmObject.add(new AMFString("audioCodecs"), new AMFNumber(4071.0));
        afmObject.add(new AMFString("videoCodecs"), new AMFNumber(252.0));
        afmObject.add(new AMFString("videoFunction"), new AMFNumber(1.0));
    }

    @Override
    public byte[] toBinary() {
        if (binary == null) {
            try {
                //todo 没有判断空对象
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(connect.toBinary());
                byteArrayOutputStream.write(transactionId.toBinary());
                byteArrayOutputStream.write(afmObject.toBinary());
                binary = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return binary;
    }
}
