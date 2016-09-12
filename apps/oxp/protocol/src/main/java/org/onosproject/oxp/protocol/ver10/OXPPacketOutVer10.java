package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.OXPPacketOut;

import java.util.Arrays;

/**
 * Created by cr on 16-9-12.
 */
public class OXPPacketOutVer10 implements OXPPacketOut {

    private int outPort;
    private byte[] data;

    private OXPPacketOutVer10(int outPort, byte[] data) {
        this.outPort = outPort;
        this.data = data;
    }

    public static OXPPacketOut of(int outPort, byte[] data) {
        return new OXPPacketOutVer10(outPort, data);
    }

    public static OXPPacketOut read(ChannelBuffer bb, int dataLength) {
        int outPort = bb.readInt();
        byte[] data = new byte[dataLength];
        bb.readBytes(data);
        return of(outPort, data);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeInt(outPort);
        bb.writeBytes(data);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public long getOutPort() {
        return outPort;
    }



    @Override
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OXPPacketOutVer10 that = (OXPPacketOutVer10) o;

        if (outPort != that.outPort) return false;
        return Arrays.equals(data, that.data);

    }

    @Override
    public int hashCode() {
        int result = outPort;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
