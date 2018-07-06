package io.reactiverse.pgclient.shared;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface Buffer {

    static Buffer buffer(byte[] bytes) {
        return new ByteBufBuffer(Unpooled.unreleasableBuffer(Unpooled.buffer(bytes.length, 2147483647)).writeBytes(bytes));
    }

    static Buffer buffer(String s) {
        return buffer(s.getBytes(StandardCharsets.UTF_8));
    }

    static Buffer buffer(ByteBuf buf) {
        return new ByteBufBuffer(buf);
    }

    byte getByte(int i);

    int length();

    ByteBuf getByteBuf();

    class ByteBuffer implements Buffer {
        final byte[] value;

        public ByteBuffer(byte[] value) {
            this.value = value;
        }

        @Override
        public byte getByte(int i) {
            return value[i];
        }

        @Override
        public int length() {
            return value.length;
        }

        @Override
        public ByteBuf getByteBuf() {
            UnpooledHeapByteBuf buf = new UnpooledHeapByteBuf(new UnpooledByteBufAllocator(false), length(), length());
            buf.setBytes(0, value);
            return buf;
        }

        @Override
        public String toString() {
            return new String(value, StandardCharsets.UTF_8);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ByteBuffer that = (ByteBuffer) o;

            return Arrays.equals(value, that.value);
        }
    }

    class ByteBufBuffer implements Buffer {
        final ByteBuf value;

        public ByteBufBuffer(ByteBuf value) {
            this.value = Unpooled.unreleasableBuffer(value);
        }

        @Override
        public byte getByte(int i) {
            return value.getByte(i);
        }

        @Override
        public int length() {
            return value.writerIndex();
        }

        @Override
        public ByteBuf getByteBuf() {
            return value.duplicate();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ByteBufBuffer that = (ByteBufBuffer) o;

            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
