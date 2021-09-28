//
// Created by Administrator on 2021/9/28.
//

#include "./include/RtmpController.h"


static RtmpController *rtmpController = nullptr;

void *onConnectRunnable(void *context) {
    rtmpController = static_cast<RtmpController *>(context);
    if (rtmpController) {
        rtmpController->_connect();
    }
    return nullptr;
}

void releasePacket(RTMPPacket *packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = nullptr;
    }
}

void onDataCallback(void *packet) {
    if (packet) {
        if (rtmpController) {
            auto *rtmpPacket = static_cast<RTMPPacket *>(packet);
            rtmpPacket->m_nTimeStamp = RTMP_GetTime() - rtmpController->start_time;
            rtmpController->queue->push(rtmpPacket);
        }
    }
}

RtmpController::RtmpController(JavaVM *javaVm, JNIEnv *env, jobject object) {
    rtmpCallback = new RtmpCallback(javaVm, env, object);
    videoChannel = new VideoChannel();
    videoChannel->setVideoCallback(onDataCallback);
    queue = new SafeQueue<RTMPPacket *>();
    queue->setReleaseCallback(releasePacket);
}

void RtmpController::connect(char *rtmp_url) {
    if (isConnect) {
        return;
    }
    this->url = rtmp_url;
    pthread_create(&conn_thr, nullptr, onConnectRunnable, this);
}

void RtmpController::_connect() {
    //init rtmp
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;
    if (!RTMP_SetupURL(rtmp, url)) {
        LOGE("SetupURL err");
        RTMP_Free(rtmp);
        if (rtmpCallback) {
            rtmpCallback->onErrorCallback(THREAD_CHILD, URI_ERROR);
        }
        return;
    }

    //if unable,the AMF command would be 'play' instead of 'publish'
    RTMP_EnableWrite(rtmp);
    if(rtmpCallback){
        rtmpCallback->onConnectCallback(THREAD_CHILD);
    }
    if (!RTMP_Connect(rtmp, nullptr)) {
        LOGE("rtmp connect err\n");
        if (rtmpCallback) {
            rtmpCallback->onErrorCallback(THREAD_CHILD, CONNECT_ERROR);
        }
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        return;
    }

    if (!RTMP_ConnectStream(rtmp, 0)) {
        LOGE("ConnectStream Err\n");
        if (rtmpCallback) {
            rtmpCallback->onErrorCallback(THREAD_CHILD, CONNECT_STREAM_ERROR);
        }
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        return;
    }
    if(rtmpCallback){
        rtmpCallback->onConnectSucCallback(THREAD_CHILD);
    }
    start_time = RTMP_GetTime();
    isConnect = true;
    queue->setFlag(1);
    onPush();
}

void RtmpController::onPush() {
    RTMPPacket *packet = nullptr;
    while (isConnect) {
        if(!isReadPush){
            continue;
        }
        queue->pop(packet);
        if (!packet) {
            LOGE("获取失败");
            continue;
        }
        packet->m_nInfoField2 = rtmp->m_stream_id;
        int ret = RTMP_SendPacket(rtmp, packet, 1);
        if (!ret) {
            LOGE("发送失败");
        } else {
            LOGE("发送成功");
        }
        releasePacket(packet);
    }
}

RtmpController::~RtmpController() {
    if (rtmp) {
        isConnect = false;
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    if (rtmpCallback) {
        delete rtmpCallback;
    }
    if (videoChannel) {
        delete videoChannel;
    }
    if (queue) {
        queue->setFlag(0);
        queue->clear();
        delete queue;
    }

}

void RtmpController::warpH264(int8_t *data, int len) {
    if (videoChannel) {
        videoChannel->warpH264(data, len);
    }
}

void RtmpController::release() {

}

