package oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPSbpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by cr on 16-7-22.
 */
public class OXPSbpVer10 implements OXPSbp {
    private static final Logger logger = LoggerFactory.getLogger(OXPSbpVer10.class);


    private final long xid;
    private final OXPSbpCmpType sbpCmpType;
    private final Set<OXPSbpFlags> flags;
    private final short dataLength;
    private final long sbpXid;
    private final OXPSbpData sbpData;

    OXPSbpVer10(long xid, OXPSbpCmpType sbpCmpType, Set<OXPSbpFlags> flags, short dataLength, long sbpXid, OXPSbpData sbpData) {
        this.xid = xid;
        this.sbpCmpType = sbpCmpType;
        this.flags = flags;
        this.dataLength = dataLength;
        this.sbpXid = sbpXid;
        this.sbpData = sbpData;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_SBP;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OXPSbpCmpType getSbpCmpType() {
        return sbpCmpType;
    }

    @Override
    public Set<OXPSbpFlags> getFlags() {
        return flags;
    }

    @Override
    public short getDataLength() {
        return dataLength;
    }

    @Override
    public long getSbpXid() {
        return sbpXid;
    }

    @Override
    public OXPSbpData getSbpData() {
        return sbpData;
    }



    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPSbp> {
        @Override
        public OXPSbp readFrom(ChannelBuffer bb) throws OXPParseError {
            int startIndex = bb.readerIndex();
            // version
            byte version = bb.readByte();
            // type
            byte type = bb.readByte();
            // length
            int length = bb.readShort();
            if (bb.readableBytes() + (bb.readerIndex() -startIndex) < length) {
                bb.readerIndex(startIndex);
                return null;
            }
            // xid
            long xid = bb.readInt();
            // sbpCmpType
            OXPSbpCmpType sbpCmpType = OXPSbpCmpTypeSerializerVer10.readFrom(bb);
            // flags
            Set<OXPSbpFlags> flags = OXPSbpFlagsSerializerVer10.readFrom(bb);
            // dataLength
            int dataLength = bb.readShort();
            // sbpXId
            long sbpXid = bb.readInt();
            // sbpData
            OXPSbpData sbpData = OXPSbpData.read(bb,length - (bb.readerIndex() - startIndex), OXPVersion.OXP_10);
            return new OXPSbpVer10(xid, sbpCmpType, flags, (short) dataLength, sbpXid, sbpData);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPSbpVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPSbpVer10 message) {
            int startIndex = bb.writerIndex();
            // version
            bb.writeByte(OXPVersion.OXP_10.getWireVersion());
            // type
            bb.writeByte(OXPType.OXPT_SBP.value());
            // tmp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            // xid
            bb.writeInt((int) message.xid);
            // sbpCmpType
            OXPSbpCmpTypeSerializerVer10.writeTo(bb, message.sbpCmpType);
            // flags
            OXPSbpFlagsSerializerVer10.writeTo(bb, message.flags);
            // dataLength
            bb.writeShort(message.dataLength);
            // sbpXid
            bb.writeInt((int) message.sbpXid);
            // sbpData
            bb.writeBytes(message.sbpData.getData());
            //update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    @Override
    public Builder createBuilder() {
        return null;
    }


    static class Builder implements OXPSbp.Builder {

        private long xid;
        private OXPSbpCmpType sbpCmpType;
        private Set<OXPSbpFlags> flags;
        private short dataLength;
        private long sbpXid;
        private OXPSbpData sbpData;

        @Override
        public OXPSbp build() {
            if (sbpCmpType == null)
                throw new NullPointerException("Property sbpCmdType must not be null");
            if (flags == null)
                throw new NullPointerException("Property flags must not be null");
            if (sbpData == null)
                throw new NullPointerException("Property sbpData must not be null");
            return new OXPSbpVer10(xid, sbpCmpType, flags, dataLength, sbpXid, sbpData);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_SBP;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPSbp.Builder setXid(long xid) {
            this.xid = xid;
            return this;
        }

        @Override
        public OXPSbpCmpType getSbpCmpType() {
            return sbpCmpType;
        }

        @Override
        public OXPSbp.Builder setSbpCmpType(OXPSbpCmpType sbpCmpType) {
            this.sbpCmpType = sbpCmpType;
            return this;
        }

        @Override
        public Set<OXPSbpFlags> getFlags() {
            return flags;
        }

        @Override
        public OXPSbp.Builder setFlags(Set<OXPSbpFlags> flags) {
            this.flags = flags;
            return this;
        }

        @Override
        public short getDataLength() {
            return dataLength;
        }

        @Override
        public OXPSbp.Builder setDataLength(short length) {
            this.dataLength = length;
            return this;
        }

        @Override
        public long getSbpXid() {
            return sbpXid;
        }

        @Override
        public OXPSbp.Builder setSbpXid(long sbpXid) {
            this.sbpXid = sbpXid;
            return this;
        }

        @Override
        public OXPSbpData getSbpData() {
            return sbpData;
        }

        @Override
        public OXPSbp.Builder setSbpData(OXPSbpData sbpData) {
            this.sbpData = sbpData;
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
        OXPSbpVer10 other = (OXPSbpVer10) obj;
        if (xid != other.xid)
            return false;
        if (sbpCmpType == null) {
            if (other.sbpCmpType != null)
                return false;
        } else if (!sbpCmpType.equals(other.sbpCmpType))
            return false;
        if (flags == null) {
            if (other.flags != null)
                return false;
        } else if (!flags.equals(other.flags))
            return false;
        if (dataLength != other.dataLength)
            return false;
        if (sbpXid != other.sbpXid)
            return false;
        if (sbpData == null) {
            if (other.sbpData != null)
                return false;
        } else if (!sbpData.equals(other.sbpData))
            return false;
        return true;
    }
}
