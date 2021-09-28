//
// Created by Administrator on 2021/9/28.
//

#ifndef RTMP_MEDIA_RTMPCALLBACK_H
#define RTMP_MEDIA_RTMPCALLBACK_H

#include <jni.h>
#include "../util/log.h"

#define THREAD_MAIN 1   // 此宏代表 主线程的意思
#define THREAD_CHILD 2  // 此宏代表 子线程的意思

/**
 * 回调java层
 */
class RtmpCallback {

private:
    JavaVM *javaVm;
    JNIEnv *env;
    jobject obj;
    jmethodID connectId;
    jmethodID errorId;
    jmethodID successId;

public:
    RtmpCallback(JavaVM *jvm, JNIEnv *env, jobject object);

    ~RtmpCallback();

    void onConnectCallback(int thread_mode);

    void onConnectSucCallback(int thread_mode);

    void onErrorCallback(int thread_mode, int code);
};


#endif //RTMP_MEDIA_RTMPCALLBACK_H
