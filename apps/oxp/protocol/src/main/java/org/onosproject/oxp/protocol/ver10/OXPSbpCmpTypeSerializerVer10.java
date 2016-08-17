package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPSbpCmpType;

/**
 * Created by cr on 16-7-22.
 */
public class OXPSbpCmpTypeSerializerVer10 {

    public static final byte NORMAL_VAL = 0;
    public static final byte FORWARDING_REQUEST_VAL = 1;
    public static final byte PACKET_OUT_VAL = 2;
    public static final byte FORWARDING_REPLY_VAL = 4;


    public static OXPSbpCmpType readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPSbpCmpType type) {
        bb.writeByte(toWireValue(type));
    }

    public static OXPSbpCmpType ofWireValue (byte val) {
        switch (val) {
            case NORMAL_VAL:
                return OXPSbpCmpType.NORMAL;
            case FORWARDING_REQUEST_VAL:
                return OXPSbpCmpType.FORWARDING_REQUEST;
            case PACKET_OUT_VAL:
                return OXPSbpCmpType.PACKET_OUT;
            case FORWARDING_REPLY_VAL:
                return OXPSbpCmpType.FORWARDING_REPLY;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPSbpModeFlag in version 1.0: " + val);
        }
    }

    public static byte toWireValue(OXPSbpCmpType type) {
        switch (type) {
            case NORMAL:
                return NORMAL_VAL;
            case FORWARDING_REQUEST:
                return FORWARDING_REQUEST_VAL;
            case FORWARDING_REPLY:
                return FORWARDING_REPLY_VAL;
            case PACKET_OUT:
                return PACKET_OUT_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPSbpModeFlag in version 1.0: " + type);
        }
    }
}
