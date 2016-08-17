package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPHostState;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.MacAddress;
import org.onosproject.oxp.types.OXPHost;

/**
 * Created by cr on 16-7-21.
 */
abstract class OXPHostVer10 {
    static final int LENGTH = 12;

    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPHost> {
        @Override
        public OXPHost readFrom(ChannelBuffer bb) throws OXPParseError {
            IPv4Address ipAddress = IPv4Address.read4Bytes(bb);
            MacAddress macAddress = MacAddress.read6Bytes(bb);
            IPv4Address mask = IPv4Address.of(bb.readByte());
            OXPHostState state = OXPHostStateSerializerVer10.readFrom(bb);
            return OXPHost.of(ipAddress, macAddress, mask, state);
        }
    }
}
