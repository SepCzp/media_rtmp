package com.example.rtmplib.header;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * +--------------+----------------+--------------------+--------------+
 * | Basic Header | Message Header | Extended Timestamp |  Chunk Data  |
 * +--------------+----------------+--------------------+--------------+
 * |                                                    |
 * |<------------------- Chunk Header ----------------->|
 * Chunk Format
 * <p>
 * <p>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          timestamp            | message length|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      message length (cont)    |message type id| msg stream id |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          message stream id (cont)             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * Chunk Message Header - Type 0
 * <p>
 * <p>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          timestamp            | message length|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      message length (cont)    |message type id|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * Chunk Message Header - Type 1
 * <p>
 * <p>
 * 0                   1                   2
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                          timestamp            |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * Chunk Message Header - Type 2
 */
public class ChunkHeader implements IBinary {


    public static final int MAX_VALUE_TIMESTAMP = 0x00FFFFFF;


    /*时间戳 占3字节*/
    private int timestamp3Bytes;
    /*消息长度 占3字节*/
    private int messageLength3Bytes;
    /*消息类型 占1字节，如8代表音频数据、9代表视频数据。*/
    private int messageTypeId1Bytes;
    private int msgStreamId4Bytes;
    /*外部时间戳 占3字节*/
    private int extendedTimestamp;

    private BasicHeader basicHeader;
    private byte[] binary;


    public ChunkHeader(int timestamp3Bytes, int messageLength3Bytes, int messageTypeId1Bytes, int msgStreamId4Bytes) {
        this.timestamp3Bytes = timestamp3Bytes;
        this.messageLength3Bytes = messageLength3Bytes;
        this.messageTypeId1Bytes = messageTypeId1Bytes;
        this.msgStreamId4Bytes = msgStreamId4Bytes;
    }

    public ChunkHeader(int timestamp3Bytes, int messageLength3Bytes, int messageTypeId1Bytes) {
        this(timestamp3Bytes, messageLength3Bytes, messageTypeId1Bytes, 0);
    }

    public ChunkHeader(int timestamp3Bytes) {
        this(timestamp3Bytes, 0, 0);
    }

    public void setBasicHeader(BasicHeader basicHeader) {
        this.basicHeader = basicHeader;
    }

    @Override
    public byte[] toBinary() {
        if (binary == null) {
            byte[] timeBytes = null;
            byte[] messageLenBytes = null;
            byte[] messageTypeBytes = null;
            byte[] streamIdBytes = null;
            byte[] result = null;
            int offset = 0;
            switch (basicHeader.getFmt()) {
                case BasicHeader.FMT_TYPE_0://onMetaData 流开始的绝对时间戳     控制消息（如connect）
                    result = new byte[11];
                    if (timestamp3Bytes >= MAX_VALUE_TIMESTAMP) {
                        timestamp3Bytes = MAX_VALUE_TIMESTAMP;
                        extendedTimestamp = (int) (System.currentTimeMillis() / 1000);
                    }
                    timeBytes = BinaryUtil.getBytesFromIntValue(timestamp3Bytes, 3);
                    messageLenBytes = BinaryUtil.getBytesFromIntValue(messageLength3Bytes, 3);
                    messageTypeBytes = BinaryUtil.getBytesFromIntValue(messageTypeId1Bytes, 1);
                    streamIdBytes = BinaryUtil.getBytesFromIntValue(msgStreamId4Bytes, 4);
                    System.arraycopy(timeBytes, 0, result, 0, timeBytes.length);
                    offset += timeBytes.length;
                    System.arraycopy(messageLenBytes, 0, result, offset, messageLenBytes.length);
                    offset += messageLenBytes.length;
                    System.arraycopy(messageTypeBytes, 0, result, offset, messageTypeBytes.length);
                    offset += messageTypeBytes.length;
                    System.arraycopy(streamIdBytes, 0, result, offset, streamIdBytes.length);
                    break;
                case BasicHeader.FMT_TYPE_1://大部分的rtmp header都是8字节的
                    result = new byte[7];
                    if (timestamp3Bytes >= MAX_VALUE_TIMESTAMP) {
                        timestamp3Bytes = MAX_VALUE_TIMESTAMP;
                        extendedTimestamp = (int) (System.currentTimeMillis() / 1000);
                    }
                    timeBytes = BinaryUtil.getBytesFromIntValue(timestamp3Bytes, 3);
                    messageLenBytes = BinaryUtil.getBytesFromIntValue(messageLength3Bytes, 3);
                    messageTypeBytes = BinaryUtil.getBytesFromIntValue(messageTypeId1Bytes, 1);
                    System.arraycopy(timeBytes, 0, result, 0, timeBytes.length);
                    offset += timeBytes.length;
                    System.arraycopy(messageLenBytes, 0, result, offset, messageLenBytes.length);
                    offset += messageLenBytes.length;
                    System.arraycopy(messageTypeBytes, 0, result, offset, messageTypeBytes.length);
                    break;
                case BasicHeader.FMT_TYPE_2://少见
                    if (timestamp3Bytes >= MAX_VALUE_TIMESTAMP) {
                        timestamp3Bytes = MAX_VALUE_TIMESTAMP;
                        extendedTimestamp = (int) (System.currentTimeMillis() / 1000);
                    }
                    result = BinaryUtil.getBytesFromIntValue(timestamp3Bytes, 3);
                    break;
                case BasicHeader.FMT_TYPE_3://偶尔出现，低于8字节频率
                    result = null;
                    break;
            }
            byte[] basicArray = basicHeader.toBinary();
            if (result != null) {
                if (extendedTimestamp != 0) {
                    binary = new byte[basicArray.length + result.length + 4];
                    System.arraycopy(basicArray, 0, binary, 0, basicArray.length);
                    System.arraycopy(result, 0, binary, basicArray.length, result.length);
                    System.arraycopy(BinaryUtil.getBytesFromIntValue(extendedTimestamp, 4), 0, binary, basicArray.length + result.length, 4);
                } else {
                    binary = new byte[basicArray.length + result.length];
                    System.arraycopy(basicArray, 0, binary, 0, basicArray.length);
                    System.arraycopy(result, 0, binary, basicArray.length, result.length);
                }
            } else {
                binary = basicArray;
            }
        }
        return binary;
    }
}
