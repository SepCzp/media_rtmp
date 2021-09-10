package com.example.rtmplib.message.command;

import androidx.annotation.NonNull;

import com.example.rtmplib.AMF.AMF;
import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;

import java.util.List;


/**
 * fmt:1
 * chunk stream id:3
 * type id:AMF0
 */
public class ReleaseStream extends Command {

    public ReleaseStream(String channel) {
        addAMF(new AMFString(CommandTag.NAME_RELEASE_STREAM));
        addAMF(new AMFNumber(2.0));
        addAMF(new AMFNull());
        addAMF(new AMFString(channel));
    }


}
