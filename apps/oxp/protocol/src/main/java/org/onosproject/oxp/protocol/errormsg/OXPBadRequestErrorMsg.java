package org.onosproject.oxp.protocol.errormsg;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPErrorCauseData;

/**
 * Created by cr on 16-7-17.
 */
public interface OXPBadRequestErrorMsg  extends OXPObject, OXPErrorMsg {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    OXPErrorType getErrType();
    OXPBadRequestCode getCode();
    OXPErrorCauseData getData();

    void writeTo(ChannelBuffer bb);

    public interface Builder extends OXPErrorMsg.Builder {
        OXPBadRequestErrorMsg build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        OXPErrorType getErrType();
        OXPBadRequestCode getCode();
        Builder setCode(OXPBadRequestCode code);
        OXPErrorCauseData getData();
        Builder setData(OXPErrorCauseData data);
    }
}
