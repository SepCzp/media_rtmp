package com.example.rtmplib.header;


import com.example.rtmplib.IBinary;
import com.example.rtmplib.util.BinaryUtil;

/**
 * BasicHeader 结构
 * 0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+
 * |fmt|    cs id  |
 * +-+-+-+-+-+-+-+-+
 * <p>
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |fmt|     0     |    cs id - 64 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * <p>
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |fmt|      1    |        cs id - 64             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class BasicHeader implements IBinary {

    //fm高2位最大值(2进制转10进制)，在占1字节时fm max value：1*2的次方7+1*2的次方6=192
    public static final int FMT_ONE_BYTE_MASK = 0x000000C0;
    public static final int FMT_TWO_BYTES_MASK = 0x0000C000;
    public static final int FMT_THREE_BYTES_MASK = 0x00C00000;
    //stream chunk id低 6位/8位/16位最大值(2进制转10进制)
    public static final int STREAM_ID_ONE_BYTE_MASK = 0x0000003F;
    public static final int STREAM_ID_TWO_BYTES_MASK = 0x000000FF;
    public static final int STREAM_ID_THREE_BYTES_MASK = 0x003FFFFF;

    public static final int STREAM_ID_MIN_VALUE = 0;
    public static final int STREAM_ID_MAX_VALUE = 65599;

    public static final int FMT_TYPE_0 = 0;
    public static final int FMT_TYPE_1 = 1;
    public static final int FMT_TYPE_2 = 2;
    public static final int FMT_TYPE_3 = 3;

    private int fmt;
    private int chunkStreamId;
    private int realBytesLength;
    private byte[] binary;

    public BasicHeader(int fmt) {
        this.fmt = fmt;
        setChunkStreamId(3);
    }

    public BasicHeader(int fmt, int chunkStreamId) {
        this.fmt = fmt;
        setChunkStreamId(chunkStreamId);
    }

    public void setFmt(int fmt) {
        this.fmt = fmt;
    }

    public int getFmt() {
        return fmt;
    }

    public void setChunkStreamId(int chunkStreamId) {
        if (chunkStreamId < STREAM_ID_MIN_VALUE ||
                chunkStreamId > STREAM_ID_MAX_VALUE) {
            System.out.println("The chunkStreamId must be between STREAM_ID_MIN_VALUE and STREAM_ID_MAX_VALUE");
            return;
        }

        /**
         * RTMP最多支持65597个流，ID在3-65599范围内。ID0，1，2为保留值。
         *  在3～63之间的csid，可以直接用2～7位来标识
         *         在64～319之间的csid，用第二个字节+64计算得来，第2～7位全部置0
         *         在64～65599之间的csid，用第三个字节值*255 + 第二个字节值 + 64计算得来，第2～7位全部置1
         *         64～319之间的csid既可以用两个字节来标识也可以用三个字节来表示
         * */
        if (chunkStreamId <= 63) {//[3,63]
            realBytesLength = 1;
            this.chunkStreamId = chunkStreamId & STREAM_ID_ONE_BYTE_MASK;
        } else if (chunkStreamId < 319) {//[64,256+63]
            realBytesLength = 2;
            this.chunkStreamId = chunkStreamId & STREAM_ID_TWO_BYTES_MASK;
        } else if (chunkStreamId < 65599) {//[319,65536+63]
            realBytesLength = 3;
            this.chunkStreamId = chunkStreamId | 0x003F0000 & STREAM_ID_THREE_BYTES_MASK;
        }
    }

    @Override
    public byte[] toBinary() {
        if (binary == null) {
            binary = new byte[realBytesLength];
            int value = 0;
            //左移位为了把fmt推上高位
            //使用|不改变任何一个值
            switch (realBytesLength) {
                case 1:
                    value |= (fmt & FMT_ONE_BYTE_MASK << 6);
                    value |= chunkStreamId & STREAM_ID_ONE_BYTE_MASK;
                    break;
                case 2:
                    value |= (fmt & FMT_TWO_BYTES_MASK << 14);
                    value |= chunkStreamId & STREAM_ID_TWO_BYTES_MASK;
                    break;
                case 3:
                    value |= (fmt & FMT_THREE_BYTES_MASK << 22);
                    value |= chunkStreamId & FMT_THREE_BYTES_MASK;
                    break;
            }
            byte[] data = BinaryUtil.getBytesFromIntValue(value, realBytesLength);
            System.arraycopy(data, 0, binary, 0, realBytesLength);
        }
        return binary;
    }
}
