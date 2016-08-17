package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-7-19.
 */
public interface OXPFeaturesRequest extends OXPObject, OXPMessage, OXPRequest<OXPFeaturesReply> {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPFeaturesRequest build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
    }
}
