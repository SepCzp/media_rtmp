package com.example.rtmp_media.codec;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

public class MediaCodecHelper {


    public static MediaCodecInfo selectCodec(String mimeType) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] infos = mediaCodecList.getCodecInfos();
        for (int i = 0; i < infos.length; i++) {
            MediaCodecInfo info = infos[i];
            if (!info.isEncoder()) {
                break;
            }
            String[] supportedTypes = info.getSupportedTypes();
            for (int j = 0; j < supportedTypes.length; j++) {
                String supportedType = supportedTypes[j];
                if (supportedType.equalsIgnoreCase(mimeType)) {
                    return info;
                }
            }
        }
        return null;
    }
}
