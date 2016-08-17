package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPSbpType;

/**
 * Created by cr on 16-7-18.
 */
public class OXPSbpTypeSerializerVer10 {
    public static final byte OPENFLOW_VAL = 1 << 0;
    public static final byte NETCONF_VAL = 1 << 1;
    public static final byte XMPP_VAL = 1 << 4;

    public static OXPSbpType readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPSbpType sbpType) {
        bb.writeByte(toWireValue(sbpType));
    }

    public static OXPSbpType ofWireValue(byte val) {
        switch (val) {
            case OPENFLOW_VAL:
                return OXPSbpType.OPENFLOW;
            case NETCONF_VAL:
                return OXPSbpType.NETCONF;
            case XMPP_VAL:
                return OXPSbpType.XMPP;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPSbpType in version 1.0: " + val);
        }
    }

    public static byte toWireValue(OXPSbpType sbpType) {
        switch (sbpType) {
            case OPENFLOW:
                return OPENFLOW_VAL;
            case NETCONF:
                return NETCONF_VAL;
            case XMPP:
                return XMPP_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPSbpType in version 1.0: " + sbpType);
        }
    }
}
