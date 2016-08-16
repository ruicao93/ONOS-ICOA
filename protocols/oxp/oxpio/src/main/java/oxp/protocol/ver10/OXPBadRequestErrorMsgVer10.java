package oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.errormsg.OXPBadRequestErrorMsg;
import org.onosproject.oxp.types.OXPErrorCauseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cr on 16-7-18.
 */
public class OXPBadRequestErrorMsgVer10 implements OXPBadRequestErrorMsg {

    private final static Logger logger = LoggerFactory.getLogger(OXPBadRequestErrorMsgVer10.class);

    //version = 1.0
    final static byte WIRE_VERSION = 1;
    final static int MINIMUM_LENGTH = 12;

    private static final long DEFAULT_XID = 0x01;
    private static final OXPErrorCauseData DEFAULT_DATA = OXPErrorCauseData.NONE;

    //OXP msg fields
    private final long xid;
    private final OXPBadRequestCode code;
    private final OXPErrorCauseData data;

    OXPBadRequestErrorMsgVer10(long xid, OXPBadRequestCode code, OXPErrorCauseData data) {
        if (code == null) {
            throw new NullPointerException("OXPBadRequestErrorMsgVer10: property code cannot be null");
        }
        if (data == null) {
            throw new NullPointerException("OXPBadRequestErrorMsgVer10: property data cannot be null");
        }
        this.xid = xid;
        this.code = code;
        this.data = data;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_ERROR;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public OXPErrorType getErrType() {
        return OXPErrorType.BAD_REQUEST;
    }

    @Override
    public OXPBadRequestCode getCode() {
        return code;
    }

    @Override
    public OXPErrorCauseData getData() {
        return data;
    }

    final static Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPBadRequestErrorMsg> {
        @Override
        public OXPBadRequestErrorMsg readFrom(ChannelBuffer bb) throws OXPParseError{
            int start = bb.readerIndex();
            //version
            byte version = bb.readByte();
            //type
            byte type = bb.readByte();
            //length
            short length = bb.readShort();
            if (length < MINIMUM_LENGTH)
                throw new OXPParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            if (bb.readableBytes() + (bb.readerIndex() - start) < length) {
                bb.readerIndex(start);
                return null;
            }
            //xid
            long xid = bb.readInt();
            //errType
            short errType = bb.readShort();
            //errCode
            OXPBadRequestCode code = OXPBadRequestCodeSerializerVer10.readFrom(bb);
            //errData
            OXPErrorCauseData data = OXPErrorCauseData.read(bb, length - (bb.readerIndex() - start), OXPVersion.OXP_10);
            OXPBadRequestErrorMsg badRequestErrorMsg = new OXPBadRequestErrorMsgVer10(xid, code, data);
            return badRequestErrorMsg;
        }
    }
    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPBadRequestErrorMsgVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPBadRequestErrorMsgVer10 message){
            int startIndex = bb.writerIndex();
            //version = 1
            bb.writeByte(0x1);
            //type = 1
            bb.writeByte(0x1);
            //tmp length
            int lengthIndex = bb.writerIndex();
            bb.writeShort(0);
            //xid
            bb.writeInt((int)message.xid);
            //errType = 1
            bb.writeShort(0x1);
            //errCode
            OXPBadRequestCodeSerializerVer10.writeTo(bb, message.code);
            //errData
            message.data.writeTo(bb);
            //update length
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    @Override
    public OXPMessage.Builder createBuilder() {
        return null;
    }
    static class Builder implements OXPBadRequestErrorMsg.Builder {
        private boolean xidSet;
        private long xid;
        private boolean codeSet;
        private OXPBadRequestCode code;
        private boolean dataSet;
        private OXPErrorCauseData data;

        @Override
        public OXPBadRequestErrorMsg build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            if (!this.codeSet)
                throw new IllegalStateException("Property code doesn't have default value -- must be set");
            if (code == null)
                throw new NullPointerException("Property code must not be null");
            OXPErrorCauseData data = this.dataSet ? this.data : DEFAULT_DATA;
            if (data == null)
                throw new NullPointerException("Property data must not be null");
            return new OXPBadRequestErrorMsgVer10(xid, code, data);
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_ERROR;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPBadRequestErrorMsg.Builder setXid(long xid) {
            this.xidSet = true;
            this.xid = xid;
            return this;
        }

        @Override
        public OXPErrorType getErrType() {
            return OXPErrorType.BAD_REQUEST;
        }

        @Override
        public OXPBadRequestCode getCode() {
            return code;
        }

        @Override
        public OXPBadRequestErrorMsg.Builder setCode(OXPBadRequestCode code) {
            this.codeSet = true;
            this.code = code;
            return this;
        }

        @Override
        public OXPErrorCauseData getData() {
            return data;
        }

        @Override
        public OXPBadRequestErrorMsg.Builder setData(OXPErrorCauseData data) {
            this.dataSet = true;
            this.data = data;
            return this;
        }
    }
    @Override
    public void putTo(PrimitiveSink sink) {
        //TODO
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPBadRequestErrorMsgVer10 other = (OXPBadRequestErrorMsgVer10) obj;
        if ( xid != other.xid)
            return false;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }
}
