package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPVportState;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by cr on 16-7-21.
 */
public class OXPVportStateSerializerVer10 {
    public static final int LINK_DOWN_VAL = 1 << 0;
    public static final int BLOCKED_VAL = 1 << 1;
    public static final int LIVE_VAL = 1 << 2;


    public static Set<OXPVportState> readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readInt());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OXPVportState> set) {
        bb.writeInt(toWireValue(set));
    }

    public static void putTo(Set<OXPVportState> set, PrimitiveSink sink) {
        sink.putInt(toWireValue(set));
    }

    public static Set<OXPVportState> ofWireValue(int val) {
        EnumSet<OXPVportState> set = EnumSet.noneOf(OXPVportState.class);

        if ((val & LINK_DOWN_VAL) != 0 )
            set.add(OXPVportState.LINK_DOWN);
        if ((val & BLOCKED_VAL) != 0)
            set.add(OXPVportState.BLOCKED);
        if ((val & LIVE_VAL) != 0)
            set.add(OXPVportState.LIVE);
        return Collections.unmodifiableSet(set);
    }

    public static int toWireValue(Set<OXPVportState> set) {
        int wireValue = 0;
        for (OXPVportState e : set) {
            switch (e) {
                case LINK_DOWN:
                    wireValue |= LINK_DOWN_VAL;
                    break;
                case BLOCKED:
                    wireValue |= BLOCKED_VAL;
                    break;
                case LIVE:
                    wireValue |= LIVE_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OXPVportState in version 1.0: " + e);
            }
        }
        return wireValue;
    }
}
