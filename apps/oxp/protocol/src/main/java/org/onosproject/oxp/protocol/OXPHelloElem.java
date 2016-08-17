
package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-4-7.
 */
public interface OXPHelloElem extends OXPObject {
    int getType();
    OXPVersion getVersion();

    void writeTo(ChannelBuffer channelBuffer);

    Builder createBuilder();
    public interface Builder {
        OXPHelloElem build();
        int getType();
        OXPVersion getVersion();
    }
}
