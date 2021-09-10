package com.example.rtmplib.message.command;


import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;
import com.example.rtmplib.message.A;

/**
 * fmt:1
 * chunk stream id:1
 * type id:AMF0
 */
public class FCPublish extends Command {

    public FCPublish(String stream) {
        addAMF(new AMFString(CommandTag.NAME_FC_PUBLISH));
        addAMF(new AMFNumber(3.0));
        addAMF(new AMFNull());
        addAMF(new AMFString(stream));
    }
}
