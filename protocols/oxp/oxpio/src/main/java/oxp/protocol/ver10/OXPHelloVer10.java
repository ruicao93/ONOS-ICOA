
package oxp.protocol.ver10;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.U16;
import org.onosproject.oxp.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cr on 16-4-7.
 */
public class OXPHelloVer10 implements OXPHello {

    private static final Logger log = LoggerFactory.getLogger(OXPHelloVer10.class);

    static final byte WIRE_VERSION = 1;
    static final int LENGTH = 8;

    private static final long DEFAULT_XID = 0x0L;
    private final long xid;

    static final OXPHelloVer10 DEFAULT = new OXPHelloVer10(DEFAULT_XID);

    OXPHelloVer10(long xid) {
        this.xid = xid;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public OXPType getType() {
        return OXPType.OXPT_HELLO;
    }

    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public List<OXPHelloElem> getElements() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property elements not supported in version 1.0");
    }

    public OXPHello.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OXPHello.Builder {
        final OXPHelloVer10 parentMessage;

        private boolean xidSet;
        private long xid;

        BuilderWithParent(OXPHelloVer10 parentMessage) {
            this.parentMessage = parentMessage;
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_HELLO;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPHello.Builder setXid(long xxid) {
            this.xid = xxid;
            this.xidSet = true;
            return this;
        }

        @Override
        public List<OXPHelloElem> getElements() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property elements not supported in version 1.0");
        }

        @Override
        public OXPHello.Builder setElements(List<OXPHelloElem> elements) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property elements not supported in version 1.0");
        }

        @Override
        public OXPHello build() {
            long xxid = xidSet ? this.xid : parentMessage.xid;
            return new OXPHelloVer10(xxid);
        }
    }

    static class Builder implements OXPHello.Builder {
        private long xid;
        private boolean xidSet;

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }

        @Override
        public OXPType getType() {
            return OXPType.OXPT_HELLO;
        }

        @Override
        public long getXid() {
            return xid;
        }

        @Override
        public OXPHello.Builder setXid(long xxid) {
            this.xid = xxid;
            this.xidSet = true;
            return this;
        }

        @Override
        public List<OXPHelloElem> getElements() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property elements not supported in version 1.0");
        }

        @Override
        public OXPHello.Builder setElements(List<OXPHelloElem> elements) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Property elements not supported in version 1.0");
        }

        @Override
        public OXPHello build() {
            long xxid = this.xidSet ? this.xid : DEFAULT_XID;
            return new OXPHelloVer10(xxid);
        }
    }

    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPHello> {
        @Override
        public OXPHello readFrom(ChannelBuffer bb) throws OXPParseError {
            int start = bb.readerIndex();
            // fixed value property version == 1
            byte version = bb.readByte();
            if (version != (byte) 0x1) {
                throw new OXPParseError("Wrong version: Expected=OXPVersion.OF_10(1), got=" + version);
            }
            // fixed value property type == 0
            byte type = bb.readByte();
            if (type != (byte) 0x0) {
                throw new OXPParseError("Wrong type: Expected=OXPType.HELLO(0), got=" + type);
            }
            int length = U16.f(bb.readShort());
            if (length != 8) {
                throw new OXPParseError("Wrong length: Expected=8(8), got=" + length);
            }
            if (bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if (log.isTraceEnabled()) {
                log.trace("readFrom - length={}", length);
            }
            long xid = U32.f(bb.readInt());

            OXPHelloVer10 helloVer10 = new OXPHelloVer10(
                    xid
            );
            if (log.isTraceEnabled()) {
                log.trace("readFrom - read={}", helloVer10);
            }
            return helloVer10;
        }
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    static final OXPHelloVer10Funnel FUNNEL = new OXPHelloVer10Funnel();
    static class OXPHelloVer10Funnel implements Funnel<OXPHelloVer10> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OXPHelloVer10 message, PrimitiveSink sink) {
            // fixed value property version = 1
            sink.putByte((byte) 0x1);
            // fixed value property type = 0
            sink.putByte((byte) 0x0);
            // fixed value property length = 8
            sink.putShort((short) 0x8);
            sink.putLong(message.xid);
        }
    }

    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPHelloVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPHelloVer10 message) {
            // fixed value property version = 1
            bb.writeByte((byte) 0x1);
            // fixed value property type = 0
            bb.writeByte((byte) 0x0);
            // fixed value property length = 8
            bb.writeShort((short) 0x8);
            bb.writeInt(U32.t(message.xid));
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OXPHelloVer10(");
        b.append("xid=").append(xid);
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OXPHelloVer10 other = (OXPHelloVer10) obj;

        if (xid != other.xid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime *  (int) (xid ^ (xid >>> 32));
        return result;
    }
}
