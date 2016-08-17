package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPErrorCauseData;

/**
 * Created by cr on 16-7-16.
 */
public interface OXPErrorMsg extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    OXPErrorType getErrType();
    OXPErrorCauseData getData();

    void writeTo(ChannelBuffer bb);

    public interface Builder extends OXPMessage.Builder {
        OXPErrorMsg build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        OXPErrorType getErrType();
        OXPErrorCauseData getData();
    }
}
