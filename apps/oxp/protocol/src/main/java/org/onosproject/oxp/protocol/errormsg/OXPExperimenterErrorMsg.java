package org.onosproject.oxp.protocol.errormsg;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPErrorCauseData;

/**
 * Created by cr on 16-7-17.
 */
public interface OXPExperimenterErrorMsg  extends OXPObject, OXPErrorMsg {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    OXPErrorType getErrType();
    OXPErrorCauseData getData();

    void writeTo(ChannelBuffer channelBuffer);

    public interface Builder extends OXPErrorMsg.Builder {
        OXPExperimenterErrorMsg build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        OXPErrorType getErrType();
        OXPErrorCauseData getData();
        Builder setData(OXPErrorCauseData data);
    }
}
