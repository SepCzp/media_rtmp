//
// Created by Administrator on 2021/9/28.
//

#include "include/RtmpCallback.h"

RtmpCallback::RtmpCallback(JavaVM *jvm, JNIEnv *env, jobject object) {
    javaVm = jvm;
    env = env;//只能主线程使用
    obj = env->NewGlobalRef(object);

    jclass jcls = env->GetObjectClass(object);

    connectId = env->GetMethodID(jcls, "onConnect",
                                 "()V");
    successId = env->GetMethodID(jcls, "onConnectSuccess",
                                 "()V");
    errorId = env->GetMethodID(jcls, "onError", "(I)V");
}

void RtmpCallback::onConnectCallback(int thread_mode) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(obj, connectId);
    } else {
        JNIEnv *jniEnv = nullptr;
        jint ok = javaVm->AttachCurrentThread(&jniEnv, 0);
        if (ok != JNI_OK) {
            LOGE("AttachCurrentThread fail");
            return;
        }
        jniEnv->CallVoidMethod(obj, connectId);
        javaVm->DetachCurrentThread();
    }
}

void RtmpCallback::onConnectSucCallback(int thread_mode) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(obj, successId);
    } else {
        JNIEnv *jniEnv = nullptr;
        jint ok = javaVm->AttachCurrentThread(&jniEnv, 0);
        if (ok != JNI_OK) {
            LOGE("AttachCurrentThread fail");
            return;
        }
        jniEnv->CallVoidMethod(obj, successId);
        javaVm->DetachCurrentThread();
    }
}

void RtmpCallback::onErrorCallback(int thread_mode, int code) {
    if (thread_mode == THREAD_MAIN) {
        env->CallVoidMethod(obj, errorId, code);
    } else {
        JNIEnv *jniEnv = nullptr;
        jint ok = javaVm->AttachCurrentThread(&jniEnv, 0);
        if (ok != JNI_OK) {
            LOGE("AttachCurrentThread fail");
            return;
        }
        jniEnv->CallVoidMethod(obj, errorId, code);
        javaVm->DetachCurrentThread();
    }
}

RtmpCallback::~RtmpCallback() {
    env->DeleteGlobalRef(obj);
    env = 0;
    javaVm = 0;
    obj = 0;
}
