package com.example.rtmplib.AMF;

public class AMFUndefind extends AMF{
    public AMFUndefind() {
        super(AMF.AFM0_UNDEFINED);
    }

    @Override
    public byte[] toBinary() {
        return new byte[]{typeMarker};
    }
}
