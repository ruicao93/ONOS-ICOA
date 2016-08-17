package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPSbpData;

import java.util.Set;

/**
 * Created by cr on 16-7-22.
 */
public interface OXPSbp extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    OXPSbpCmpType getSbpCmpType();
    Set<OXPSbpFlags> getFlags();
    short getDataLength();
    long getSbpXid();
    OXPSbpData getSbpData();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPSbp build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        OXPSbpCmpType getSbpCmpType();
        Builder setSbpCmpType(OXPSbpCmpType sbpCmpType);
        Set<OXPSbpFlags> getFlags();
        Builder setFlags(Set<OXPSbpFlags> flags);
        short getDataLength();
        Builder setDataLength(short length);
        long getSbpXid();
        Builder setSbpXid(long sbpXid);
        OXPSbpData getSbpData();
        Builder setSbpData(OXPSbpData sbpData);
    }
}
