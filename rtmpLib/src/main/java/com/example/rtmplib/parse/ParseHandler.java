package com.example.rtmplib.parse;

import java.io.InputStream;

public interface ParseHandler {

    RtmpResponse handle(InputStream is);

}
