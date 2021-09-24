package com.example.rtmp_media;

import java.io.Serializable;

public class VideoInfo implements Serializable {

    private int width;
    private int height;
    private byte[] data;

    public VideoInfo() {
    }

    public VideoInfo(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
