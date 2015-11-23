package com.num.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpIn implements Runnable {
    private static final String TAG = TcpIn.class.getSimpleName();
    private static final int HEADER_SIZE = Packet.IPV4_HEADER_SIZE + Packet.TCP_HEADER_SIZE;

    private ConcurrentLinkedQueue<ByteBuffer> tcpOut;
    private Selector selector;

    public TcpIn(ConcurrentLinkedQueue<ByteBuffer> tcpOut, Selector selector) {
        this.tcpOut = tcpOut;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                int numChannelsReady = selector.select();
                if (numChannelsReady == 0) {
                    Thread.sleep(10);
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectedKeys.iterator();
                while (selectionKeyIterator.hasNext() && !Thread.interrupted()) {
                    SelectionKey key = selectionKeyIterator.next();
                    if (key.isValid()) {
                        if (key.isConnectable()) {
                            connect(selectionKeyIterator, key);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {

        } catch (IOException e) {

        }
    }

    private void connect(Iterator<SelectionKey> selectionKeyIterator, SelectionKey selectionKey) {

    }
}
