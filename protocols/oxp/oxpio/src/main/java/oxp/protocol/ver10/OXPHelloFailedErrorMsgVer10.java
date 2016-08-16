package oxp.protocol.ver10;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.errormsg.OXPHelloFailedErrorMsg;
import org.onosproject.oxp.types.OXPErrorCauseData;
import org.onosproject.oxp.types.U16;
import org.onosproject.oxp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cr on 16-7-17.
 */
public class OXPHelloFailedErrorMsgVer10 implements OXPHelloFailedErrorMsg {

    private final static Logger logger = LoggerFactory.getLogger(OXPHelloFailedErrorMsgVer10.class);

    final static byte WIRE_VERSION = 1;
    final static int MINIMUM_LENGTH = 12;

    private final static long DEFAULT_XID = 0x0L;
    private final static OXPErrorCauseData DEFAULT_DATA = OXPErrorCauseData.NONE;

    private final long xid;
    private final OXPHelloFailedCode code;
    private final OXPErrorCauseData data;

    OXPHelloFailedErrorMsgVer10(long xid, OXPHelloFailedCode code, OXPErrorCauseData data) {
        if(code == null) {
            throw new NullPointerException("OXPHelloFailedErrorMsgVer10: property code cannot be null");
        }
        if(data == null) {
            throw new NullPointerException("OXPHelloFailedErrorMsgVer10: property data cannot be null");
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
        return OXPErrorType.HELLO_FAILED;
    }

    @Override
    public OXPHelloFailedCode getCode() {
        return code;
    }

    @Override
    public OXPErrorCauseData getData() {
        return data;
    }



    @Override
    public OXPMessage.Builder createBuilder() {
        //TODO
        return null;
    }

    static class Builder implements OXPHelloFailedErrorMsg.Builder {
        private boolean xidSet;
        private long xid;
        private boolean codeSet;
        private OXPHelloFailedCode code;
        private boolean dataSet;
        private OXPErrorCauseData data;

        @Override
        public OXPHelloFailedErrorMsg build() {
            long xid = this.xidSet ? this.xid : DEFAULT_XID;
            if(!this.codeSet)
                throw new IllegalStateException("Property code doesn't have default value -- must be set");
            if(code == null)
                throw new NullPointerException("Property code must not be null");
            OXPErrorCauseData data = this.dataSet ? this.data : DEFAULT_DATA;
            if(data == null)
                throw new NullPointerException("Property data must not be null");
            return new OXPHelloFailedErrorMsgVer10(xid, code, data);
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
        public OXPHelloFailedErrorMsg.Builder setXid(long xid) {
            this.xid = xid;
            this.xidSet = true;
            return this;
        }

        @Override
        public OXPErrorType getErrType() {
            return OXPErrorType.HELLO_FAILED;
        }

        @Override
        public OXPHelloFailedCode getCode() {
            return code;
        }

        @Override
        public OXPHelloFailedErrorMsg.Builder setCode(OXPHelloFailedCode code) {
            this.code = code;
            this.codeSet = true;
            return this;
        }

        @Override
        public OXPErrorCauseData getData() {
            return data;
        }

        @Override
        public OXPHelloFailedErrorMsg.Builder setData(OXPErrorCauseData data) {
            this.data = data;
            this.dataSet = true;
            return this;
        }
    }

    final static Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPHelloFailedErrorMsg> {
        @Override
        public OXPHelloFailedErrorMsg readFrom(ChannelBuffer bb) throws OXPParseError {
            int start = bb.readerIndex();
            byte version = bb.readByte();
            //check version == 1
            if(version != (byte) 0x1)
                throw new OXPParseError("Wrong version: Expected=OXPVersion.OXP_10(1), got="+version);
            //check type = 1
            byte type = bb.readByte();
            if(type != (byte)0x1)
                throw new OXPParseError("Wrong type: Expected=OXPType.ERROR(1), got="+type);
            int length = U16.f(bb.readShort());
            if (length < MINIMUM_LENGTH)
                throw new OXPParseError("Wrong length: Expected to be >= " + MINIMUM_LENGTH + ", was: " + length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                //Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            //get xid
            long xid = U32.f(bb.readInt());
            //chech err type = 0
            short errType = bb.readShort();
            if(errType != (short) 0x0)
                throw new OXPParseError("Wrong errType: Expected=OXPErrorType.HELLO_FAILED(0), got="+errType);
            //get errCode
            OXPHelloFailedCode code = OXPHelloFailedCodeSerializerVer10.readFrom(bb);
            //get errData
            OXPErrorCauseData data = OXPErrorCauseData.read(bb, length - (bb.readerIndex() - start), OXPVersion.OXP_10);

            OXPHelloFailedErrorMsgVer10 helloFailedErrorMsgVer10 = new OXPHelloFailedErrorMsgVer10(xid, code, data);
            return helloFailedErrorMsgVer10;
        }
    }


    @Override
    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OXPHelloFailedErrorMsgVer10Funnel FUNNEL = new OXPHelloFailedErrorMsgVer10Funnel();
    static class OXPHelloFailedErrorMsgVer10Funnel implements Funnel<OXPHelloFailedErrorMsgVer10> {
        @Override
        public void funnel(OXPHelloFailedErrorMsgVer10 message, PrimitiveSink sink) {
            //oxp_version = 1
            sink.putByte((byte) 0x01);
            //message type = 1
            sink.putByte((byte) 0x01);
            //FIXME: skip finnel of length

            //xid
            sink.putLong(message.xid);
            //err type = 0
            sink.putShort((short) 0x0);
            //err code
            OXPHelloFailedCodeSerializerVer10.putTo(message.code, sink);
            //err data
            message.data.putTo(sink);
        }
    }



    @Override
    public void writeTo(ChannelBuffer channelBuffer) {
        WRITER.write(channelBuffer, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPHelloFailedErrorMsgVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPHelloFailedErrorMsgVer10 message) {
            int startIndex = bb.writerIndex();
            //oxp_version = 1
            bb.writeByte((byte)0x01);
            //message type = 1
            bb.writeByte((byte) 0x01);
            int lengthIndex = bb.writerIndex();
            bb.writeShort(U16.t(0));

            //xid
            bb.writeInt(U32.t(message.xid));
            //err type = 0
            bb.writeShort((short)0x0);
            //err code
            OXPHelloFailedCodeSerializerVer10.writeTo(bb, message.code);
            //err data
            message.data.writeTo(bb);

            //update length field
            int length = bb.writerIndex() - startIndex;
            bb.setShort(lengthIndex, length);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPHelloFailedErrorMsgVer10 other = (OXPHelloFailedErrorMsgVer10) obj;
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
