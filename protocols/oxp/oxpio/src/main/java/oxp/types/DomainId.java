package oxp.types;

import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedLongs;
import org.onosproject.oxp.util.HexString;

import javax.annotation.Nonnull;

/**
 * Created by cr on 16-7-18.
 */
public class DomainId implements PrimitiveSinkable, Comparable<DomainId> {
    public static final DomainId None = new DomainId(0);

    private final long rawValue;

    private DomainId(long rawValue) {
        this.rawValue = rawValue;
    }

    public static DomainId of(long rawValue) {
        return new DomainId(rawValue);
    }

    public static DomainId of(String s) {
        return new DomainId(HexString.toLong(s));
    }

    public static DomainId of(@Nonnull MacAddress mac) {
        return DomainId.of(mac.getLong());
    }

    public long getLong() {
        return rawValue;
    }

    public byte[] getBytes() {
        return Longs.toByteArray(rawValue);
    }

    @Override
    public String toString() {
        return HexString.toHexString(rawValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DomainId other = (DomainId) obj;
        if (rawValue != other.rawValue)
            return false;
        return true;
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putLong(rawValue);
    }

    @Override
    public int compareTo(DomainId o) {
        return UnsignedLongs.compare(rawValue, o.rawValue);
    }
}
