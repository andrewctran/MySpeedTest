package com.num.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
        TransmissionControlBlock tcb = (TransmissionControlBlock) selectionKey.attachment();
        Packet packet = tcb.packet;
        try {
            if (tcb.socketChannel.finishConnect()) {
                selectionKeyIterator.remove();
                tcb.status = TransmissionControlBlock.TransmissionControlBlockStatus.SYN_RECEIVED;
                ByteBuffer responseBuffer = ByteBufferPool.acquire();
                packet.setTcpBuffer(responseBuffer, (byte) (Packet.TcpHeader.SYN | Packet.TcpHeader.ACK),
                        tcb.thisSeqNum, tcb.thisAckNum, 0);
                tcpOut.offer(responseBuffer);
                tcb.thisSeqNum++;
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            // Connection error
            ByteBuffer responseBuffer = ByteBufferPool.acquire();
            packet.setTcpBuffer(responseBuffer, (byte) Packet.TcpHeader.RST, 0, tcb.thisAckNum, 0);
            tcpOut.offer(responseBuffer);
            TransmissionControlBlock.close(tcb);
        }
    }

    private void handleInput(Iterator<SelectionKey> selectionKeyIterator, SelectionKey selectionKey) {
        TransmissionControlBlock tcb = (TransmissionControlBlock) selectionKey.attachment();
        selectionKeyIterator.remove();
        ByteBuffer recvBuffer = ByteBufferPool.acquire();
        recvBuffer.position(HEADER_SIZE);

        synchronized (tcb) {
            Packet packet = tcb.packet;
            SocketChannel inputChannel = (SocketChannel) selectionKey.channel();
            int readBytes;
            try {
                readBytes = inputChannel.read(recvBuffer);
            } catch (IOException e) {
                // Network read error
                packet.setTcpBuffer(recvBuffer, (byte) Packet.TcpHeader.RST, 0, tcb.thisAckNum, 0);
                tcpOut.offer(recvBuffer);
                TransmissionControlBlock.close(tcb);
                return;
            }
            if (readBytes == -1) {
                // EOS
                selectionKey.interestOps(0);
                tcb.isWaiting = false;
                if (tcb.status != TransmissionControlBlock.TransmissionControlBlockStatus.CLOSE_WAIT) {
                    ByteBufferPool.release(recvBuffer);
                    return;
                }
                tcb.status = TransmissionControlBlock.TransmissionControlBlockStatus.LAST_ACK;
                packet.setTcpBuffer(recvBuffer, (byte) Packet.TcpHeader.FIN, tcb.thisSeqNum, tcb.thisAckNum, 0);
                tcb.thisSeqNum++;
            } else {
                packet.setTcpBuffer(recvBuffer, (byte) (Packet.TcpHeader.PSH | Packet.TcpHeader.ACK), tcb.thisSeqNum, tcb.thisAckNum, readBytes);
                tcb.thisSeqNum += readBytes;
                recvBuffer.position(HEADER_SIZE + readBytes);
            }
        }
        tcpOut.offer(recvBuffer);
    }
}
