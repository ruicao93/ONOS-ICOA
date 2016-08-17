package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPSbpModeFlag;

/**
 * Created by cr on 16-7-22.
 */
public class OXPSbpModeFlagSerializerVer10 {
    public static final byte NORMAL_VAL = 0;
    public static final byte COMPRESSED_VAL = 1;


    public static OXPSbpModeFlag readFrom(ChannelBuffer bb) throws OXPParseError{
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPSbpModeFlag sbpModeFlag) {
        bb.writeByte(toWireValue(sbpModeFlag));
    }

    public static OXPSbpModeFlag ofWireValue(byte val) {
        switch (val) {
            case NORMAL_VAL:
                return OXPSbpModeFlag.NORMAL;
            case COMPRESSED_VAL:
                return OXPSbpModeFlag.COMPRESSED;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPSbpModeFlag in version 1.0: " + val);
        }
    }

    public static byte toWireValue(OXPSbpModeFlag sbpModeFlag) {
        switch (sbpModeFlag) {
            case NORMAL:
                return NORMAL_VAL;
            case COMPRESSED:
                return COMPRESSED_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPSbpModeFlag in version 1.0: " + sbpModeFlag);
        }
    }
}
