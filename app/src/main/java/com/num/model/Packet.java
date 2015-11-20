package com.num.model;

import com.num.controller.utils.BitUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Packet {
    public static final int IPV4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    public Ipv4Header ipv4Header;
    public TcpHeader tcpHeader;
    public UdpHeader udpHeader;
    public ByteBuffer backup;

    private boolean isTcp;
    private boolean isUdp;
    private int payloadSize;

    public Packet(ByteBuffer byteBuffer) throws UnknownHostException {
        this.ipv4Header = new Ipv4Header(byteBuffer);
        if (ipv4Header.transportProtocol == Ipv4Header.TransportProtocol.TCP) {
            this.isTcp = true;
            tcpHeader = new TcpHeader(byteBuffer);
        } else if (ipv4Header.transportProtocol == Ipv4Header.TransportProtocol.UDP) {
            this.isUdp = true;
            udpHeader = new UdpHeader(byteBuffer);
        }
        this.backup = byteBuffer;
        this.payloadSize = backup.limit() - backup.position();
    }

    public boolean isTcp() {
        return this.isTcp;
    }

    public boolean isUdp() {
        return this.isUdp;
    }

    public void reverseRoute() {
        InetAddress newSrcAddr = ipv4Header.dstAddr;
        ipv4Header.dstAddr = ipv4Header.srcAddr;
        ipv4Header.srcAddr = newSrcAddr;
        if (isTcp) {
            int newSrcPort = tcpHeader.dstPort;
            tcpHeader.dstPort = tcpHeader.srcPort;
            tcpHeader.srcPort = newSrcPort;
        } else if (isUdp) {
            int newSrcPort = udpHeader.dstPort;
            udpHeader.dstPort = udpHeader.srcPort;
            udpHeader.srcPort = newSrcPort;
        }
    }

    public void setTcpBuffer(ByteBuffer byteBuffer, byte flags, long seqNum, long ackNum, int payloadSize) {
        byteBuffer.position(0);
        ipv4Header.buildHeader(byteBuffer);
        tcpHeader.buildHeader(byteBuffer);
        backup = byteBuffer;

        byte offset = (byte) (TCP_HEADER_SIZE << 2);
        int ipTotalLength = IPV4_HEADER_SIZE + TCP_HEADER_SIZE + payloadSize;

        tcpHeader.flags = flags;
        tcpHeader.seqNum = seqNum;
        tcpHeader.ackNum = ackNum;
        tcpHeader.offsetAndReserved = offset;
        updateTcpChecksum(payloadSize);

        ipv4Header.totalLength = ipTotalLength;
        updateIpv4Checksum();

        backup.put(IPV4_HEADER_SIZE + 13, flags);
        backup.putInt(IPV4_HEADER_SIZE + 4, (int) seqNum);
        backup.putInt(IPV4_HEADER_SIZE + 8, (int) ackNum);
        backup.put(IPV4_HEADER_SIZE + 12, offset);
        backup.putShort(2, (short) ipTotalLength);
    }

    public void setUdpBuffer(ByteBuffer byteBuffer, int payloadSize) {
        byteBuffer.position(0);
        ipv4Header.buildHeader(byteBuffer);
        udpHeader.buildHeader(byteBuffer);
        backup = byteBuffer;

        int udpLength = UDP_HEADER_SIZE + payloadSize;
        backup.putShort(IPV4_HEADER_SIZE + 4, (short) udpLength);
        udpHeader.length = udpLength;

        backup.putShort(IPV4_HEADER_SIZE + 6, (short) 0);
        udpHeader.checksum = 0;

        int ipLength = IPV4_HEADER_SIZE + udpLength;
        backup.putShort(2, (short) ipLength);
        ipv4Header.totalLength = ipLength;
        updateIpv4Checksum();
    }

    private void updateTcpChecksum(int payloadSize) {
        int sum = 0;
        int tcpLength = payloadSize + TCP_HEADER_SIZE;

        // Calculate pseudo-header
        // http://www.tcpipguide.com/free/t_TCPChecksumCalculationandtheTCPPseudoHeader-2.htm
        ByteBuffer byteBuffer = ByteBuffer.wrap(ipv4Header.srcAddr.getAddress());
        sum += BitUtil.getUnsignedShort(byteBuffer.getShort()) + BitUtil.getUnsignedShort(byteBuffer.getShort());
        byteBuffer.wrap(ipv4Header.dstAddr.getAddress());
        sum += BitUtil.getUnsignedShort(byteBuffer.getShort()) + BitUtil.getUnsignedShort(byteBuffer.getShort());
        sum += Ipv4Header.TransportProtocol.TCP.getProtocolNumber() + tcpLength;
        byteBuffer = backup.duplicate();

        // Clear stale checksum
        byteBuffer.putShort(IPV4_HEADER_SIZE + 16, (short) 0);

        // Calculate segment checksum
        byteBuffer.position(IPV4_HEADER_SIZE);
        while (tcpLength > 1) {
            sum += BitUtil.getUnsignedShort(byteBuffer.getShort());
            tcpLength -= 2;
        }
        if (tcpLength > 0) sum += BitUtil.getUnsignedByte(byteBuffer.get()) << 8;
        while (sum >> 16 > 0) sum = (sum & 0xFFFF) + (sum >> 16);
        sum = ~sum;
        tcpHeader.checksum = sum;
        backup.putShort(IPV4_HEADER_SIZE + 16, (short) sum);
    }

    private void updateIpv4Checksum() {
        int sum = 0;

        ByteBuffer byteBuffer = backup.duplicate();
        byteBuffer.position(0);
        // Clear stale checksum
        byteBuffer.putShort(10, (short) 0);

        int headerLength = ipv4Header.headerLength;
        while (headerLength > 0) {
            sum += BitUtil.getUnsignedShort(byteBuffer.getShort());
            headerLength -= 2;
        }
        while (sum >> 16 > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        sum = ~sum;
        ipv4Header.checksum = sum;
        backup.putShort(10, (short) sum);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Packet{");
        stringBuilder.append("IPv4Header=").append(ipv4Header);
        if (isTcp) stringBuilder.append(", tcpHeader=").append(tcpHeader);
        else if (isUdp) stringBuilder.append("u, dpHeader=").append(udpHeader);
        stringBuilder.append(", payloadSize=").append(payloadSize);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

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
        public int checksum;
        public InetAddress srcAddr;
        public InetAddress dstAddr;
        public int options;
        public int headerLength;

        private Ipv4Header(ByteBuffer byteBuffer) throws UnknownHostException {
            byte versionAndIhl = byteBuffer.get();
            this.version = (byte) (versionAndIhl >> 4);
            this.ihl = (byte) (versionAndIhl & 0x0F);
            this.headerLength = this.ihl << 2;
            this.dscp = BitUtil.getUnsignedByte(byteBuffer.get());
            this.totalLength = BitUtil.getUnsignedShort(byteBuffer.getShort());
            this.identificationAndFlagsAndOffset = byteBuffer.getInt();
            this.ttl = BitUtil.getUnsignedByte(byteBuffer.get());
            this.transportProtocol = TransportProtocol.getProtocol(BitUtil.getUnsignedByte(byteBuffer.get()));
            this.checksum = BitUtil.getUnsignedShort(byteBuffer.getShort());

            byte[] addrBytes = new byte[4];
            byteBuffer.get(addrBytes, 0, 4);
            this.srcAddr = InetAddress.getByAddress(addrBytes);
            byteBuffer.get(addrBytes, 0, 4);
            this.dstAddr = InetAddress.getByAddress(addrBytes);

        }

        public void buildHeader(ByteBuffer byteBuffer) {
            byteBuffer.put((byte) (this.version << 4 | this.ihl));
            byteBuffer.put((byte) this.dscp);
            byteBuffer.putShort((short) this.totalLength);
            byteBuffer.putInt(this.identificationAndFlagsAndOffset);
            byteBuffer.put((byte) this.ttl);
            byteBuffer.put((byte) this.transportProtocol.getProtocolNumber());
            byteBuffer.putShort((short) this.checksum);
            byteBuffer.put(this.srcAddr.getAddress());
            byteBuffer.put(this.dstAddr.getAddress());
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("Ipv4Header{");
            stringBuilder.append("version=").append(version)
                    .append(", IHL=").append(ihl)
                    .append(", DSCP=").append(dscp)
                    .append(", totalLength=").append(totalLength)
                    .append(", identificationAndFlagsAndFragmentOffset=").append(identificationAndFlagsAndOffset)
                    .append(", TTL=").append(ttl)
                    .append(", protocol=").append(transportProtocol)
                    .append(", checksum=").append(checksum)
                    .append(", srcAddr=").append(srcAddr.getHostAddress())
                    .append(", dstAddr=").append(dstAddr.getHostAddress());
            stringBuilder.append('}');
            return stringBuilder.toString();
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
