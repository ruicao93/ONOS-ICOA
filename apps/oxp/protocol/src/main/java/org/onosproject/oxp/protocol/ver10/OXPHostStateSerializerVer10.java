package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPHostState;

/**
 * Created by cr on 16-7-21.
 */
public class OXPHostStateSerializerVer10 {
    public static final byte ACTIVE_VAL = 0;
    public static final byte INACTIVE_VAL = 1;


    public static OXPHostState readFrom(ChannelBuffer bb)  throws OXPParseError{
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, OXPHostState state) {
        bb.writeByte(toWireValue(state));
    }

    public static void putTo(OXPHostState state, PrimitiveSink sink) {
        sink.putByte(toWireValue(state));
    }
    public static OXPHostState ofWireValue(byte val) {
        switch (val) {
            case ACTIVE_VAL:
                return OXPHostState.ACTIVE;
            case INACTIVE_VAL:
                return OXPHostState.INACTIVE;
            default:
                throw new IllegalArgumentException("Illegal enum value for type OFPortState in version 1.0: " + val);
        }
    }

    public static byte toWireValue(OXPHostState state) {
        if (state == null)
            throw new NullPointerException("OXPHostState must cannot be null");
        switch (state) {
            case ACTIVE:
                return ACTIVE_VAL;
            case INACTIVE:
                return INACTIVE_VAL;
            default:
                throw new IllegalArgumentException("Illegal enum type  for type OFPortState in version 1.0: " + state);
        }
    }
}
