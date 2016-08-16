package oxp.protocol.ver10;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by cr on 16-7-19.
 */
public class OXPFeaturesReplyVer10 implements OXPFeaturesReply {
    private static final Logger logger = LoggerFactory.getLogger(OXPFeaturesReplyVer10.class);

    private static int LENGTH = 24;

    private static final long DEFAULT_XID = 0x0L;
    private static final DomainId DEFAULT_DOMAIN_ID = DomainId.None;
    public static final Set<OXPCapabilities> DEFAULT_CAPABILITIES = ImmutableSet.<OXPCapabilities>of();

    private final long xid;
    private final DomainId domainId;
    private final OXPSbpType sbpType;
    private final OXPSbpVersion sbpVersion;
    private final Set<OXPCapabilities> capabilities;

    public OXPFeaturesReplyVer10(long xid, DomainId domainId, OXPSbpType sbpType, OXPSbpVersion sbpVersion, Set<OXPCapabilities> capabilities) {
        if (domainId == null) {
            throw new NullPointerException("OXPFeaturesReplyVer10: property domainId cannot be null");
        }
        if (sbpType == null) {
            throw new NullPointerException("OXPFeaturesReplyVer10: property sbpType cannot be null");
        }
        if (sbpVersion == null) {
            throw new NullPointerException("OXPFeaturesReplyVer10: property sbpVersion cannot be null");
        }
        if (capabilities == null) {
            throw new NullPointerException("OXPFeaturesReplyVer10: property capabilities cannot be null");
        }
        this.xid = xid;
        this.domainId = domainId;
        this.sbpType = sbpType;
        this.sbpVersion = sbpVersion;
        this.capabilities = capabilities;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_REATURES_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public DomainId getDomainId() {
        return domainId;
    }

    @Override
    public OXPSbpType getSbpType() {
        return sbpType;
    }

    @Override
    public OXPSbpVersion getSbpVsesion() {
        return sbpVersion;
    }

    @Override
    public Set<OXPCapabilities> getCapabilities() {
        return capabilities;
    }


    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPFeaturesReply> {
        @Override
        public OXPFeaturesReply readFrom(ChannelBuffer bb) throws OXPParseError {
            int startIndex = bb.readerIndex();
            // version
            byte version = bb.readByte();
            // type
            byte type = bb.readByte();
            // length
            int length = bb.readShort();
            if (length != LENGTH)
                throw new OXPParseError("Wrong length: Expected to be >= " + LENGTH + ", was: " + length);
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }
            // xid
            long xid = bb.readInt();
            // domainID
            DomainId domainId = DomainId.of(bb.readLong());
            // sbpType
            OXPSbpType sbpType = OXPSbpTypeSerializerVer10.readFrom(bb);
            // sbpVersion
            OXPSbpVersion sbpVersion = OXPSbpVersion.of(bb.readByte(), OXPVersion.OXP_10);
            // pad[2]
            bb.skipBytes(2);
            // capabilities
            Set<OXPCapabilities> capabilities = OXPCapabilitiesSerializerVer10.readFrom(bb);
            return new OXPFeaturesReplyVer10(xid, domainId, sbpType, sbpVersion, capabilities);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPFeaturesReplyVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPFeaturesReplyVer10 message) {
            // version
            bb.writeByte(OXPVersion.OXP_10.getWireVersion());
            // type
            bb.writeByte(OXPType.OXPT_REATURES_REPLY.value());
            // length
            bb.writeShort(LENGTH);
            // xid
            bb.writeInt((int) message.xid);
            // domainId
            bb.writeLong(message.domainId.getLong());
            // sbpType
            OXPSbpTypeSerializerVer10.writeTo(bb, message.sbpType);
            // sbpVersion
            message.sbpVersion.writeTo(bb);
            // byte pad[2]
            bb.writeZero(2);
            // capabilities
            OXPCapabilitiesSerializerVer10.writeTo(bb, message.capabilities);
        }
    }

    @Override
    public OXPMessage.Builder createBuilder() {
        return null;
    }

    static final class Builder implements OXPFeaturesReply.Builder {
        private boolean xidSet;
        private long xid;
        private boolean domainIdSet;
        private DomainId domainId;
        private boolean sbpTypeSet;
        private OXPSbpType sbpType;
        private boolean sbpVersionSet;
        private OXPSbpVersion sbpVersion;
        private boolean capabilitiesSet;
        private Set<OXPCapabilities> capabilities;

        @Override
        public OXPFeaturesReply build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            DomainId domainId = this.domainIdSet? this.domainId: DEFAULT_DOMAIN_ID;
            if (domainId == null)
                throw new NullPointerException("Property domainId must not be null");
            if (this.sbpType == null)
                throw new NullPointerException("Property sbpType must not be null");
            OXPSbpType sbpType = this.sbpType;
            if (this.sbpVersion == null)
                throw new NullPointerException("Property sbpVersion must not be null");
            OXPSbpVersion sbpVersion = this.sbpVersion;
            Set<OXPCapabilities> capabilities = this.capabilitiesSet ? this.capabilities : DEFAULT_CAPABILITIES;
            if (capabilities == null)
                throw new NullPointerException("Property capabilities must not be null");
            return new OXPFeaturesReplyVer10(xid, domainId, sbpType, sbpVersion, capabilities);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_REATURES_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPFeaturesReply.Builder setXid(long xid) {
            this.xidSet = true;
            this.xid = xid;
            return this;
        }

        @Override
        public DomainId getDomainId() {
            return domainId;
        }

        @Override
        public OXPFeaturesReply.Builder setDomainId(DomainId domainId) {
            this.domainIdSet = true;
            this.domainId = domainId;
            return this;
        }

        @Override
        public OXPSbpType getSbpType() {
            return sbpType;
        }

        @Override
        public OXPFeaturesReply.Builder setSbpType(OXPSbpType sbpType) {
            this.sbpTypeSet = true;
            this.sbpType = sbpType;
            return this;
        }

        @Override
        public OXPSbpVersion getSbpVsesion() {
            return sbpVersion;
        }

        @Override
        public OXPFeaturesReply.Builder setSbpVersion(OXPSbpVersion sbpVersion) {
            this.sbpVersionSet = true;
            this.sbpVersion = sbpVersion;
            return this;
        }

        @Override
        public Set<OXPCapabilities> getCapabilities() {
            return capabilities;
        }

        @Override
        public OXPFeaturesReply.Builder setCapabilities(Set<OXPCapabilities> capabilities) {
            this.capabilitiesSet = true;
            this.capabilities = capabilities;
            return this;
        }
    }
    @Override
    public void putTo(PrimitiveSink sink) {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPFeaturesReplyVer10 other = (OXPFeaturesReplyVer10) obj;

        if( xid != other.xid)
            return false;
        if (domainId == null) {
            if (other.domainId != null)
                return false;
        } else if (!domainId.equals(other.domainId))
            return false;
        if (sbpType == null) {
            if (other.sbpType != null)
                return false;
        } else if (!sbpType.equals(other.sbpType))
            return false;
        if (sbpVersion == null) {
            if (other.sbpVersion != null)
                return false;
        } else if (!sbpVersion.equals(other.sbpVersion))
            return false;
        if (capabilities == null) {
            if (other.capabilities != null)
                return false;
        } else if (!capabilities.equals(other.capabilities))
            return false;
        return true;
    }
}
