package oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cr on 16-7-21.
 */
public class OXPTopologyReplyVer10 implements OXPTopologyReply {
    public static final Logger logger = LoggerFactory.getLogger(OXPTopologyReplyVer10.class);

    private static final int HEAD_LENGTH = 8;

    private final long xid;
    private final List<OXPInternalLink> internalLinks;

    OXPTopologyReplyVer10(long xid, List<OXPInternalLink> internalLinks) {
        if (internalLinks == null)
            throw new NullPointerException("OXPTopologyReplyVer10: property internalLinks cannot be null");
        this.xid = xid;
        this.internalLinks = internalLinks;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_TOPO_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    public List<OXPInternalLink> getInternalLinks() {
        return internalLinks;
    }

    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPTopologyReply> {
        @Override
        public OXPTopologyReply readFrom(ChannelBuffer bb) throws OXPParseError {
            int startIndex = bb.readerIndex();
            // version
            byte version = bb.readByte();
            // type
            byte type = bb.readByte();
            // length
            int length = bb.readShort();
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }
            // xid
            long xid = bb.readInt();
            // internalLinks[]
            List<OXPInternalLink> internalLinks = ChannelUtils.readList(bb,length - (bb.readerIndex() - startIndex), OXPInternalLinkVer10.READER);
            return new OXPTopologyReplyVer10(xid, internalLinks);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPTopologyReplyVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPTopologyReplyVer10 message) {
            int startIndex = bb.writerIndex();
            // version
            bb.writeByte(OXPVersion.OXP_10.getWireVersion());
            // type
            bb.writeByte(OXPType.OXPT_TOPO_REPLY.value());
            // tmp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            // xid
            bb.writeInt((int) message.xid);
            // internalLinks[]
            ChannelUtils.writeList(bb, message.internalLinks);
            // update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }
    @Override
    public Builder createBuilder() {
        return null;
    }

    static class Builder implements OXPTopologyReply.Builder {
        private long xid;
        private List<OXPInternalLink> internalLinks;

        @Override
        public OXPTopologyReply build() {
            if (internalLinks == null)
                throw new NullPointerException("Property internalLinks must not be null");
            return new OXPTopologyReplyVer10(xid, internalLinks);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_TOPO_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPTopologyReply.Builder setXid(long xid) {
            this.xid = xid;
            return this;
        }

        @Override
        public List<OXPInternalLink> getInternalLink() {
            return internalLinks;
        }

        @Override
        public OXPTopologyReply.Builder setInternalLink(List<OXPInternalLink> list) {
            this.internalLinks = list;
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
        OXPTopologyReplyVer10 other = (OXPTopologyReplyVer10) obj;
        if (this.xid != other.xid)
            return false;
        if (this.internalLinks == null) {
            if (other.internalLinks != null)
                return false;
        } else if (!this.internalLinks.equals(other.internalLinks))
            return false;
        return true;
    }
}
