package com.example.rtmplib.message.command;

import com.example.rtmplib.AMF.AMFNull;
import com.example.rtmplib.AMF.AMFNumber;
import com.example.rtmplib.AMF.AMFString;
import com.example.rtmplib.message.A;

public class DeleteStream extends Command{

    public DeleteStream(){
        addAMF(new AMFString(CommandTag.NAME_DELETE_STREAM));
        addAMF(new AMFNumber(7.0));
        addAMF(new AMFNull());
        addAMF(new AMFNumber(1.0));
    }
}
