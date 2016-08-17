package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPBadRequestCode;

/**
 * Created by cr on 16-7-17.
 */
public class OXPBadRequestCodeSerializerVer10 {
    public final static short BAD_VERSION_VAL = (short) 0x0;
    public final static short BAD_TYPE_VAL = (short) 0x1;
    public final static short BAD_EXPERIMENTER_VAL = (short) 0x2;
    public final static short BAD_EXP_TYPE_VAL = (short) 0x3;
    public final static short EPERM_VAL = (short) 0x4;
    public final static short BAD_LEN_VAL = (short) 0x5;


    public static OXPBadRequestCode readFrom(ChannelBuffer bb)  throws OXPParseError{
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public  static void writeTo(ChannelBuffer bb, OXPBadRequestCode e) {
        bb.writeShort(toWireValue(e));
    }

    public  static void putTo(OXPBadRequestCode e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OXPBadRequestCode ofWireValue(short val) {
        switch (val) {
            case BAD_VERSION_VAL:
                return OXPBadRequestCode.BAD_VERSION;
            case BAD_TYPE_VAL:
                return OXPBadRequestCode.BAD_TYPE;
            case BAD_EXPERIMENTER_VAL:
                return OXPBadRequestCode.BAD_EXPERIMENTER;
            case BAD_EXP_TYPE_VAL:
                return OXPBadRequestCode.BAD_EXP_TYPE;
            case EPERM_VAL:
                return OXPBadRequestCode.EPERM;
            case BAD_LEN_VAL:
                return  OXPBadRequestCode.BAD_LEN;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPBadRequestCode in version 1.0: " + val);
        }
    }

    public static short toWireValue(OXPBadRequestCode e) {
        switch (e) {
            case BAD_VERSION:
                return BAD_VERSION_VAL;
            case BAD_TYPE:
                return BAD_TYPE_VAL;
            case BAD_EXPERIMENTER:
                return BAD_EXPERIMENTER_VAL;
            case BAD_EXP_TYPE:
                return BAD_EXP_TYPE_VAL;
            case EPERM:
                return EPERM_VAL;
            case BAD_LEN:
                return BAD_LEN_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPBadRequestCode in version 1.0: " + e);
        }
    }
}
