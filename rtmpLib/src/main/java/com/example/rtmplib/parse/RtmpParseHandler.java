package com.example.rtmplib.parse;

import java.io.IOException;
import java.io.InputStream;

public class RtmpParseHandler {




    public static RtmpResponse parse(InputStream is) {
        BasicHeaderHandler basicHeaderHandler = new BasicHeaderHandler();
        RtmpResponse response = basicHeaderHandler.handle(is);
        response = new ChunkHeaderHandler(response).handle(is);
        return new BodyHandler(response).handle(is);
    }

    public static void read(InputStream is, byte[] dest, int offset, int remained) {
        if (dest != null && offset >= 0 && remained > 0) {
            try {
                int actual;
                do {
                    actual = is.read(dest, offset, remained);
                    offset += actual;
                    remained -= actual;
                } while (remained > 0 && actual > 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
