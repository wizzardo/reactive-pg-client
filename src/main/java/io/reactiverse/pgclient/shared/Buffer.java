package io.reactiverse.pgclient.shared;

import io.netty.buffer.ByteBuf;

public interface Buffer {

    static Buffer buffer(byte[] decodeHexStringToBytes) {
        throw new IllegalStateException("Not implemented yet");
    }

    static Buffer buffer(ByteBuf readBytes) {
        throw new IllegalStateException("Not implemented yet");
    }

    byte getByte(int var1);

    int length();

    ByteBuf getByteBuf();
}
