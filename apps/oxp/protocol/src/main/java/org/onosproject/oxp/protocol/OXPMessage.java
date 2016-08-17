
package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-4-6.
 */
public interface OXPMessage extends OXPObject {

    OXPVersion getVersion();
    OXPType getType();
    long getXid();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder {
        OXPMessage build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
    }
}
