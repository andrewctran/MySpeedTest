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

        // TCP header segments
        private int srcPort;
        private int dstPort;
        private long seqNum;
        private long ackNum;
        public byte offsetAndReserved;
        public int headerLength;
        public byte flags;
        public int window;
        public int checksum;
        public int urgentPointer;
        public byte[] optionsAndPadding;

        private TcpHeader(ByteBuffer byteBuffer) {
            this.srcPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.dstPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.seqNum = BitUtil.getUnsignedInt(byteBuffer.getInt());
            this.ackNum = BitUtil.getUnsignedInt(byteBuffer.getInt());
            this.offsetAndReserved = byteBuffer.get();
            this.headerLength = (this.offsetAndReserved & 0xF0) >> 2;
            this.flags = byteBuffer.get();
            this.window = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.checksum = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.urgentPointer = BitUtil.getUnsignedShort(byteBuffer.getShort());

            if (this.headerLength - TCP_HEADER_SIZE > 0) {
                optionsAndPadding = new byte[this.headerLength - TCP_HEADER_SIZE];
                byteBuffer.get(optionsAndPadding, 0, this.headerLength - TCP_HEADER_SIZE);
            }
        }

        public boolean isFIN()
        {
            return (flags & FIN) == FIN;
        }

        public boolean isSYN()
        {
            return (flags & SYN) == SYN;
        }

        public boolean isRST()
        {
            return (flags & RST) == RST;
        }

        public boolean isPSH()
        {
            return (flags & PSH) == PSH;
        }

        public boolean isACK()
        {
            return (flags & ACK) == ACK;
        }

        public boolean isURG()
        {
            return (flags & URG) == URG;
        }
    }
}
