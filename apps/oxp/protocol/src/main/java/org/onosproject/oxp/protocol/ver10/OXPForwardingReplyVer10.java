package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.OXPForwardingReply;
import org.onosproject.oxp.types.IPv4Address;

/**
 * Created by cr on 16-9-12.
 */
public class OXPForwardingReplyVer10 implements OXPForwardingReply {

    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    private int srcVport;
    private int dstVport;
    private IPv4Address mask;
    private short ethType;
    private byte qos;

    private OXPForwardingReplyVer10(IPv4Address srcIpAddress, IPv4Address dstIpAddress,
                                   int srcVport, int dstVport, IPv4Address mask, short ethType, byte qos) {
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.srcVport = srcVport;
        this.dstVport = dstVport;
        this.mask = mask;
        this.ethType = ethType;
        this.qos = qos;
    }

    public static OXPForwardingReply of(IPv4Address srcIpAddress, IPv4Address dstIpAddress,
                                        int srcVport, int dstVport, IPv4Address mask, short ethType, byte qos) {
        return new OXPForwardingReplyVer10(srcIpAddress, dstIpAddress, srcVport, dstVport,
                mask, ethType, qos);
    }

    public static OXPForwardingReply read(ChannelBuffer bb) {
        IPv4Address srcIpAddress = IPv4Address.read4Bytes(bb);
        IPv4Address dstIpAddress = IPv4Address.read4Bytes(bb);
        int srcVport = bb.readInt();
        int dstVport = bb.readInt();
        IPv4Address mask = IPv4Address.read4Bytes(bb);
        short ethType = bb.readShort();
        byte qos = bb.readByte();
        return of(srcIpAddress, dstIpAddress,
                srcVport, dstVport, mask,
                ethType, qos);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);;
        bb.writeInt(srcVport);
        bb.writeInt(dstVport);
        mask.writeTo(bb);
        bb.writeShort(ethType);
        bb.writeByte(qos);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public IPv4Address getSrcIpAddress() {
        return srcIpAddress;
    }

    @Override
    public IPv4Address getDstIpAddress() {
        return dstIpAddress;
    }

    @Override
    public int getSrcVport() {
        return srcVport;
    }

    @Override
    public int getDstVport() {
        return dstVport;
    }

    @Override
    public IPv4Address getMask() {
        return mask;
    }

    @Override
    public short getEthType() {
        return ethType;
    }

    @Override
    public byte getQos() {
        return qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OXPForwardingReplyVer10 that = (OXPForwardingReplyVer10) o;

        if (srcVport != that.srcVport) return false;
        if (dstVport != that.dstVport) return false;
        if (ethType != that.ethType) return false;
        if (qos != that.qos) return false;
        if (srcIpAddress != null ? !srcIpAddress.equals(that.srcIpAddress) : that.srcIpAddress != null) return false;
        if (dstIpAddress != null ? !dstIpAddress.equals(that.dstIpAddress) : that.dstIpAddress != null) return false;
        return mask != null ? mask.equals(that.mask) : that.mask == null;

    }

    @Override
    public int hashCode() {
        int result = srcIpAddress != null ? srcIpAddress.hashCode() : 0;
        result = 31 * result + (dstIpAddress != null ? dstIpAddress.hashCode() : 0);
        result = 31 * result + srcVport;
        result = 31 * result + dstVport;
        result = 31 * result + (mask != null ? mask.hashCode() : 0);
        result = 31 * result + (int) ethType;
        result = 31 * result + (int) qos;
        return result;
    }
}
