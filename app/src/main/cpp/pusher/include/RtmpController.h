//
// Created by Administrator on 2021/9/28.
//

#ifndef RTMP_MEDIA_RTMPCONTROLLER_H
#define RTMP_MEDIA_RTMPCONTROLLER_H

#include "RtmpCallback.h"
#include "VideoChannel.h"
#include "../util/log.h"
#include "../util/safe_queue.h"
#include <pthread.h>

#define URI_ERROR 0
#define CONNECT_ERROR 1
#define CONNECT_STREAM_ERROR 2

class RtmpController {
private:
    RtmpCallback *rtmpCallback;
    VideoChannel *videoChannel;

    pthread_t conn_thr;

public:
    //connect
    bool isConnect = false;
    //
    bool isReadPush = false;
    //rtmp 链接
    char *url;
    long start_time;
    RTMP *rtmp;
    SafeQueue<RTMPPacket *> *queue = nullptr;

public:
    RtmpController(JavaVM *javaVm, JNIEnv *env, jobject object);

    ~RtmpController();

    void connect(char *url);

    void _connect();

    void onPush();

    void release();

    void warpH264(int8_t *data, int len);
};


#endif //RTMP_MEDIA_RTMPCONTROLLER_H
