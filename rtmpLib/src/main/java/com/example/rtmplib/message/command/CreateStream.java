package com.example.rtmplib.message.command;


import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;

/**
 * fmt:1
 * chunk stream id:3
 * message type id: AMF0
 */
public class CreateStream extends Command {
    public CreateStream() {
        addAMF(new AMFString(CommandTag.NAME_CREATE_STREAM));
        addAMF(new AMFNumber(4.0));
        addAMF(new AMFNull());
    }
}
