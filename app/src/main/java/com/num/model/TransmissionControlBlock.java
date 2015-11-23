package com.num.model;

import android.util.LruCache;

import com.num.controller.utils.SynchronizedLruCache;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class TransmissionControlBlock {
    private static final int MAX_CACHE_SIZE = 50;
    private static LruCache<String, TransmissionControlBlock> cache =
            new SynchronizedLruCache<String, TransmissionControlBlock>(MAX_CACHE_SIZE, new SynchronizedLruCache.RemovalCallback() {
                @Override
                public void remove(Map.Entry<String, TransmissionControlBlock> eldest) {
                    eldest.getValue().closeChannel();
                }
            }
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

    private void closeChannel() {
        try {
            socketChannel.close();
        } catch (IOException e) {

        }
    }




}
