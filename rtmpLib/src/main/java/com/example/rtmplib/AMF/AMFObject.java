package com.example.rtmplib.AMF;


import com.example.rtmplib.IBinary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AMFObject extends AMF {

    protected List<ObjectProperty> properties = null;
    //AFM.AFM0_OBJECT 占1字节，Object_end 占3字节
    protected int size = 4;
    private AMFObjectEnd objectEnd;

    public AMFObject() {
        super(AMF.AFM0_OBJECT);
        properties = new ArrayList<>();
        objectEnd = new AMFObjectEnd();
    }

    public void add(AMFString key, AMF data) {
        //上传object 忽略key类型 所以要减去1字节
        int len = key.toBinary().length - 1 + data.toBinary().length;
        size += len;
        properties.add(new ObjectProperty(key, data, len));
    }

    public void clear() {
        size = 4;
        binaryData = null;
        properties.clear();
    }

    @Override
    public byte[] toBinary() {
        if (binaryData == null) {
            binaryData = new byte[size];
            binaryData[0] = typeMarker;
            int offset = 1;
            for (ObjectProperty property : properties) {
                System.arraycopy(property.toBinary(), 0, binaryData, offset, property.len);
                offset += property.len;
            }
            System.arraycopy(objectEnd.toBinary(), 0, binaryData, offset, 3);
        }
        return binaryData;
    }

    public static ICreator.Stub<AMFObject> CREATOR = new ICreator.Stub<AMFObject>() {
        @Override
        public AMFObject create(ByteBuffer buffer) {
            AMFObject amfObject = new AMFObject();
            AMFObjectEnd end = new AMFObjectEnd();
            byte[] endBytes = end.toBinary();
            byte[] temp = new byte[3];
            buffer.get(temp);
            while (!Arrays.equals(endBytes, temp)) {
                int len = 0;
                len |= ((temp[0] & 0xFF) << 8);
                len |= (temp[1] & 0xFF);

                byte[] cont = new byte[len];
                cont[0] = temp[2];

                buffer.get(cont, 1, len - 1);

                AMFString key = AMFString.INSTANCE(cont);
                byte type = buffer.get();
                AMF value = AMF.propertyParser(buffer, type);
                amfObject.add(key, value);

                buffer.get(temp);
            }
            return amfObject;
        }
    };

    protected static class ObjectProperty implements IBinary {

        AMFString key;
        AMF value;
        int len;
        byte[] propertyData;

        public ObjectProperty(AMFString key, AMF data, int len) {
            this.key = key;
            this.value = data;
            this.len = len;
        }

        @Override
        public byte[] toBinary() {
            if (propertyData == null) {
                propertyData = new byte[len];
            }
            byte[] keyArray = key.toBinary();
            int keyLen = keyArray.length;
            byte[] valueArray = value.toBinary();

            System.arraycopy(keyArray, 1, propertyData, 0, keyLen - 1);
            System.arraycopy(valueArray, 0, propertyData, keyLen - 1, valueArray.length);
            return propertyData;
        }

        @Override
        public String toString() {
            return "ObjectProperty{" +
                    "key=" + key +
                    ", value=" + value +
                    ", len=" + len +
                    ", propertyData=" + Arrays.toString(propertyData) +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AFMObject{" +
                "properties=" + properties +
                ", size=" + size +
                ", objectEnd=" + objectEnd +
                '}';
    }
}
