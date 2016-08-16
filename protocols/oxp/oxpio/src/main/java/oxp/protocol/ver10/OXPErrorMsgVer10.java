package oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPErrorMsg;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.types.U16;
import org.onosproject.oxp.types.U32;

/**
 * Created by cr on 16-7-17.
 */
abstract class OXPErrorMsgVer10 {
    //version: 1.0
    final static byte WIRE_VERSION = 1;
    final static int MINIMUM_LENGTH = 10;

    public final static Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPErrorMsg> {
        @Override
        public OXPErrorMsg readFrom(ChannelBuffer bb) throws OXPParseError {
            if (bb.readableBytes() < MINIMUM_LENGTH)
                return null;
            int start = bb.readerIndex();
            byte version = bb.readByte();
            byte type = bb.readByte();
            int length = U16.f(bb.readShort());
            long xid = U32.f(bb.readInt());
            short errType = bb.readShort();
            bb.readerIndex(start);
            switch(errType) {
                case (short) 0x0:
                    return OXPHelloFailedErrorMsgVer10.READER.readFrom(bb);
                case (short) 0x1:
                    return OXPBadRequestErrorMsgVer10.READER.readFrom(bb);
                case (short) 0x2:
                    return OXPDomainConfigFailedErrorMsgVer10.READER.readFrom(bb);
                default:
                    throw new OXPParseError("Unknown value for errType of class OXPErrorMsgVer10:" + errType);
            }
        }
    }
}
