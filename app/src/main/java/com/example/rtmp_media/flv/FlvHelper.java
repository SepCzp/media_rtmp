package com.example.rtmp_media.flv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FlvHelper {

    public static final int FLV_TAG_HEADER_LEN = 11;

    /**
     * @param isHasAudio
     * @param isHasVideo
     * @return
     */
    public static byte[] warpFlvHeader(boolean isHasAudio, boolean isHasVideo) {
        /**
         *  Flv Header在当前版本中总是由9个字节组成。
         *  第1-3字节为文件标识（Signature），总为“FLV”（0x46 0x4C 0x56），如图中紫色区域。
         *  第4字节为版本，目前为1（0x01）。
         *  第5个字节的前5位保留，必须为0。
         *  第5个字节的第6位表示是否存在音频Tag。
         *  第5个字节的第7位保留，必须为0。
         *  第5个字节的第8位表示是否存在视频Tag。
         *  第6-9个字节为UI32类型的值，表示从File Header开始到File Body开始的字节数，版本1中总为9。
         */
        //Signature
        byte[] flvHeader = new byte[9];
        flvHeader[0] = 'F';
        flvHeader[1] = 'L';
        flvHeader[2] = 'V';
        //version
        flvHeader[3] = 0x1;
        //5byte
        int videoTag = isHasVideo ? 1 : 0;
        int audioTag = isHasAudio ? 4 : 0;
        flvHeader[4] = (byte) (audioTag | videoTag);
        //data offset
        flvHeader[5] = 0;
        flvHeader[6] = 0;
        flvHeader[7] = 0;
        flvHeader[8] = 0x9;
        return flvHeader;
    }

    public static byte[] warpFLVBodyOfTagHeader(int type, int dataSize, int time) {
        /**
         * 第1个byte为记录着tag的类型，音频（0x8），视频（0x9），脚本（0x12）；
         * 第2-4bytes是数据区的长度，UI24类型的值，也就是tag data的长度；注：这个长度等于最后的Tag Size-11
         * 第5-7个bytes是时间戳，UI24类型的值，单位是毫秒，类型为0x12脚本类型数据，则时间戳为0，时间戳控制着文件播放的速度，可以根据音视频的帧率类设置；
         * 第8个byte是扩展时间戳，当24位数值不够时，该字节作为最高位将时间戳扩展为32位值；
         * 第9-11个bytes是streamID，UI24类型的值，但是总为0；
         * tag header 长度为1+3+3+1+3=11。
         */
        byte[] tagHeader = new byte[11];
        //type
        tagHeader[0] = (byte) (type & 0xFF);
        //data size
        tagHeader[1] = (byte) ((dataSize >> 16) & 0xFF);
        tagHeader[2] = (byte) ((dataSize >> 8) & 0xFF);
        tagHeader[3] = (byte) (dataSize & 0xFF);
        //time stamp
        tagHeader[4] = (byte) ((time >> 16) & 0xFF);
        tagHeader[5] = (byte) ((time >> 8) & 0xFF);
        tagHeader[6] = (byte) (time & 0xFF);
        //extend time stamp
        tagHeader[7] = (byte) ((time << 24) & 0xFF);
        //stream id
        tagHeader[8] = 0;
        tagHeader[9] = 0;
        tagHeader[10] = 0;
        return tagHeader;
    }

    /**
     * 封装flv 视频头信息
     * 4 bit Frame Type  ------ 帧类型
     * 4 bit CodecID ------ 视频类型
     * 8 bit AVCPacketType ------ 是NALU 还是 sequence header
     * 24 bit CompositionTime ------ 如果为NALU 则为时间间隔，否则为0
     *
     * @param flvVideoFrameType 参见 class FlvVideoFrameType
     * @param codecID           参见 class FlvVideo
     * @param acvPacketType     参见 class FlvVideoAVCPacketType
     * @return
     */
    public static byte[] warpFLVBodyOfFixVideoTag(int flvVideoFrameType, int codecID, byte acvPacketType) {
        byte[] videoFixTag = new byte[5];
        videoFixTag[0] = (byte) (((flvVideoFrameType & 0x0F) << 4) | (codecID & 0x0F));
        videoFixTag[1] = (byte) acvPacketType;
        videoFixTag[2] = 0x00;
        videoFixTag[3] = 0x00;
        videoFixTag[4] = 0x00;
        return videoFixTag;
    }

    public static byte[] warpFLVBodyOfVideoTag(byte[] data, boolean isKeyFrame) {
        int flvVideoFrameType = 2;
        if (isKeyFrame) {
            flvVideoFrameType = 1;
        }

        byte[] bytes = warpFLVBodyOfFixVideoTag(flvVideoFrameType, 7, (byte) 1);

        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length + data.length);
        buffer.put(bytes);
        buffer.put(data);
        return buffer.array();
    }

    /**
     * 三位分 00 00 01
     * 四位分 00 00 00 01
     * 67 sps
     * 68 pps
     * 00 00 00 01 67 42 C0 29 8D 68 1E 05 12 01 E1 10 8D 40
     * 00 00 00 01 68 CE 01 A8 35 C8
     * <p>
     * 第一个视频Tag，需要写入AVC视频流的configuretion信息，这个信息根据pps、sps生成
     * 8 bit configuration version ------ 版本号
     * 8 bit AVC Profile Indication ------- sps[1]
     * 8 bit Profile Compatibility ------- sps[2]
     * 8 bit AVC Level Compatibility ------- sps[3]
     * 6 bit Reserved ------- 111111
     * 2 bit Length Size Minus One ------- NAL Unit Length长度为－1，一般为3
     * 3 bit Reserved ------- 111
     * 5 bit Num of Sequence Parameter Sets ------- sps个数，一般为1
     * ? bit Sequence Parameter Set NAL Units ------- （sps_size + sps）的数组
     * 8 bit Num of Picture Parameter Sets ------- pps个数，一般为1
     * ? bit Picture Parameter Set NAL Units ------- （pps_size + pps）的数组
     *
     * @param sps
     * @param pps
     * @return
     */
    public static byte[] warpFLVBodyOfVideoFirstData(int flvVideoFrameType, int codecID, byte acvPacketType, byte[] sps, byte[] pps) {
        byte[] videoFixData = warpFLVBodyOfFixVideoTag(flvVideoFrameType, codecID, acvPacketType);
        int fixLen = videoFixData.length;
        //
        int len = sps.length + pps.length + fixLen+11;

        byte[] tagData = new byte[len];

        System.arraycopy(videoFixData, 0, tagData, 0, fixLen);
        int offset = fixLen;
        //version
        tagData[offset++] = 0x01;
        //sps编码格式
        tagData[offset++] = sps[1];
        tagData[offset++] = sps[2];
        tagData[offset++] = sps[3];
        //NALU 长度
        tagData[offset++] = (byte) 0xFF;
        //sps个数
        tagData[offset++] = (byte) 0xE1;
        //sps长度
        short spsLen = (short) sps.length;
        tagData[offset++] = (byte) ((spsLen >> 8) & 0xFF);
        tagData[offset++] = (byte) (spsLen & 0xFF);
        //sps数据
        System.arraycopy(sps, 0, tagData, offset, spsLen);
        offset += spsLen;
        //pps 个数
        tagData[offset++] = (byte) (0x01);
        short ppsLen = (short) pps.length;
        //pps 长度
        tagData[offset++] = (byte) ((ppsLen >> 8) & 0xFF);
        tagData[offset++] = (byte) (ppsLen & 0xFF);
        //pps 数据
        System.arraycopy(pps, 0, tagData, offset, ppsLen);
        return tagData;
    }

    /**
     * 写Audio Header信息
     * soundFormat 声音类型 参见 FlvAudio 4b
     * soundRate 声音采样频率 参加 FlvAudioSampleRate 2b
     * soundSize 声音采样大小 参加 FlvAudioSampleSize 0 8bit 1 16bit 1b
     * soundType 声音的类别 参加 FlvAudioSampleType  0 单 1 双 1b
     * AACPacketType 0 ＝ AAC sequence header   1 = AAC raw
     *
     * @return
     */
    public static byte[] warpFLVBodyOfFixAudioTag(boolean isFirst, int audioSize) {
        byte[] audioFix = new byte[2];

        int soundFormat = FlvAudio.AAC;
        int soundRate = FlvAudioRate.KHZ_44;
        int soundSize = audioSize / 8 - 1;
        int soundType = 1;
        audioFix[0] = (byte) (((byte)(soundFormat & 0x0F) << 4) | ((byte)(soundRate & 0x03) << 2) | ((byte)(soundSize & 0x01) << 1) | (byte)(soundType & 0x01));
        //AACPacketType
        if (isFirst) {
            audioFix[1] = 0x0;
        } else {
            audioFix[1] = 0x1;
        }
        return audioFix;
    }

    public class FlvAudio {
        public final static int LINEAR_PCM = 0;
        public final static int AD_PCM = 1;
        public final static int MP3 = 2;
        public final static int LINEAR_PCM_LE = 3;
        public final static int NELLYMOSER_16_MONO = 4;
        public final static int NELLYMOSER_8_MONO = 5;
        public final static int NELLYMOSER = 6;
        public final static int G711_A = 7;
        public final static int G711_MU = 8;
        public final static int RESERVED = 9;
        public final static int AAC = 10;
        public final static int SPEEX = 11;
        public final static int MP3_8 = 14;
        public final static int DEVICE_SPECIFIC = 15;
    }

    public class FlvAudioRate {
        public static final int KHZ_55 = 0;
        public static final int KHZ_11 = 1;
        public static final int KHZ_22 = 2;
        public static final int KHZ_44 = 3;
    }

}
