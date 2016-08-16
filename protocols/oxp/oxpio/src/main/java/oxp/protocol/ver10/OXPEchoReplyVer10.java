package oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by cr on 16-7-18.
 */
public class OXPEchoReplyVer10 implements OXPEchoReply {
    public static final Logger logger = LoggerFactory.getLogger(OXPEchoReplyVer10.class);

    // version : 1.0
    final static byte WIRE_VERSION = 1;
    final static int MINIMUM_LENGTH = 8;

    private final static long DEFAULT_XID = 0x0L;
    private final static byte[] DEFAULT_DATA = new byte[0];

    // OXP message fields
    private final long xid;
    private final byte[] data;

    OXPEchoReplyVer10(long xid, byte[] data) {
        if (data == null) {
            throw new NullPointerException("OXPEchoReplyVer10: property data cannot be null");
        }
        this.xid = xid;
        this.data = data;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_ECHO_REPLY;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    final static Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPEchoReply> {
        @Override
        public OXPEchoReply readFrom(ChannelBuffer bb) throws OXPParseError {
            int startIndex = bb.readerIndex();
            //version
            byte version = bb.readByte();
            //type
            byte type = bb.readByte();
            //length
            int length = bb.readShort();
            if (length < MINIMUM_LENGTH)
                throw new OXPParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            if (bb.readableBytes() + (bb.readerIndex() - startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }
            //xid
            long xid = bb.readInt();
            //data
            byte[] data = ChannelUtils.readBytes(bb, length - (bb.readerIndex() - startIndex));
            return new OXPEchoReplyVer10(xid, data);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPEchoReplyVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPEchoReplyVer10 message) {
            int startIndex = bb.writerIndex();
            //version = 1
            bb.writeByte(1);
            //type = 3
            bb.writeByte(3);
            //tmp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            //xid
            bb.writeInt((int) message.xid);
            //data
            bb.writeBytes(message.data);
            //update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    @Override
    public Builder createBuilder() {
        return null;
    }

    static class Builder implements OXPEchoReply.Builder {
        // OXP message fields
        private boolean xidSet;
        private long xid;
        private boolean dataSet;
        private byte[] data;

        @Override
        public OXPEchoReply build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            byte[] data = this.dataSet ? this.data : DEFAULT_DATA;
            if(data == null)
                throw new NullPointerException("Property data must not be null");
            return new OXPEchoReplyVer10(xid, data);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_ECHO_REPLY;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPEchoReply.Builder setXid(long xid) {
            this.xidSet = true;
            this.xid = xid;
            return this;
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public OXPEchoReply.Builder setData(byte[] data) {
            this.dataSet = true;
            this.data = data;
            return this;
        }
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        // TODO: 16-7-18
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPEchoReplyVer10 other = (OXPEchoReplyVer10) obj;

        if( xid != other.xid)
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }
}
