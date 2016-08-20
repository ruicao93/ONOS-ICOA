package org.onosproject.oxp.protocol.ver10;

import com.google.common.hash.PrimitiveSink;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPVport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by cr on 16-7-21.
 */
public class OXPVportDescVer10 implements OXPVportDesc {
    public static final Logger logger = LoggerFactory.getLogger(OXPVportDescVer10.class);

    static final int LENGTH = 8;

    // OXP msg fields
    private final OXPVport portNo;
    private final Set<OXPVportState> state;

    OXPVportDescVer10(OXPVport portNo, Set<OXPVportState> state) {
        if (portNo == null)
            throw new NullPointerException("OXPVportDescVer10: property portNo cannot be null");
        if (state == null)
            throw new NullPointerException("OXPVportDescVer10: property state cannot be null");
        this.portNo = portNo;
        this.state = state;
    }

    @Override
    public OXPVport getPortNo() {
        return portNo;
    }

    @Override
    public Set<OXPVportState> getState() {
        return state;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }


    static final Reader READER = new Reader();
    static class Reader implements OXPMessageReader<OXPVportDesc> {
        @Override
        public OXPVportDesc readFrom(ChannelBuffer bb) throws OXPParseError {
            OXPVport portNo = OXPVport.ofShort(bb.readShort());
            Set<OXPVportState> state = OXPVportStateSerializerVer10.readFrom(bb);
            return new OXPVportDescVer10(portNo, state);
        }
    }

    @Override
    public void writeTo(ChannelBuffer bb) {
        WRITER.write(bb, this);
    }

    static final Writer WRITER = new Writer();
    static class Writer implements OXPMessageWriter<OXPVportDescVer10> {
        @Override
        public void write(ChannelBuffer bb, OXPVportDescVer10 message) {
            bb.writeShort(message.portNo.getPortNumber());
            OXPVportStateSerializerVer10.writeTo(bb, message.state);
        }
    }

    @Override
    public void putTo(PrimitiveSink sink) {

    }

    public static class Builder implements OXPVportDesc.Builder {
        private boolean portNoSet;
        private OXPVport portNo;
        private boolean stateSet;
        private Set<OXPVportState> state;

        @Override
        public OXPVportDesc build() {
            if (portNo == null)
                throw new NullPointerException("Property portNo must not be null");
            if (state == null)
                throw new NullPointerException("Property state must not be null");
            return new OXPVportDescVer10(portNo, state);
        }

        @Override
        public OXPVport getPortNo() {
            return portNo;
        }

        @Override
        public OXPVportDesc.Builder setPortNo(OXPVport portNo) {
            this.portNoSet = true;
            this.portNo = portNo;
            return this;
        }

        @Override
        public Set<OXPVportState> getState() {
            return state;
        }

        @Override
        public OXPVportDesc.Builder setState(Set<OXPVportState> state) {
            this.stateSet = true;
            this.state = state;
            return this;
        }

        @Override
        public OXPVersion getVersion() {
            return OXPVersion.OXP_10;
        }
    }

}
