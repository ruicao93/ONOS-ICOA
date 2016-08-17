package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-7-21.
 */
public interface OXPTopologyRequest extends OXPObject, OXPMessage, OXPRequest<OXPTopologyReply> {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPTopologyRequest build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
    }
}
