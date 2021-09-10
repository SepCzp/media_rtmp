package com.example.rtmplib.AMF;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class AMFECMAArray extends AMFObject {

    private int arraySize4Bytes;

    public AMFECMAArray() {
        typeMarker = AMF.AFM0_ECMA_ARRAY;
    }

    public void setCount(int count) {
        arraySize4Bytes = count;
    }

    public int getCount() {
        return arraySize4Bytes;
    }

    public static final ICreator.Stub<AMFECMAArray> CREATOR = new ICreator.Stub<AMFECMAArray>() {

        @Override
        public AMFECMAArray create(InputStream in) {
            try {
                AMFECMAArray amfecmaArray = new AMFECMAArray();

                byte[] count = new byte[4];
                read(in, count, 0, 4);
                int len = 0;
                len |= ((count[0] & 0x000000FF) << 24);
                len |= ((count[1] & 0x000000FF) << 16);
                len |= ((count[2] & 0x000000FF) << 8);
                len |= ((count[3] & 0x000000FF));

                amfecmaArray.setCount(len);
                for (int i = 0; i < len; i++) {
                    AMFString key = (AMFString) AMF.propertyParser(in, AMF.AFM0_STRING);
                    AMF value = AMF.propertyParser(in, in.read());
                    amfecmaArray.add(key, value);
                }
                byte[] objectEnd = new byte[3];
                read(in, objectEnd, 0, 3);
                return amfecmaArray;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return super.create(in);
        }

        @Override
        public AMFECMAArray create(ByteBuffer buffer) {
            AMFECMAArray amfecmaArray = new AMFECMAArray();
            int len = buffer.getInt();
            amfecmaArray.setCount(len);

            for (int i = 0; i < len; i++) {
                AMFString key = (AMFString) AMF.propertyParser(buffer, buffer.get());
                AMF value = AMF.propertyParser(buffer, buffer.get());
                amfecmaArray.add(key, value);
            }
            byte[] objectEnd = new byte[3];
            buffer.get(objectEnd);
            return amfecmaArray;
        }
    };
}
