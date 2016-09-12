package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.OXPForwardingRequest;
import org.onosproject.oxp.types.IPv4Address;

import java.util.Arrays;

/**
 * Created by cr on 16-9-11.
 */
public class OXPForwardingRequestVer10 implements OXPForwardingRequest {

    private IPv4Address srcIpAddress;
    private IPv4Address dstIpAddress;
    int inPort;
    private IPv4Address mask;
    private short ethType;
    private byte qos;
    private byte[] data;

    private OXPForwardingRequestVer10(IPv4Address srcIpAddress, IPv4Address dstIpAddress,
                                      int inPort, IPv4Address mask,
                                     short ethType, byte qos, byte[] data) {
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.inPort = inPort;
        this.mask = mask;
        this.ethType = ethType;
        this.qos = qos;
        this.data = Arrays.copyOf(data,data.length);
    }

    public static OXPForwardingRequest of(IPv4Address srcIpAddress, IPv4Address dstIpAddress,
                                          int inPort, IPv4Address mask,
                                          short ethType, byte qos, byte[] data) {
        return new OXPForwardingRequestVer10(srcIpAddress, dstIpAddress,
                inPort, mask,
                ethType, qos, data);
    }

    public static OXPForwardingRequest read(ChannelBuffer bb, int dataLength) {
        IPv4Address srcIpAddress = IPv4Address.read4Bytes(bb);
        IPv4Address dstIpAddress = IPv4Address.read4Bytes(bb);
        int inPort = bb.readInt();
        IPv4Address mask = IPv4Address.read4Bytes(bb);
        short ethType = bb.readShort();
        byte qos = bb.readByte();
        byte[] data = new byte[dataLength];
        bb.readBytes(data, 0, dataLength);
        return of(srcIpAddress, dstIpAddress,
                inPort, mask,
                ethType, qos, data);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcIpAddress.writeTo(bb);
        dstIpAddress.writeTo(bb);
        bb.writeInt(inPort);
        mask.writeTo(bb);
        bb.writeShort(ethType);
        bb.writeByte(qos);
        bb.writeBytes(data);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

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
    public int getInport() {
        return inPort;
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
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OXPForwardingRequestVer10 that = (OXPForwardingRequestVer10) o;

        if (inPort != that.inPort) return false;
        if (ethType != that.ethType) return false;
        if (qos != that.qos) return false;
        if (srcIpAddress != null ? !srcIpAddress.equals(that.srcIpAddress) : that.srcIpAddress != null) return false;
        if (dstIpAddress != null ? !dstIpAddress.equals(that.dstIpAddress) : that.dstIpAddress != null) return false;
        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;
        return Arrays.equals(data, that.data);

    }

    @Override
    public int hashCode() {
        int result = srcIpAddress != null ? srcIpAddress.hashCode() : 0;
        result = 31 * result + (dstIpAddress != null ? dstIpAddress.hashCode() : 0);
        result = 31 * result + inPort;
        result = 31 * result + (mask != null ? mask.hashCode() : 0);
        result = 31 * result + (int) ethType;
        result = 31 * result + (int) qos;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
