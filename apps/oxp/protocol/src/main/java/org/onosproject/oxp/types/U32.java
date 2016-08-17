package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.UnsignedInts;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.protocol.Writeable;

/**
 * Created by cr on 16-4-7.
 */
public final class U32 implements Writeable, OXPValueType<U32> {
    private static final int ZERO_VAL = 0;
    public static final U32 ZERO = new U32(ZERO_VAL);

    private static final int NO_MASK_VAL = 0xFFffFFff;
    public static final U32 NO_MASK = new U32(NO_MASK_VAL);
    public static final U32 FULL_MASK = ZERO;

    private final int raw;

    private U32(int raw) {
        this.raw = raw;
    }

    public static U32 of(long value) {
        return ofRaw(U32.t(value));
    }

    public static U32 ofRaw(int raw) {
        if (raw == ZERO_VAL) {
            return ZERO;
        }
        if (raw == NO_MASK_VAL) {
            return NO_MASK;
        }
        return new U32(raw);
    }

    public long getValue() {
        return f(raw);
    }

    public int getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return String.format("0x%08x", raw);
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

        U32 other = (U32) obj;
        if (raw != other.raw) {
            return false;
        }
        return true;
    }

    public static long f(final int i) {
        return i & 0xffffffffL;
    }

    public static int t(final long l) {
        return (int) l;
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeInt(raw);
    }

    public static final Reader READER = new Reader();

    private static class Reader implements OXPMessageReader<U32> {
        @Override
        public U32 readFrom(ChannelBuffer bb) throws OXPParseError {
            return new U32(bb.readInt());
        }
    }

    @Override
    public int getLength() {
        return 4;
    }

    @Override
    public U32 applyMask(U32 mask) {
        return ofRaw(raw & mask.raw);
    }

    @Override
    public int compareTo(U32 o) {
        return UnsignedInts.compare(raw, o.raw);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putInt(raw);
    }
}

