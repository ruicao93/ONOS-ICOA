package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPVportReason;

/**
 * Created by cr on 16-7-22.
 */
public class OXPVportReasonSerializerVer10 {
    public static final byte ADD_VAL = 0;
    public static final byte DELETE_VAL = 1;
    public static final byte MODIFY_VAL = 2;


    public static OXPVportReason readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPVportReason reason) {
        bb.writeByte(toWireValue(reason));
    }

    public static OXPVportReason ofWireValue(byte val) {
        switch (val) {
            case ADD_VAL:
                return OXPVportReason.ADD;
            case DELETE_VAL:
                return OXPVportReason.DELETE;
            case MODIFY_VAL:
                return OXPVportReason.MODIFY;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPVportReason in version 1.0: " + val);
        }
    }

    public static byte toWireValue(OXPVportReason reason) {
        switch (reason) {
            case ADD:
                return ADD_VAL;
            case DELETE:
                return DELETE_VAL;
            case MODIFY:
                return MODIFY_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPVportReason in version 1.0: " + reason);
        }
    }
}
