package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.ver10.OXPFactoryVer10;
import org.onosproject.oxp.types.U8;

/**
 * Created by cr on 16-4-9.
 */
public final class OXPFactories {
    private static final GenericReader GENERIC_READER = new GenericReader();

    private  OXPFactories() {

    }

    public static OXPFactory getFactory(OXPVersion version) {
        switch (version) {
            case OXP_10:
                return OXPFactoryVer10.INSTANCE;
            default:
                throw new IllegalArgumentException("Unknown version: " + version);
        }
    }

    private static class GenericReader implements OXPMessageReader<OXPMessage> {
        public OXPMessage readFrom(ChannelBuffer bb) throws OXPParseError {
            if (!bb.readable()) {
                return null;
            }
            short wireVersion = U8.f(bb.getByte(bb.readerIndex()));
            OXPFactory factory;
            switch (wireVersion) {
                case 1:
                    factory = OXPFactoryVer10.INSTANCE;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown wire version: " + wireVersion);
            }
            return factory.getReader().readFrom(bb);
        }
    }

    public static OXPMessageReader<OXPMessage> getGenericReader() {
        return GENERIC_READER;
    }
}
