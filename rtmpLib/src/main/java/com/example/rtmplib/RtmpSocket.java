package com.example.rtmplib;


import com.example.rtmplib.message.protocolcontrol.Connect;
import com.example.rtmplib.message.protocolcontrol.SetChunkSize;
import com.example.rtmplib.header.RtmpChunk;
import com.example.rtmplib.headshake.CS1;
import com.example.rtmplib.parse.RtmpParseHandler;
import com.example.rtmplib.parse.RtmpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RtmpSocket {

    private static int c0_version = 0x03;
    private static int handshakeSize = 1536;

    public static void main(String[] args) {
        try {

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("192.168.88.182", 1935), 3000);
            socket.setKeepAlive(true);
            byte[] data = new byte[1537];
            if (socket.isConnected()) {//tcp连接成功
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(c0_version);
                outputStream.flush();
                outputStream.write(writeC1());
                outputStream.flush();
                InputStream inputStream = socket.getInputStream();
                int read = inputStream.read(data);
                //复制s1发送c2
                byte[] s1 = new byte[1536];
                System.arraycopy(data, 1, s1, 0, s1.length);
                printBytes(s1, "S1");
                System.out.println("s1 length:" + read);
                outputStream.write(s1);
                outputStream.flush();
                read = inputStream.read(data);
                printBytes(data, "S2");
                System.out.println("s2 length:" + read);

                //set chunk size
                outputStream.write(setChunkSize());
                outputStream.flush();

                outputStream.write(connect());
                outputStream.flush();

                RtmpResponse parse = RtmpParseHandler.parse(inputStream);
                System.out.println(parse.toString());

                parse = RtmpParseHandler.parse(inputStream);
                System.out.println(parse.toString());

                parse = RtmpParseHandler.parse(inputStream);
                System.out.println(parse.toString());

                parse = RtmpParseHandler.parse(inputStream);
                for (; ; ) {
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           time (4 bytes)                      |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           zero (4 bytes)                      |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           random bytes                        |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           random bytes                        |
     * |                               (cont)                          |
     * |                               ....                            |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     * @return
     */
    private static byte[] writeC1() {
        return new CS1().toBinary();
    }

    private static void printBytes(byte[] bytes, String tag) {
        System.out.println("===================" + tag + "======================");
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i]);
        }
        System.out.println("===================end======================");
    }

    private static byte[] setChunkSize() {
        //set chunk size
        RtmpChunk rtmpChunk = new RtmpChunk.Builder()
                .setBasicHeader(0, 2)
                .setChunkHeader(0, 4, 0x01, 0)
                .setChunkData(new SetChunkSize(4096).toBinary())
                .build();
        return rtmpChunk.toBinary();
    }

    private static byte[] connect() {
        //connect
        Connect connect = new Connect(1, "", "");
        byte[] connectBytes = connect.toBinary();
        RtmpChunk rtmpChunk = new RtmpChunk.Builder()
                .setBasicHeader(0)
                .setChunkHeader(0, connectBytes.length, 0x14, 0)
                .setChunkData(connectBytes)
                .build();
        return rtmpChunk.toBinary();
    }
}
