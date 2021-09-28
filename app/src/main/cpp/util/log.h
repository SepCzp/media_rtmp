//
// Created by Administrator on 2021/9/28.
//

#ifndef RTMP_MEDIA_LOG_H
#define RTMP_MEDIA_LOG_H

#include <android/log.h>

#define NATIVE_LOG "NATIVE_LOG"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,NATIVE_LOG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,NATIVE_LOG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,NATIVE_LOG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,NATIVE_LOG,__VA_ARGS__)

#endif //RTMP_MEDIA_LOG_H
