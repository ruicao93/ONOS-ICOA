
package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.Ints;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.protocol.Writeable;

/**
 * Created by cr on 16-4-7.
 */
public final class U16 implements Writeable, OXPValueType<U16> {
    private static final short ZERO_VAL = 0;
    public static final U16 ZERO = new U16(ZERO_VAL);

    private static final short NO_MASK_VAL = (short) 0xFFff;
    public static final U16 NO_MASK = new U16(NO_MASK_VAL);
    public static final U16 FULL_MASK = ZERO;

    public static int f(final short i) {
        return i & 0xffff;
    }

    public static short t(final int l) {
        return (short) l;
    }

    private final short raw;

    private U16(short raw) {
        this.raw = raw;
    }

    public static final U16 of(int value) {
        return ofRaw(t(value));
    }

    public static final U16 ofRaw(short raw) {
        if (raw == ZERO_VAL) {
            return ZERO;
        }
        return new U16(raw);
    }

    public int getValue() {
        return f(raw);
    }

    public short getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return String.format("0x%04x", raw);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + raw;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        U16 other = (U16) obj;
        if (raw != other.raw) {
            return false;
        }
        return true;
    }


    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeShort(raw);
    }


    public static final Reader READER = new Reader();

    private static class Reader implements OXPMessageReader<U16> {
        @Override
        public U16 readFrom(ChannelBuffer bb) throws OXPParseError {
            return ofRaw(bb.readShort());
        }
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public U16 applyMask(U16 mask) {
        return ofRaw( (short) (raw & mask.raw));
    }

    @Override
    public int compareTo(U16 o) {
        return Ints.compare(f(raw), f(o.raw));
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putShort(raw);
    }
}
