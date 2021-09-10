package com.example.rtmplib.message.command;

import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;

/**
 * fmt:0
 * chunk stream id:4
 * message type id:AMF
 */
public class Publish extends Command {

    public Publish(String appName, String stream) {
        addAMF(new AMFString(CommandTag.NAME_PUBLISH));
        addAMF(new AMFNumber(5.0));
        addAMF(new AMFNull());
        addAMF(new AMFString(stream));
        addAMF(new AMFString(appName));
    }

}
