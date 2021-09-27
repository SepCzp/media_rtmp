#include <jni.h>
#include <android/log.h>
#include <rtmp.h>
#include <unistd.h>
#include <stdio.h>
#include <log.h>
#include <malloc.h>
#include <pthread.h>
#include <mutex>
#include <queue>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"RTMP",__VA_ARGS__)

#define HTON16(x)  ((x>>8&0xff)|(x<<8&0xff00))
#define HTON24(x)  ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00))
#define HTON32(x)  ((x>>24&0xff)|(x>>8&0xff00)|\
    (x<<8&0xff0000)|(x<<24&0xff000000))
#define HTONTIME(x) ((x>>16&0xff)|(x<<16&0xff0000)|(x&0xff00)|(x&0xff000000))


/*read 1 byte*/
int ReadU8(uint32_t *u8, FILE *fp) {
    if (fread(u8, 1, 1, fp) != 1)
        return 0;
    return 1;
}

/*read 2 byte*/
int ReadU16(uint32_t *u16, FILE *fp) {
    if (fread(u16, 2, 1, fp) != 1)
        return 0;
    *u16 = HTON16(*u16);
    return 1;
}

/*read 3 byte*/
int ReadU24(uint32_t *u24, FILE *fp) {
    if (fread(u24, 3, 1, fp) != 1)
        return 0;
    *u24 = HTON24(*u24);
    return 1;
}

/*read 4 byte*/
int ReadU32(uint32_t *u32, FILE *fp) {
    if (fread(u32, 4, 1, fp) != 1)
        return 0;
    *u32 = HTON32(*u32);
    return 1;
}

/*read 1 byte,and loopback 1 byte at once*/
int PeekU8(uint32_t *u8, FILE *fp) {
    if (fread(u8, 1, 1, fp) != 1)
        return 0;
    fseek(fp, -1, SEEK_CUR);
    return 1;
}

/*read 4 byte and convert to time format*/
int ReadTime(uint32_t *utime, FILE *fp) {
    if (fread(utime, 4, 1, fp) != 1)
        return 0;
    *utime = HTONTIME(*utime);
    return 1;
}

RTMP *rtmp1 = nullptr;
bool isConnect = false;
pthread_mutex_t mutex;
std::queue<RTMPPacket *> packets;
pthread_t push_thr;
uint32_t start_time = 0;


