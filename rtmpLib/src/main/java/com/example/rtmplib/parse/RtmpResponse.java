package com.example.rtmplib.parse;


import com.example.rtmplib.AMF.AMF;

import java.util.ArrayList;
import java.util.List;

public class RtmpResponse {

    private int fmt2Bit;
    private int chunkStreamId;
    private int basicHeaderByteLen;

    /*时间戳 占3字节*/
    private int timestamp3Bytes;
    /*消息长度 占3字节*/
    private int messageLength3Bytes;
    /*消息类型 占1字节，如8代表音频数据、9代表视频数据。*/
    private int messageTypeId1Bytes;
    private int msgStreamId4Bytes;
    /*外部时间戳 占3字节*/
    private int extendedTimestamp;

    /*set chunk size*/
    private Integer intBody;
    private Byte bBody;

    private List<AMF> AMFS;

    public RtmpResponse() {

    }

    public void setbBody(Byte bBody) {
        this.bBody = bBody;
    }

    public Byte getbBody() {
        return bBody;
    }

    public void setIntBody(Integer intBody) {
        this.intBody = intBody;
    }

    public Integer getIntBody() {
        return intBody;
    }

    public void setBasicHeaderByteLen(int basicHeaderByteLen) {
        this.basicHeaderByteLen = basicHeaderByteLen;
    }

    public int getBasicHeaderByteLen() {
        return basicHeaderByteLen;
    }

    public int getFmt2Bit() {
        return fmt2Bit;
    }

    public void setFmt2Bit(int fmt2Bit) {
        this.fmt2Bit = fmt2Bit;
    }

    public int getChunkStreamId() {
        return chunkStreamId;
    }

    public void setChunkStreamId(int chunkStreamId) {
        this.chunkStreamId = chunkStreamId;
    }

    public int getTimestamp3Bytes() {
        return timestamp3Bytes;
    }

    public void setTimestamp3Bytes(int timestamp3Bytes) {
        this.timestamp3Bytes = timestamp3Bytes;
    }

    public int getMessageLength3Bytes() {
        return messageLength3Bytes;
    }

    public void setMessageLength3Bytes(int messageLength3Bytes) {
        this.messageLength3Bytes = messageLength3Bytes;
    }

    public int getMessageTypeId1Bytes() {
        return messageTypeId1Bytes;
    }

    public void setMessageTypeId1Bytes(int messageTypeId1Bytes) {
        this.messageTypeId1Bytes = messageTypeId1Bytes;
    }

    public int getMsgStreamId4Bytes() {
        return msgStreamId4Bytes;
    }

    public void setMsgStreamId4Bytes(int msgStreamId4Bytes) {
        this.msgStreamId4Bytes = msgStreamId4Bytes;
    }

    public int getExtendedTimestamp() {
        return extendedTimestamp;
    }

    public void setExtendedTimestamp(int extendedTimestamp) {
        this.extendedTimestamp = extendedTimestamp;
    }

    public List<AMF> getAfms() {
        return AMFS;
    }

    public void addAfm(AMF AMF) {
        if (AMFS == null) {
            AMFS = new ArrayList<>();
        }
        AMFS.add(AMF);
    }

    @Override
    public String toString() {
        return "RtmpResponse{" +
                "fmt2Bit=" + fmt2Bit +
                ", chunkStreamId=" + chunkStreamId +
                ", basicHeaderByteLen=" + basicHeaderByteLen +
                ", timestamp3Bytes=" + timestamp3Bytes +
                ", messageLength3Bytes=" + messageLength3Bytes +
                ", messageTypeId1Bytes=" + messageTypeId1Bytes +
                ", msgStreamId4Bytes=" + msgStreamId4Bytes +
                ", extendedTimestamp=" + extendedTimestamp +
                ", intBody=" + intBody +
                ", bBody=" + bBody +
                ", afms=" + AMFS +
                '}';
    }
}
