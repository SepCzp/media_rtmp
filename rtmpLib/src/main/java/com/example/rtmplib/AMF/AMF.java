package com.example.rtmplib.AMF;


import com.example.rtmplib.IBinary;

import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class AMF implements IBinary {

    public static final byte AFM0_NUMBER = 0x00;
    public static final byte AFM0_BOOLEAN = 0x01;
    public static final byte AFM0_STRING = 0x02;
    public static final byte AFM0_OBJECT = 0x03;
    public static final byte AFM0_MOVIECLIP = 0x04;
    public static final byte AFM0_NULL = 0x05;
    public static final byte AFM0_UNDEFINED = 0x06;
    public static final byte AFM0_REFERENCE = 0x07;
    public static final byte AFM0_ECMA_ARRAY = 0x08;
    public static final byte AFM0_OBJECT_END = 0x09;
    public static final byte AFM0_STRICT_ARRAY = 0x0a;
    public static final byte AFM0_DATE = 0x0b;
    public static final byte AFM0_LONG_STRING = 0x0c;
    public static final byte AFM0_UNSUPPORTED = 0x0d;
    public static final byte AFM0_RECORDSET = 0x0e;
    public static final byte AFM0_XML_DOCUMENT = 0x0f;
    public static final byte AFM0_TYPED_OBJECT = 0x10;

    protected byte typeMarker;
    protected byte[] binaryData;


    public AMF(byte typeMarker) {
        this.typeMarker = typeMarker;
    }

    public static AMF propertyParser(ByteBuffer buffer, int marker) {
        switch (marker) {
            case AMF.AFM0_NUMBER:
                return AMFNumber.CREATOR.create(buffer);
//            case AMFData.BOOLEAN_MARKER:
//                return AMFBoolean.Creator.create(buffer);
            case AMF.AFM0_STRING:
                return AMFString.CREATOR.create(buffer);
            case AMF.AFM0_OBJECT:
                return AMFObject.CREATOR.create(buffer);
//            case AMFData.UNDEFINED_MARKER:
//                return AMFUndefined.Creator.create(buffer);
//            case AMFData.REFERENCE_MARKER:
//                return AMFReference.Creator.create(buffer);
//            case AMFData.ECMA_ARRAY_MARKER:
//                return AMFECMAArray.Creator.create(buffer);
//            case AMFData.STRICT_ARRAY_MARKER:
//                return AMFStrictArray.Creator.create(buffer);
            case AMF.AFM0_NULL:
                return AMFNull.CREATOR.create(buffer);
//            case AMFData.DATE_MARKER:
//                return AMFDate.Creator.create(buffer);
//            case AMFData.LONG_STRING_MARKER:
//                return AMFLongString.Creator.create(buffer);
//            case AMFData.TYPED_OBJECT_MARKER:
//                return AMFTypedObject.Creator.create(buffer);
            default:
                return null;
        }
    }

    public static AMF propertyParser(InputStream is, int marker) {
        switch (marker) {
            case AMF.AFM0_NUMBER:
                return AMFNumber.CREATOR.create(is);
//            case AMFData.BOOLEAN_MARKER:
//                return AMFBoolean.Creator.create(buffer);
            case AMF.AFM0_STRING:
                return AMFString.CREATOR.create(is);
            case AMF.AFM0_OBJECT:
                return AMFObject.CREATOR.create(is);
//            case AMFData.UNDEFINED_MARKER:
//                return AMFUndefined.Creator.create(buffer);
//            case AMFData.REFERENCE_MARKER:
//                return AMFReference.Creator.create(buffer);
            case AMF.AFM0_ECMA_ARRAY:
                return AMFECMAArray.CREATOR.create(is);
//            case AMFData.STRICT_ARRAY_MARKER:
//                return AMFStrictArray.Creator.create(buffer);
            case AMF.AFM0_NULL:
                return AMFNull.CREATOR.create(is);
//            case AMFData.DATE_MARKER:
//                return AMFDate.Creator.create(buffer);
//            case AMFData.LONG_STRING_MARKER:
//                return AMFLongString.Creator.create(buffer);
//            case AMFData.TYPED_OBJECT_MARKER:
//                return AMFTypedObject.Creator.create(buffer);
            default:
                return null;
        }
    }

}
