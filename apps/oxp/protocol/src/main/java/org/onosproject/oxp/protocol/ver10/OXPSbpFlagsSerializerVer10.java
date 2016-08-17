package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPSbpFlags;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by cr on 16-7-22.
 */
public class OXPSbpFlagsSerializerVer10 {
    public static final byte DATA_EXISTS_VAL = 1;


    public static Set<OXPSbpFlags> readFrom(ChannelBuffer bb) throws OXPParseError {
        try {
            return ofWireValue(bb.readByte());
        } catch (IllegalArgumentException e) {
            throw new OXPParseError(e);
        }
    }

    public static void writeTo(ChannelBuffer bb, Set<OXPSbpFlags> set) {
        bb.writeByte(toWireValue(set));
    }

    public static Set<OXPSbpFlags> ofWireValue(byte val) {
        EnumSet<OXPSbpFlags> set = EnumSet.noneOf(OXPSbpFlags.class);
        switch (val) {
            case DATA_EXISTS_VAL:
                set.add(OXPSbpFlags.DATA_EXIST);
                break;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPSbpFlags in version 1.0: " + val);
        }
        return set;
    }

    public static byte toWireValue(Set<OXPSbpFlags> set) {
        byte wireValue = 0;
        for (OXPSbpFlags e : set) {
            switch (e) {
                case DATA_EXIST:
                    wireValue |= DATA_EXISTS_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal  enum for type OXPSbpFlags in version 1.0: " + e);
            }
        }
        return wireValue;
    }
}
