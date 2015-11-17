package com.num.model;

import com.num.controller.utils.BitUtil;

import java.nio.ByteBuffer;

public class Packet {
    public static final int IPV4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 4;

    private class TcpHeader {
        public static final int FIN = 0x01;
        public static final int SYN = 0x02;
        public static final int RST = 0x04;
        public static final int PSH = 0x08;
        public static final int ACK = 0x10;
        public static final int URG = 0x20;

        private int srcPort;
        private int dstPort;
        private long seqNum;
        private long ackNum;

        private TcpHeader(ByteBuffer byteBuffer) {
            this.srcPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.dstPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.seqNum = BitUtil.getUnsignedInt(byteBuffer.getInt());
            this.ackNum = BitUtil.getUnsignedInt(byteBuffer.getInt());
            
        }
    }
}
