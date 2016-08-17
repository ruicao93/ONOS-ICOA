package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPCapabilities;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by cr on 16-7-18.
 */
public class OXPCapabilitiesSerializerVer10 {
    public static final int FLOW_STATS_VAL = 1 << 0;
    public static final int TABLE_STATS_VAL = 1 << 1;
    public static final int PORT_STATS_VAL = 1 << 2 ;
    public static final int GROUP_STATS_VAL = 1 << 3;
    public static final int IP_REASM_VAL = 1 << 4;
    public static final int QUEUE_STATS_VAL = 1 << 5;
    public static final int PORT_BLOCKED_VAL = 1 << 6;

    public static Set<OXPCapabilities> readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readInt());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OXPCapabilities> set) {
        bb.writeInt(toWireValue(set));
    }

    public static void putTo(Set<OXPCapabilities> set, PrimitiveSink sink) {
        sink.putInt(toWireValue(set));
    }

    public static Set<OXPCapabilities> ofWireValue(int val) {
        EnumSet<OXPCapabilities> set = EnumSet.noneOf(OXPCapabilities.class);

        if ((val & FLOW_STATS_VAL) != 0)
            set.add(OXPCapabilities.FLOW_STATS);
        if ((val & TABLE_STATS_VAL) != 0)
            set.add(OXPCapabilities.TABLE_STATS);
        if ((val & PORT_STATS_VAL) != 0)
            set.add(OXPCapabilities.PORT_STATS);
        if ((val & GROUP_STATS_VAL) != 0)
            set.add(OXPCapabilities.GROUP_STATS);
        if ((val & IP_REASM_VAL) != 0)
            set.add(OXPCapabilities.IP_REASM);
        if ((val & QUEUE_STATS_VAL) != 0)
            set.add(OXPCapabilities.QUEUE_STATS);
        if ((val & PORT_BLOCKED_VAL) != 0)
            set.add(OXPCapabilities.PORT_BLOCKED);
        return Collections.unmodifiableSet(set);
    }

    public static int toWireValue(Set<OXPCapabilities> set) {
        int wireValue = 0;

        for (OXPCapabilities e : set) {
            switch (e) {
                case FLOW_STATS:
                    wireValue |= FLOW_STATS_VAL;
                    break;
                case TABLE_STATS:
                    wireValue |= TABLE_STATS_VAL;
                    break;
                case PORT_STATS:
                    wireValue |= PORT_STATS_VAL;
                    break;
                case GROUP_STATS:
                    wireValue |= GROUP_STATS_VAL;
                    break;
                case IP_REASM:
                    wireValue |= IP_REASM_VAL;
                    break;
                case QUEUE_STATS:
                    wireValue |= QUEUE_STATS_VAL;
                    break;
                case PORT_BLOCKED:
                    wireValue |= PORT_BLOCKED_VAL;
                    break;
            }
        }
        return wireValue;
    }
}
