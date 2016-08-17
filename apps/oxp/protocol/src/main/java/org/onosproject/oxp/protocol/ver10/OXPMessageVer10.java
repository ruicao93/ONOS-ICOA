package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPMessageReader;

/**
 * Created by cr on 16-4-9.
 */
public final class OXPMessageVer10 {
    // version: 1.0
    static final byte WIRE_VERSION = 1;
    static final int MINIMUM_LENGTH = 8;

    private OXPMessageVer10() {
    }

    public static final Reader READER = new Reader();

    static class Reader implements OXPMessageReader<OXPMessage> {
        @Override
        public OXPMessage readFrom(ChannelBuffer bb) throws OXPParseError {
            if (bb.readableBytes() < MINIMUM_LENGTH) {
                return null;
            }
            int start = bb.readerIndex();
            // fixed value property version == 1
            byte version = bb.readByte();
            if (version != (byte) 0x1) {
                throw new OXPParseError("Wrong version: Expected=OFVersion.OF_10(1), got=" + version);
            }
            byte type = bb.readByte();
            bb.readerIndex(start);
            switch (type) {
                case (byte) 0x0:
                    // discriminator value OXPType=0 for class OXPHelloVer10
                    return OXPHelloVer10.READER.readFrom(bb);
                case (byte) 0x1:
                    // discriminator value OXPType=1 for class OXPErrorVer10
                    return OXPErrorMsgVer10.READER.readFrom(bb);
                case (byte) 0x2:
                    // discriminator value OXPType=2 for class OXPEchoRequestVer10
                    return OXPEchoRequestVer10.READER.readFrom(bb);
                case (byte) 0x3:
                    // discriminator value OXPType=3 for class OXPEchoReplyVer10
                    return OXPEchoReplyVer10.READER.readFrom(bb);
                case (byte) 0x4:
                    // discriminator value OXPType=4 for class OXPFeaturesRequestVer10
                    throw new OXPParseError("Unknown value for discriminator type of class OXPMessageVer10: " + type);
                case (byte) 0x5:
                    // discriminator value OXPType=5 for class OXPFeaturesRequestVer10
                    return OXPFeaturesRequestVer10.READER.readFrom(bb);
                case (byte) 0x6:
                    // discriminator value OXPType=6 for class OXPFeaturesReplyVer10
                    return OXPFeaturesReplyVer10.READER.readFrom(bb);
                case (byte) 0x7:
                    // discriminator value OXPType=7 for class OXPGetConfigRequestVer10
                    return OXPGetConfigRequestVer10.READER.readFrom(bb);
                case (byte) 0x8:
                    // discriminator value OXPType=8 for class OXPGetConfigReplyVer10
                    return OXPGetConfigReplyVer10.READER.readFrom(bb);
                case (byte) 0x9:
                    // discriminator value OXPType=9 for class OXPSetConfigVer10
                    return OXPSetConfigVer10.READER.readFrom(bb);
                case (byte) 0xa:
                    // discriminator value OXPType=10 for class OXPTopologyRequestVer10
                    return OXPTopologyRequestVer10.READER.readFrom(bb);
                case (byte) 0xb:
                    // discriminator value OXPType=11 for class OXPSetConfigVer10
                    return OXPTopologyReplyVer10.READER.readFrom(bb);
                case (byte) 0xc:
                    // discriminator value OXPType=12 for class OXPHostRequestVer10
                    return OXPHostRequestVer10.READER.readFrom(bb);
                case (byte) 0xd:
                    // discriminator value OXPType=13 for class OXPHostReplyVer10
                    return OXPHostReplyVer10.READER.readFrom(bb);
                case (byte) 0xe:
                    // discriminator value OXPType=14 for class OXPHostUpdateVer10
                    return OXPHostUpdateVer10.READER.readFrom(bb);
                case (byte) 0xf:
                    // discriminator value OXPType=15 for class OXPVportStatusVer10
                    return OXPVportStatusVer10.READER.readFrom(bb);
                case (byte) 0x10:
                    // discriminator value OXPType=16 for class OXPVportStatusVer10
                    return OXPSbpVer10.READER.readFrom(bb);
                default:
                    throw new OXPParseError("Unknown value for discriminator type of class OXPMessageVer10: " + type);
            }
        }
    }
}
