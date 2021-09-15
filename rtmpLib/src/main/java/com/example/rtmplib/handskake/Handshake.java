package com.example.rtmplib.handskake;


import com.example.rtmplib.AMF.ICreator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Handshake {

    private OutputStream write;
    private InputStream read;
    private CS1 c1;
    private CS1 s1;

    public Handshake(OutputStream write, InputStream read) {
        this.write = write;
        this.read = read;
    }

    /**
     * begin handShake
     */
    private void realHandshake() throws IOException {
        //send c0
        CS0 c0 = new CS0();
        write.write(c0.toBinary());
        //send c1
        c1 = new CS1();
        write.write(c1.toBinary());

        //response s0
        CS0 s0 = new CS0((byte) read.read());
        System.out.println("s0 version: " + s0.getVersion());
        //response s0
        byte[] bytes = new byte[1536];
        ICreator.Stub.read(read, bytes, 0, 1536);
        s1 = new CS1(bytes);

        //send c2
        CS2 c2 = new CS2(s1.getTime(), c1.getTime());
        write.write(c2.toBinary());
        System.out.println(c2);
    }

    public boolean handshake() {
        try {
            realHandshake();
            return ackSent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean ackSent() throws IOException {
        byte[] bytes = new byte[CS1.HANDSHAKE];
        int actual = 0;
        int offset = 0;
        int remained = CS1.HANDSHAKE;
        do {
            actual = read.read(bytes, offset, remained);
            offset += actual;
            remained -= actual;
        } while (actual > 0 && remained > 0);

        if (offset != CS1.HANDSHAKE) {
            return false;
        }
        return true;
    }

}
