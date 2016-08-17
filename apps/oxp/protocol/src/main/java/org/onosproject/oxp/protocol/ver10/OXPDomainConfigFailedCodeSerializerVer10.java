package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPDomainConfigFaliedCode;

/**
 * Created by cr on 16-7-17.
 */
public class OXPDomainConfigFailedCodeSerializerVer10 {
    public final static short BAD_FLAGS_VAL = (short) 0x0;
    public final static short BAD_LEN_VAL = (short) 0x1;
    public final static short EPERM_VAL = (short) 0x2;

    public static OXPDomainConfigFaliedCode readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPDomainConfigFaliedCode e) {
        bb.writeShort(toWireValue(e));
    }

    public static void putTo(OXPDomainConfigFaliedCode e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OXPDomainConfigFaliedCode ofWireValue(short val) {
        switch (val) {
            case BAD_FLAGS_VAL:
                return OXPDomainConfigFaliedCode.BAD_FALGS;
            case BAD_LEN_VAL:
                return OXPDomainConfigFaliedCode.BAD_LEN;
            case EPERM_VAL:
                return OXPDomainConfigFaliedCode.EPERM;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPDomainConfigFaliedCode in version 1.0: " + val);
        }
    }
    public static short toWireValue(OXPDomainConfigFaliedCode e) {
        switch (e) {
            case BAD_FALGS:
                return BAD_FLAGS_VAL;
            case BAD_LEN:
                return BAD_LEN_VAL;
            case EPERM:
                return EPERM_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPDomainConfigFaliedCode in version 1.0: " + e);
        }
    }
}
