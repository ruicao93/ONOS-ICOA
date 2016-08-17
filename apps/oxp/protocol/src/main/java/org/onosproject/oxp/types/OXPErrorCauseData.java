package org.onosproject.oxp.types;

import com.google.common.base.Optional;
import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by cr on 16-7-17.
 */
public class OXPErrorCauseData implements Writeable, PrimitiveSinkable{
    private final static  Logger logger = LoggerFactory.getLogger(OXPErrorCauseData.class);

    public final static OXPErrorCauseData NONE = new OXPErrorCauseData(new byte[0], OXPVersion.OXP_10);

    private final byte[] data;
    private final OXPVersion version;

    private OXPErrorCauseData(byte[] data, OXPVersion version) {
        this.data = data;
        this.version = version;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public Optional<OXPMessage> getParsedMessage() {
        OXPFactory factory = OXPFactories.getFactory(version);
        try {
            OXPMessage msg = factory.getReader().readFrom(ChannelBuffers.wrappedBuffer(data));
            if(msg != null)
                return Optional.of(msg);
            else
                return Optional.absent();
        } catch (OXPParseError e) {
            logger.debug("Error parsing error cause data as OXPMessage: {}", e.getMessage(), e);
            return Optional.absent();
        }
    }

    public static OXPErrorCauseData of(byte[] data, OXPVersion version) {
        return new OXPErrorCauseData(data, version);
    }

    public static OXPErrorCauseData read(ChannelBuffer bb, int length, OXPVersion version) {
        byte[] bytes = ChannelUtils.readBytes(bb, length);
        return of(bytes, version);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putBytes(data);
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        bb.writeBytes(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OXPErrorCauseData other = (OXPErrorCauseData) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        if (version != other.version)
            return false;
        return true;
    }
}
