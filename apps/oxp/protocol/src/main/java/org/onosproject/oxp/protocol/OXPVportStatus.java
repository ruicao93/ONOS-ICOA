package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-7-22.
 */
public interface OXPVportStatus extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    OXPVportReason getReason();
    OXPVportDesc getVportDesc();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPVportStatus build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        OXPVportReason getReason();
        Builder setReason(OXPVportReason reason);
        OXPVportDesc getVportDesc();
        Builder setVportDesc(OXPVportDesc vportDesc);
    }
}
