package com.num.model;

import com.num.controller.services.LocalVpnService;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpOut implements Runnable {
    private static final String TAG = TcpOut.class.getSimpleName();

    private LocalVpnService localVpnService;
    private ConcurrentLinkedQueue<Packet> inputQueue;
    private ConcurrentLinkedQueue<ByteBuffer> outputQueue;
    private Selector selector;
    private Random random = new Random();

    public TcpOut(LocalVpnService localVpnService, ConcurrentLinkedQueue<Packet> inputQueue,
                  ConcurrentLinkedQueue<ByteBuffer> outputQueue, Selector selector) {
        this.localVpnService = localVpnService;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            Thread currentThread = Thread.currentThread();
            while (true) {
                Packet currentPacket;
                do {
                    currentPacket = inputQueue.poll();
                    if (currentPacket != null) break;
                    Thread.sleep(10);
                } while (!currentThread.isInterrupted());
                if (currentThread.isInterrupted()) break;
                ByteBuffer payload = currentPacket.backup;
                currentPacket.backup = null;
                ByteBuffer respBuffer = ByteBufferPool.acquire();
                InetAddress dstAddr = currentPacket.ipv4Header.dstAddr;
            }
        } catch (InterruptedException e) {

        }
    }
}
