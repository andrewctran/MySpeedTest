package com.num.model;

import com.num.controller.utils.BitUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Packet {
    public static final int IPV4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 4;

    public static class TcpHeader {
        public static final int FIN = 0x01;
        public static final int SYN = 0x02;
        public static final int RST = 0x04;
        public static final int PSH = 0x08;
        public static final int ACK = 0x10;
        public static final int URG = 0x20;

        // TCP header segments
        public int srcPort;
        public int dstPort;
        public long seqNum;
        public long ackNum;
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

        private void buildHeader(ByteBuffer byteBuffer) {
            byteBuffer.putShort((short) srcPort);
            byteBuffer.putShort((short) dstPort);
            byteBuffer.putInt((int) seqNum);
            byteBuffer.putInt((int) ackNum);
            byteBuffer.put(offsetAndReserved);
            byteBuffer.put(flags);
            byteBuffer.putShort((short) window);
            byteBuffer.putShort((short) checksum);
            byteBuffer.putShort((short) urgentPointer);
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

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("TcpHeader{");
            stringBuilder.append("srcPort=").append(srcPort)
                    .append(", dstPort=").append(dstPort)
                    .append(", seqNum=").append(seqNum)
                    .append(", ackNum=").append(ackNum)
                    .append(", headerLength=").append(headerLength)
                    .append(", flags=").append(flags)
                    .append(", window=").append(window)
                    .append(", checksum=").append(checksum);
            if (isFIN()) stringBuilder.append(" FIN");
            if (isSYN()) stringBuilder.append(" SYN");
            if (isRST()) stringBuilder.append(" RST");
            if (isPSH()) stringBuilder.append(" PSH");
            if (isACK()) stringBuilder.append(" ACK");
            if (isURG()) stringBuilder.append(" URG");
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    public static class UdpHeader {
        public int srcPort;
        public int dstPort;
        public int checksum;
        public int length;

        private UdpHeader(ByteBuffer byteBuffer) {
            this.srcPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.dstPort = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.length = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.checksum = BitUtil.getUnsignedShort(byteBuffer.getShort());
        }

        private void buildHeader(ByteBuffer byteBuffer) {
            byteBuffer.putShort((short) srcPort);
            byteBuffer.putShort((short) dstPort);
            byteBuffer.putShort((short) length);
            byteBuffer.putShort((short) checksum);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("UdpHeader{");
            stringBuilder.append("srcPort=").append(srcPort)
                    .append("dstPort=").append(dstPort)
                    .append("length=").append(length)
                    .append("checksum=").append(checksum);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    public static class Ipv4Header {
        public byte version;
        public byte ihl;
        public short dscp;
        public int totalLength;
        public int identificationAndFlagsAndOffset;
        public short ttl;
        public TransportProtocol transportProtocol;
        private short protocolNumber;
        public int checksum;
        public InetAddress srcAddr;
        public InetAddress dstAddr;
        public int options;

        private Ipv4Header(ByteBuffer byteBuffer) throws UnknownHostException {
            
        }

        private enum TransportProtocol {
            TCP(6),
            UDP(17),
            Other(0xFF);

            private int protocolNumber;

            TransportProtocol(int protocolNumber) {
                this.protocolNumber = protocolNumber;
            }

            private static TransportProtocol getProtocol(int protocolNumber) {
                if (protocolNumber == 6) return TCP;
                if (protocolNumber == 17) return UDP;
                return Other;
            }

            public int getProtocolNumber() {
                return this.protocolNumber;
            }

        }
    }
}
