package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPHost;

import java.util.List;

/**
 * Created by cr on 16-7-21.
 */
public interface OXPHostUpdate extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    List<OXPHost> getHosts();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPHostUpdate build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        List<OXPHost> getHosts();
        Builder setHosts(List<OXPHost> hosts);
    }
}
