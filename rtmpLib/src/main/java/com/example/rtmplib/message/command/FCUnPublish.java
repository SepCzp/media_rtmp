package com.example.rtmplib.message.command;

import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;

/**
 * fmt:1
 * chunk stream id:3
 * message type id:AFM
 */
public class FCUnPublish extends Command {


    public FCUnPublish(String stream){
        addAMF(new AMFString(CommandTag.NAME_FC_PUBLISH));
        addAMF(new AMFNumber(6.0));
        addAMF(new AMFNull());
        addAMF(new AMFString(stream));
    }

}
