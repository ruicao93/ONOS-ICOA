
package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import java.util.List;

/**
 * Created by cr on 16-4-7.
 */
public interface OXPHello extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    List<OXPHelloElem> getElements() throws UnsupportedOperationException;

    void writeTo(ChannelBuffer channelBuffer);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPHello build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        List<OXPHelloElem> getElements() throws UnsupportedOperationException;
        Builder setElements(List<OXPHelloElem> elements) throws UnsupportedOperationException;
    }
}
