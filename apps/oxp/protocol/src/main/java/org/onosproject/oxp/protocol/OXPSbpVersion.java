package org.onosproject.oxp.protocol;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPErrorCauseData;
import org.onosproject.oxp.types.PrimitiveSinkable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cr on 16-7-18.
 */
public class OXPSbpVersion implements Writeable, PrimitiveSinkable {
    private final static Logger logger = LoggerFactory.getLogger(OXPErrorCauseData.class);

    private final byte sbpVersion;
    private final OXPVersion version;

    private OXPSbpVersion(byte sbpVersion, OXPVersion version) {
        this.sbpVersion = sbpVersion;
        this.version = version;
    }

    public byte getSbpVersion() {
        return sbpVersion;
    }

    public static OXPSbpVersion of(byte data, OXPVersion version) {
        return new OXPSbpVersion(data, version);
    }

    public static OXPSbpVersion read(ChannelBuffer bb, OXPVersion version) {
        byte sbgVersion = bb.readByte();
        return of(sbgVersion, version);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putByte(sbpVersion);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeByte(sbpVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPSbpVersion other = (OXPSbpVersion) obj;
        if (sbpVersion !=sbpVersion)
            return false;
        if (version != other.version)
            return false;
        return true;
    }
}
