package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-7-18.
 */
public interface OXPEchoReply extends OXPObject,OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    byte[] getData();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPEchoReply build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        byte[] getData();
        Builder setData(byte[] data);
    }
}
