package oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.protocol.OXPVersion;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.types.OXPVport;

/**
 * Created by cr on 16-7-21.
 */
abstract class OXPInternalLinkVer10 {
    final static int LENGTH = 12;

    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPInternalLink> {
        @Override
        public OXPInternalLink readFrom(ChannelBuffer bb) throws OXPParseError {
            OXPVport srcVport = OXPVport.readFrom(bb);
            OXPVport dstVport = OXPVport.readFrom(bb);
            long capability = bb.readLong();
            return OXPInternalLink.of(srcVport, dstVport, capability, OXPVersion.OXP_10);
        }
    }
}
