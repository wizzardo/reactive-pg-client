package io.reactiverse.pgclient.shared;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface Buffer {

    static Buffer buffer(byte[] bytes) {
//        return new ByteBufBuffer(Unpooled.unreleasableBuffer(Unpooled.buffer(bytes.length, 2147483647)).writeBytes(bytes));
        return new ByteBuffer(bytes);
    }

    static Buffer buffer(String s) {
        return buffer(s.getBytes(StandardCharsets.UTF_8));
    }

    static Buffer buffer(ByteBuf buf) {
        int length = buf.readableBytes();
        byte[] bytes = new byte[length];
        buf.getBytes(buf.readerIndex(), bytes);
//        return new ByteBufBuffer(buf);
        return new ByteBuffer(bytes);
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
            return Unpooled.unreleasableBuffer(Unpooled.buffer(value.length, 2147483647)).writeBytes(value);
        }

        @Override
        public String toString() {
//            return new String(value, StandardCharsets.UTF_8);
            return length() + ":" + Arrays.toString(value);
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
