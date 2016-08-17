package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.OXPVersion;
import org.onosproject.oxp.protocol.Writeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cr on 16-7-21.
 */
public class OXPInternalLink implements Writeable, PrimitiveSinkable {
    private final static Logger logger = LoggerFactory.getLogger(OXPInternalLink.class);

    public static final int SINGLE_LENGTH = 12;

    private final OXPVport srcVport;
    private final OXPVport dstVport;
    private final long capability;
    private final OXPVersion version;

    OXPInternalLink(OXPVport srcVport, OXPVport dstVport, long capability, OXPVersion version) {
        this.srcVport = srcVport;
        this.dstVport = dstVport;
        this.capability = capability;
        this.version = version;
    }

    public OXPVport getSrcVport() {
        return srcVport;
    }

    public OXPVport getDstVport() {
        return dstVport;
    }

    public long getCapability() {
        return capability;
    }

    public static OXPInternalLink of(OXPVport srcVport, OXPVport dstVport, long capability,OXPVersion version) {
        return new OXPInternalLink(srcVport, dstVport, capability, version);
    }


    @Override
    public void putTo(PrimitiveSink sink) {
        srcVport.putTo(sink);
        dstVport.putTo(sink);
        sink.putShort((short) (capability >>> 32));
        sink.putInt((int) (capability & 0x00ff));
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        srcVport.writeTo(bb);
        dstVport.writeTo(bb);
        bb.writeShort((short) (capability >>> 32));
        bb.writeInt((int) (capability & 0x00ff));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPInternalLink other = (OXPInternalLink) obj;
        if (this.srcVport == null) {
            if (other.srcVport != null)
                return false;
        } else if (!this.srcVport.equals(other.srcVport)) {
            return false;
        }
        if (this.dstVport == null) {
            if (other.dstVport != null)
                return false;
        } else if (!this.dstVport.equals(other.dstVport)) {
            return false;
        }
        if (this.capability != other.capability)
            return false;
        if (this.version == null) {
            if (other.version != null)
                return false;
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        return true;
    }
}
