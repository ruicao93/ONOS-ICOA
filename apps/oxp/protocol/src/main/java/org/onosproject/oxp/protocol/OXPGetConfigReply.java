package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import java.util.Set;

/**
 * Created by cr on 16-7-21.
 */
public interface OXPGetConfigReply extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    Set<OXPConfigFlags> getFlags();
    byte getPeriod();
    short getMissSendLength();

    void writeTo(ChannelBuffer bb);

    public interface Builder extends OXPMessage.Builder {
        OXPGetConfigReply build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        Set<OXPConfigFlags> getFlags();
        Builder setFlags(Set<OXPConfigFlags> flags);
        byte getPeriod();
        Builder setPeriod(byte period);
        short getMissSendLength();
        Builder setMissSendLength(short missSendLength);
    }
}
