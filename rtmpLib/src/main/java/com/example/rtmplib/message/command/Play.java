package com.example.rtmplib.message.command;


import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;

/**
 * fmt:0
 * csid:8
 * message type id:0x14
 * sid:1
 */
public class Play extends Command {

    public Play(String stream) {
        addAMF(new AMFString("play"));
        addAMF(new AMFNumber(4.0));
        addAMF(new AMFNull());
        addAMF(new AMFString(stream));
        addAMF(new AMFNumber(-2000.0));
    }
}
