//
// Created by Administrator on 2021/9/28.
//

#include "include/VideoChannel.h"

VideoChannel::VideoChannel() {

}

VideoChannel::~VideoChannel() {

}

void VideoChannel::warpH264(int8_t *data, int32_t len) {
    RTMPPacket *packet = nullptr;
    packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, len);
    RTMPPacket_Reset(packet);
    //视频
    packet->m_nChannel = 0x4;
    memcpy(packet->m_body, data, len);

    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_hasAbsTimestamp = FALSE;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = len;

    if (videoCallback) {
        videoCallback(packet);
    }
}
