//
// Created by Administrator on 2021/9/28.
//

#ifndef RTMP_MEDIA_SAFE_QUEUE_H
#define RTMP_MEDIA_SAFE_QUEUE_H


#include <mutex>
#include <condition_variable>
#include <queue>
#include "log.h"

template<typename T>
class SafeQueue {
    //不知道函数指针是什么类型，使用free还是delete，只能回调给用户释放
    typedef void  (*ReleaseCallback)(T value);

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    std::queue<T> que;
    int flag = 0;
    ReleaseCallback releaseCallback;

public:
    SafeQueue() {
        pthread_mutex_init(&mutex, nullptr);
        pthread_cond_init(&cond, nullptr);
    }

    ~SafeQueue() {
        pthread_mutex_unlock(&mutex);
        pthread_cond_destroy(&cond);
        flag = 0;
    }

    bool push(T value) {
        pthread_mutex_lock(&mutex);
        if (flag == 0) {
            LOGE("flag is 0");
            return false;
        }
        que.push(value);
        pthread_cond_signal(&cond);

        pthread_mutex_unlock(&mutex);
        return true;
    }

    bool pop(T &value) {
        pthread_mutex_lock(&mutex);
        while (flag && que.empty()) {
            pthread_cond_wait(&cond, &mutex);
        }
        if (!que.empty()) {
            value = que.front();
            que.pop();
        }
        pthread_mutex_unlock(&mutex);
        return true;
    }

    int size() const {
        return que.size();
    }

    bool empty() const {
        return que.empty();
    }

    void clear() {
        pthread_mutex_lock(&mutex);
        T value;
        while (pop(value)) {
            if (releaseCallback) {
                releaseCallback(value);
            }
        }
        pthread_mutex_unlock(&mutex);
    }

    void setFlag(int i) {
        pthread_mutex_lock(&mutex);
        flag = i;
        pthread_mutex_unlock(&mutex);
    }

    void setReleaseCallback(ReleaseCallback callback) {
        releaseCallback = callback;
    }

};

#endif //RTMP_MEDIA_SAFE_QUEUE_H
