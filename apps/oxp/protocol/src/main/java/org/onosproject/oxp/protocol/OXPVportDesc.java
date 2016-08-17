package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPVport;

import java.util.Set;

/**
 * Created by cr on 16-7-21.
 */
public interface OXPVportDesc extends OXPObject {
    OXPVport getPortNo();
    Set<OXPVportState> getState();
    OXPVersion getVersion();

    void writeTo(ChannelBuffer bb);

    public interface Builder {
        OXPVportDesc build();
        OXPVport getPortNo();
        Builder setPortNo(OXPVport portNo);
        Set<OXPVportState> getState();
        Builder setState(Set<OXPVportState> state);
        OXPVersion getVersion();
    }
}
