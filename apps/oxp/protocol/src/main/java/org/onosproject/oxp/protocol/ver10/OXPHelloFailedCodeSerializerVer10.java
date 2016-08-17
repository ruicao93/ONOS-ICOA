package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPHelloFailedCode;

/**
 * Created by cr on 16-7-17.
 */
public class OXPHelloFailedCodeSerializerVer10 {
    public final static short INCOMPATIBLE_VAL = (short) 0x0;
    public final static short EPERM_VAL = (short) 0x1;

    public static OXPHelloFailedCode readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readShort());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPHelloFailedCode e) {
        bb.writeShort(toWireValue(e));
    }

    public static void putTo(OXPHelloFailedCode e, PrimitiveSink sink) {
        sink.putShort(toWireValue(e));
    }

    public static OXPHelloFailedCode ofWireValue(short val) {
        switch(val) {
            case INCOMPATIBLE_VAL:
                return OXPHelloFailedCode.INCOMPATIBLE;
            case EPERM_VAL:
                return OXPHelloFailedCode.EPERM;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPHelloFailedCode in version 1.0: " + val);
        }
    }

    public static short toWireValue(OXPHelloFailedCode e) {
        switch (e) {
            case INCOMPATIBLE:
                return INCOMPATIBLE_VAL;
            case EPERM:
                return EPERM_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OXPHelloFailedCode in version 1.0: " + e);
        }
    }
}
