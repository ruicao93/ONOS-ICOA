package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPConfigFlags;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by cr on 16-7-21.
 */
public class OXPConfigFlagsSerializerVer10 {
    public static final byte MODE_ADVANCED_VAL = 1 << 0;
    public static final byte CAP_BW_VAL = 1 << 1;
    public static final byte CAP_DELAY_VAL = 1 << 2;
    public static final byte CAP_HOP_VAL = 1 << 3;
    public static final byte MODE_COMPRESSED_VAL = 1 << 4;
    public static final byte MODE_TRUEST_VAL = 1 << 5;

    public static Set<OXPConfigFlags> readFrom(ChannelBuffer bb) throws OXPParseError {
        return ofWireValue(bb.readByte());
    }

    public static void writeTo(ChannelBuffer bb, Set<OXPConfigFlags> set) {
        bb.writeByte(toWireValue(set));
    }

    public static void putTo(Set<OXPConfigFlags> set, PrimitiveSink sink) {
        sink.putByte(toWireValue(set));
    }
    public static final Set<OXPConfigFlags> ofWireValue(byte val) {
        EnumSet<OXPConfigFlags> set = EnumSet.noneOf(OXPConfigFlags.class);

        if ((val & MODE_ADVANCED_VAL) != 0) {
            set.add(OXPConfigFlags.MODE_ADVANCED);
            if ((val & CAP_BW_VAL) !=0 ) {
                set.add(OXPConfigFlags.CAP_BW);
            } else if ((val & CAP_DELAY_VAL) !=0) {
                set.add(OXPConfigFlags.CAP_DELAY);
            } else if ((val & CAP_HOP_VAL) != 0) {
                set.add(OXPConfigFlags.CAP_HOP);
            }
        }
        if ((val & MODE_COMPRESSED_VAL) != 0) {
            set.add(OXPConfigFlags.MODE_TRUST);
        }
        if ((val & MODE_TRUEST_VAL) != 0) {
            set.add(OXPConfigFlags.MODE_TRUST);
        }
        return set;
    }

    public static final byte toWireValue(Set<OXPConfigFlags> set) {
        byte wireValue = 0;
        for (OXPConfigFlags e : set) {
            switch (e) {
                case MODE_ADVANCED:
                    wireValue |= MODE_ADVANCED_VAL;
                    break;
                case CAP_BW:
                    wireValue |= CAP_BW_VAL;
                    break;
                case CAP_DELAY:
                    wireValue |= CAP_DELAY_VAL;
                    break;
                case CAP_HOP:
                    wireValue |= CAP_HOP_VAL;
                    break;
                case MODE_COMPRESSED:
                    wireValue |= MODE_COMPRESSED_VAL;
                    break;
                case MODE_TRUST:
                    wireValue |= MODE_TRUEST_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OXPConfigFlags in version 1.0: " + e);
            }
        }
        return wireValue;
    }
}