void *onPush(void *args) {
    RTMPPacket *packet = 0;
    while (true) {

        if (packets.empty()) {
            continue;
        }
        packet = packets.front();
        packets.pop();

        packet->m_nInfoField2 = rtmp1->m_stream_id;
        packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        int ret = RTMP_SendPacket(rtmp1, packet, 1);
        if (!ret) {
            LOGE("发送失败");
            break;
        } else {
            RTMPPacket_Free(packet);
            LOGE("发送成功");
        }
    }

    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = 0;
    }

    if (rtmp1) {
        RTMP_DeleteStream(rtmp1);
        RTMP_Close(rtmp1);
        RTMP_Free(rtmp1);
        rtmp1 = 0;
        LOGE("释放 native 资源");
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_rtmp_1media_RtmpNative_RtmpConnect1(JNIEnv *env, jclass clazz) {

    pthread_mutex_init(&mutex, nullptr);
    //init rtmp
    rtmp1 = RTMP_Alloc();
    RTMP_Init(rtmp1);
    rtmp1->Link.timeout = 5;
    if (!RTMP_SetupURL(rtmp1, "rtmp://192.168.88.27/live/test")) {
        RTMP_Log(RTMP_LOGERROR, "SetupURL err\n");
        RTMP_Free(rtmp1);
        return;
    }

    //if unable,the AMF command would be 'play' instead of 'publish'
    RTMP_EnableWrite(rtmp1);

    if (!RTMP_Connect(rtmp1, nullptr)) {
//        RTMP_Log(RTMP_LOGERROR, "rtmp connect err\n");
        LOGE("rtmp connect err\n");
        RTMP_Close(rtmp1);
        RTMP_Free(rtmp1);
        return;
    }

    if (!RTMP_ConnectStream(rtmp1, 0)) {
        LOGE("ConnectStream Err\n");
//        RTMP_Log(RTMP_LOGERROR, "ConnectStream Err\n");
        RTMP_Close(rtmp1);
        RTMP_Free(rtmp1);
        return;
    }
    isConnect = true;
    start_time = RTMP_GetTime();

    LOGE("isConnect：success");
    pthread_create(&push_thr, nullptr, onPush, nullptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_rtmp_1media_RtmpNative_RtmpConnect(JNIEnv *env, jclass clazz, jstring path) {

    const char *_path = env->GetStringUTFChars(path, nullptr);
    FILE *fp = fopen(_path, "rb");
    if (!fp) {
        LOGE("open file err");
        return;
    }

    RTMP *rtmp = nullptr;
    RTMPPacket *packet = nullptr;
    uint32_t start_time = 0;
    uint32_t now_time = 0;
    //the timestamp of the previous frame
    long pre_frame_time = 0;
    long lasttime = 0;
    int bNextIsKey = 1;
    uint32_t preTagsize = 0;

    //packet attributes
    uint32_t type = 0;
    uint32_t datalength = 0;
    uint32_t timestamp = 0;
    uint32_t streamid = 0;
    //init rtmp
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;
    if (!RTMP_SetupURL(rtmp, "rtmp://192.168.88.27/live/test")) {
        RTMP_Log(RTMP_LOGERROR, "SetupURL err\n");
        RTMP_Free(rtmp);
        return;
    }

    //if unable,the AMF command would be 'play' instead of 'publish'
    RTMP_EnableWrite(rtmp);

    if (!RTMP_Connect(rtmp, nullptr)) {
        RTMP_Log(RTMP_LOGERROR, "rtmp connect err\n");
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        return;
    }

    if (!RTMP_ConnectStream(rtmp, 0)) {
        RTMP_Log(RTMP_LOGERROR, "ConnectStream Err\n");
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        return;
    }
    packet = static_cast<RTMPPacket *>(malloc(sizeof(packet)));
    RTMPPacket_Alloc(packet, 1024 * 64);
    RTMPPacket_Reset(packet);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04; //视频
    packet->m_nInfoField2 = rtmp->m_stream_id;

    //jump over FLV Header
    fseek(fp, 9, SEEK_SET);
    //jump over previousTagSizen
    fseek(fp, 4, SEEK_CUR);
    start_time = RTMP_GetTime();
    while (1) {
        if (feof(fp)) {
            fseek(fp, 0, SEEK_SET);
        }
        //not quite the same as FLV spec
        if (!ReadU8(&type, fp))
            break;
        if (!ReadU24(&datalength, fp))
            break;
        if (!ReadTime(&timestamp, fp))
            break;
        if (!ReadU24(&streamid, fp))
            break;

        if (type != 0x08 && type != 0x09) {
            //jump over non_audio and non_video frame，
            //jump over next previousTagSizen at the same time
            fseek(fp, datalength + 4, SEEK_CUR);
            continue;
        }

        if (fread(packet->m_body, 1, datalength, fp) != datalength)
            break;

        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        packet->m_nTimeStamp = timestamp;
        packet->m_packetType = type;
        packet->m_nBodySize = datalength;
        pre_frame_time = timestamp;
        long delt = RTMP_GetTime() - start_time;
        printf("%ld,%ld\n", pre_frame_time, (RTMP_GetTime() - start_time));
        __android_log_print(ANDROID_LOG_WARN, "eric",
                            "%ld,%ld", pre_frame_time, (RTMP_GetTime() - start_time));
        if (delt < pre_frame_time) {
            usleep((pre_frame_time - delt) * 1000);
        }
        if (!RTMP_IsConnected(rtmp)) {
            RTMP_Log(RTMP_LOGERROR, "rtmp is not connect\n");
            break;
        }
        if (!RTMP_SendPacket(rtmp, packet, 0)) {
            RTMP_Log(RTMP_LOGERROR, "Send Error\n");
            break;
        }

        if (!ReadU32(&preTagsize, fp))
            break;

        if (!PeekU8(&type, fp))
            break;
        if (type == 0x09) {
            if (fseek(fp, 11, SEEK_CUR) != 0)
                break;
            if (!PeekU8(&type, fp)) {
                break;
            }
            if (type == 0x17)
                bNextIsKey = 1;
            else
                bNextIsKey = 0;

            fseek(fp, -11, SEEK_CUR);
        }
    }

    RTMP_LogPrintf("\nSend Data Over\n");

    if (fp)
        fclose(fp);

    if (rtmp != NULL) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = NULL;
    }
    if (packet != NULL) {
        RTMPPacket_Free(packet);
        free(packet);
        packet = NULL;
    }

    env->ReleaseStringUTFChars(path, _path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_rtmp_1media_RtmpNative_pushVideo(JNIEnv *env, jclass clazz, jbyteArray data,
                                                  jint type, jint timestamp) {
    if (!isConnect) {
        return;
    }
    jbyte *video = env->GetByteArrayElements(data, JNI_FALSE);
    int len = env->GetArrayLength(data);

    RTMPPacket *packet = nullptr;
    packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, len);
    RTMPPacket_Reset(packet);
    //视频
    packet->m_nChannel = 0x4;
    memcpy(packet->m_body, video, len);

    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_hasAbsTimestamp = FALSE;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = len;

    packets.push(packet);

    env->ReleaseByteArrayElements(data,video,JNI_FALSE);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_rtmp_1media_RtmpNative_pushAudio(JNIEnv *env, jclass clazz, jbyteArray data,
                                                  jint type, jint timestamp) {

    if (!isConnect) {
        return;
    }
    jbyte *audio = env->GetByteArrayElements(data, JNI_FALSE);
    int len = env->GetArrayLength(data);

    RTMPPacket *packet = nullptr;
    packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, len);
    RTMPPacket_Reset(packet);
    //音频
    packet->m_nChannel = 0x5;
    memcpy(packet->m_body, audio, len);

    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_hasAbsTimestamp = FALSE;
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = len;

    packets.push(packet);
    env->ReleaseByteArrayElements(data,audio,JNI_FALSE);

}