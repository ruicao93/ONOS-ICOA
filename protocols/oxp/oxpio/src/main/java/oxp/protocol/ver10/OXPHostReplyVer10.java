package oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cr on 16-7-22.
 */
public class OXPHostReplyVer10 implements OXPHostReply {
    public static final Logger logger = LoggerFactory.getLogger(OXPHostReplyVer10.class);

    private static final int HEAD_LENGTH = 8;

    private final long xid;
    private final List<OXPHost> hosts;

    OXPHostReplyVer10(long xid, List<OXPHost> hosts) {
        if (hosts == null)
            throw new NullPointerException("OXPHostReplyVer10: property hosts cannot be null");
        this.xid = xid;
        this.hosts = hosts;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_HOST_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public List<OXPHost> getHosts() {
        return hosts;
    }


    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPHostReply> {
        @Override
        public OXPHostReply readFrom(ChannelBuffer bb) throws OXPParseError {
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
            // hosts[]
            List<OXPHost> hosts = ChannelUtils.readList(bb, length - (bb.readerIndex() - startIndex), OXPHostVer10.READER);
            return new OXPHostReplyVer10(xid, hosts);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPHostReplyVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPHostReplyVer10 message) {
            int startIndex = bb.writerIndex();
            // version
            bb.writeByte(OXPVersion.OXP_10.getWireVersion());
            // type
            bb.writeByte(OXPType.OXPT_HOST_REPLY.value());
            // tmp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            // xid
            bb.writeInt((int) message.xid);
            // host[]
            ChannelUtils.writeList(bb, message.hosts);
            // update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    @Override
    public Builder createBuilder() {
        return null;
    }

    static class Builder implements OXPHostReply.Builder {
        private long xid;
        private List<OXPHost> hosts;

        @Override
        public OXPHostReply build() {
            if (hosts == null)
                throw new NullPointerException("property hosts cannot be null");
            return new OXPHostReplyVer10(xid, hosts);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_HOST_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPHostReply.Builder setXid(long xid) {
            this.xid = xid;
            return this;
        }

        @Override
        public List<OXPHost> getHosts() {
            return hosts;
        }

        @Override
        public OXPHostReply.Builder setHosts(List<OXPHost> hosts) {
            this.hosts = hosts;
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
        OXPHostReplyVer10 other = (OXPHostReplyVer10) obj;
        if (this.xid != other.xid)
            return false;
        if (this.hosts == null) {
            if (other.hosts != null)
                return false;
        } else if (!this.hosts.equals(other.hosts))
            return false;
        return true;
    }
}
