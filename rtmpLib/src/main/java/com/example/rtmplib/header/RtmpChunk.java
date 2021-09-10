package com.example.rtmplib.header;


import com.example.rtmplib.IBinary;

public class RtmpChunk implements IBinary {

    private ChunkHeader chunkHeader;
    private byte[] data;

    private byte[] binaryData;


    private RtmpChunk() {
    }

    private RtmpChunk(ChunkHeader chunkHeader, byte[] data) {
        this.chunkHeader = chunkHeader;
        this.data = data;
    }

    public void setChunkHeader(ChunkHeader chunkHeader) {
        this.chunkHeader = chunkHeader;
    }

    public ChunkHeader getChunkHeader() {
        return chunkHeader;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] toBinary() {
        if (binaryData == null) {
            byte[] headerBytes = chunkHeader.toBinary();
            binaryData = new byte[headerBytes.length + data.length];
            System.arraycopy(headerBytes, 0, binaryData, 0, headerBytes.length);
            System.arraycopy(data, 0, binaryData, headerBytes.length, data.length);
        }
        return binaryData;
    }

    public static class Builder {

        private BasicHeader basicHeader;
        private ChunkHeader chunkHeader;
        private byte[] chunkData;

        public Builder() {
        }

        public Builder setBasicHeader(BasicHeader basicHeader) {
            this.basicHeader = basicHeader;
            return this;
        }

        public Builder setBasicHeader(int fmt) {
            basicHeader = new BasicHeader(fmt);
            return this;
        }

        public Builder setBasicHeader(int fmt, int csId) {
            basicHeader = new BasicHeader(fmt, csId);
            return this;
        }

        public Builder setChunkHeader(ChunkHeader chunkHeader) {
            this.chunkHeader = chunkHeader;
            return this;
        }

        public Builder setChunkHeader(int timestamp3Bytes, int messageLength3Bytes, int messageTypeId1Bytes, int msgStreamId4Bytes) {
            this.chunkHeader = new ChunkHeader(timestamp3Bytes, messageLength3Bytes, messageTypeId1Bytes, msgStreamId4Bytes);
            return this;
        }

        public Builder setChunkHeader(int timestamp3Bytes, int messageLength3Bytes, int messageTypeId1Bytes) {
            this.chunkHeader = new ChunkHeader(timestamp3Bytes, messageLength3Bytes, messageTypeId1Bytes);
            return this;
        }

        public Builder setChunkHeader(int timestamp3Bytes) {
            this.chunkHeader = new ChunkHeader(timestamp3Bytes);
            return this;
        }

        public Builder setChunkData(byte[] chunkData) {
            this.chunkData = chunkData;
            return this;
        }


        public RtmpChunk build() {
            if (basicHeader == null || chunkHeader == null) {
                return null;
            }
            chunkHeader.setBasicHeader(basicHeader);
            return new RtmpChunk(chunkHeader, chunkData);
        }
    }
}
