package com.num.model;

import android.util.LruCache;

import com.num.controller.utils.SynchronizedLruCache;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

public class TransmissionControlBlock {
    private static final int MAX_CACHE_SIZE = 50;
    private static SynchronizedLruCache<String, TransmissionControlBlock> cache =
            new SynchronizedLruCache<>(MAX_CACHE_SIZE,
                    new SynchronizedLruCache.RemovalCallback<String, TransmissionControlBlock>() {
        @Override
        public void remove(Map.Entry<String, TransmissionControlBlock> eldest) {
            eldest.getValue().closeChannel();
        }
    });
    public String ipPort;
    public long thisSeqNum;
    public long otherSeqNum;
    public long thisAckNum;
    public long otherAckNum;
    public Packet packet;
    public TransmissionControlBlockStatus status;

    public SocketChannel socketChannel;
    public SelectionKey selectionKey;
    public boolean isWaiting;

    public enum TransmissionControlBlockStatus
    {
        SYN_SENT,
        SYN_RECEIVED,
        ESTABLISHED,
        CLOSE_WAIT,
        LAST_ACK,
    }

    public TransmissionControlBlock(String ipPort, long thisSeqNum, long otherSeqNum, long thisAckNum, long otherAckNum,
                                    Packet packet, SocketChannel socketChannel) {
        this.ipPort = ipPort;
        this.thisSeqNum = thisSeqNum;
        this.otherSeqNum = otherSeqNum;
        this.thisAckNum = thisAckNum;
        this.otherSeqNum = otherAckNum;
        this.packet = packet;
        this.socketChannel = socketChannel;
    }

    public static void close(TransmissionControlBlock tcb) {
        tcb.closeChannel();
        synchronized (cache) {
            cache.remove(tcb.ipPort);
        }
    }

    public static void closeAll() {
        synchronized (cache) {
            Iterator<Map.Entry<String, TransmissionControlBlock>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next().getValue().closeChannel();
                iterator.remove();
            }
        }
    }

    private void closeChannel() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // Do nothing.
        }
    }

    public static TransmissionControlBlock get(String key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    public static void put(String key, TransmissionControlBlock tcb) {
        synchronized (cache) {
            cache.put(key, tcb);
        }
    }
}
