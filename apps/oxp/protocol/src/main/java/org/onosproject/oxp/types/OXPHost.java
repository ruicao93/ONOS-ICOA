package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.OXPHostState;
import org.onosproject.oxp.protocol.Writeable;
import org.onosproject.oxp.protocol.ver10.OXPHostStateSerializerVer10;

/**
 * Created by cr on 16-7-21.
 */
public class OXPHost implements Writeable, PrimitiveSinkable {

    private final IPv4Address ipAddress;
    private final MacAddress macAddress;
    private final IPv4Address mask;
    private final OXPHostState state;

    OXPHost(IPv4Address ipAddress, MacAddress macAddress,IPv4Address mask, OXPHostState state) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.mask = mask;
        this.state = state;
    }

    public OXPHostState getState() {
        return state;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public IPv4Address getIpAddress() {
        return ipAddress;
    }

    public IPv4Address getMask() {
        return mask;
    }

    public IPv4AddressWithMask getIpAddressWithMask() {
        return ipAddress.withMask(mask);
    }

    public static OXPHost of(IPv4Address ipAddress, MacAddress macAddress, IPv4Address mask, OXPHostState state) {
        if (ipAddress == null)
            throw new NullPointerException("Property ipAddress must not be null");
        if (macAddress == null)
            throw new NullPointerException("Property macAddress must not be null");
        if (mask == null)
            throw new NullPointerException("Property mask must not be null");
        if (state == null)
            throw new NullPointerException("Property state must not be null");
        return new OXPHost(ipAddress, macAddress, mask, state);
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        ipAddress.writeTo(bb);
        macAddress.write6Bytes(bb);
        bb.writeByte(mask.getInt());
        OXPHostStateSerializerVer10.writeTo(bb, state);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
        result = prime * result + ((mask == null) ? 0 : mask.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPHost other = (OXPHost) obj;
        if (this.ipAddress != null) {
            if (other.ipAddress == null)
                return false;
        } else if (!this.ipAddress.equals(other.ipAddress))
            return false;
        if (this.macAddress != null) {
            if (other.macAddress == null)
                return false;
        } else if (!this.macAddress.equals(other.macAddress))
            return false;
        if (this.mask != null) {
            if (other.mask == null)
                return false;
        } else if (!this.mask.equals(other.mask))
            return false;
        if (this.state != null) {
            if (other.state == null)
                return false;
        } else if (!this.state.equals(other.state))
            return false;
        return true;
    }
}
