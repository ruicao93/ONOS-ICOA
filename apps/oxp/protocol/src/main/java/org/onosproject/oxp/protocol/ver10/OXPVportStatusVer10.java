package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cr on 16-7-22.
 */
public class OXPVportStatusVer10 implements OXPVportStatus {
    private static final Logger logger = LoggerFactory.getLogger(OXPVportStatusVer10.class);

    // version: 1.0
    static final byte WIRE_VERSION = 1;
    static final int LENGTH = 24;

    private static final long DEFAULT_XID = 0x0L;

    // OXP msg fields:
    private final long xid;
    private final OXPVportReason reason;
    private final OXPVportDesc vportDesc;

    public OXPVportStatusVer10(long xid, OXPVportReason reason, OXPVportDesc vportDesc) {
        this.xid = xid;
        this.reason = reason;
        this.vportDesc = vportDesc;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_VPORT_STATUS;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OXPVportReason getReason() {
        return reason;
    }

    @Override
    public OXPVportDesc getVportDesc() {
        return vportDesc;
    }

    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPVportStatus> {
        @Override
        public OXPVportStatus readFrom(ChannelBuffer bb) throws OXPParseError {
            int startIndex = bb.readerIndex();
            // version
            byte version = bb.readByte();
            // type
            byte type = bb.readByte();
            // length
            int length = bb.readShort();
            // xid
            long xid = bb.readInt();
            // reason
            OXPVportReason reason = OXPVportReasonSerializerVer10.readFrom(bb);
            // pad[7]
            bb.skipBytes(7);
            // vport desc
            OXPVportDesc vportDesc = OXPVportDescVer10.READER.readFrom(bb);
            return new OXPVportStatusVer10(xid, reason, vportDesc);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPVportStatusVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPVportStatusVer10 message){
            int length = bb.writerIndex();
            // version
            bb.writeByte(OXPVersion.OXP_10.getWireVersion());
            // type
            bb.writeByte(OXPType.OXPT_VPORT_STATUS.value());
            // length
            bb.writeShort(LENGTH);
            // xid
            bb.writeInt((int) message.xid);
            // reason
            OXPVportReasonSerializerVer10.writeTo(bb, message.reason);
            // pad[7]
            bb.writeZero(7);
            // vport desc
            message.vportDesc.writeTo(bb);
        }
    }

    @Override
    public Builder createBuilder() {
        return null;
    }

    static class Builder implements OXPVportStatus.Builder {
        // OXP msg fileds
        private long xid;
        private OXPVportReason reason;
        private  OXPVportDesc vportDesc;

        @Override
        public OXPVportStatus build() {
            if (reason == null)
                throw new NullPointerException("reason cannot be null");
            if (vportDesc == null)
                throw new NullPointerException("vportDesc cannot be null");
            return new OXPVportStatusVer10(xid, reason, vportDesc);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_VPORT_STATUS;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPVportStatus.Builder setXid(long xid) {
            this.xid = xid;
            return this;
        }

        @Override
        public OXPVportReason getReason() {
            return reason;
        }

        @Override
        public OXPVportStatus.Builder setReason(OXPVportReason reason) {
            this.reason = reason;
            return this;
        }

        @Override
        public OXPVportDesc getVportDesc() {
            return vportDesc;
        }

        @Override
        public OXPVportStatus.Builder setVportDesc(OXPVportDesc vportDesc) {
            this.vportDesc = vportDesc;
            return this;
        }
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        // TODO: 16-7-20
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((vportDesc == null) ? 0 : vportDesc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPVportStatusVer10 other = (OXPVportStatusVer10) obj;
        if( xid != other.xid)
            return false;
        if (this.reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (this.vportDesc == null) {
            if (other.vportDesc != null)
                return false;
        } else if (!vportDesc.equals(other.vportDesc))
            return false;
        return true;
    }
}
