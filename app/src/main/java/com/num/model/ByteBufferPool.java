package com.num.model;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ByteBufferPool {
    private static final int BUFFER_CAPACITY = 16384;
    private static ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();

    public static ByteBuffer acquire() {
        ByteBuffer byteBuffer = pool.poll();
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocateDirect(BUFFER_CAPACITY);
        }
        return byteBuffer;
    }

    public static void release(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        pool.offer(byteBuffer);
    }

    public static void clear() {
        pool.clear();
    }
}
