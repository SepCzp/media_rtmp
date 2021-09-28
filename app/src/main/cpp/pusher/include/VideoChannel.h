//
// Created by Administrator on 2021/9/28.
//

#ifndef RTMP_MEDIA_VIDEOCHANNEL_H
#define RTMP_MEDIA_VIDEOCHANNEL_H

#include "../util/safe_queue.h"
#include "../include/rtmp.h"

class VideoChannel {

    //数据回调
    typedef void (*VideoCallback)(void *);

private:
    VideoCallback videoCallback;


public:
    VideoChannel();

    ~VideoChannel();

    void warpH264(int8_t *data,int32_t len);

    void setVideoCallback(VideoCallback callback) {
        videoCallback = callback;
    }

};


#endif //RTMP_MEDIA_VIDEOCHANNEL_H
