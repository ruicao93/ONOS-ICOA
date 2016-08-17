package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.DomainId;

import java.util.Set;

/**
 * Created by cr on 16-7-19.
 */
public interface OXPFeaturesReply extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    DomainId getDomainId();
    OXPSbpType getSbpType();
    OXPSbpVersion getSbpVsesion();
    Set<OXPCapabilities> getCapabilities();

    void writeTo(ChannelBuffer bb);

    public interface Builder extends OXPMessage.Builder {
        OXPFeaturesReply build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        DomainId getDomainId();
        Builder setDomainId(DomainId domainId);
        OXPSbpType getSbpType();
        Builder setSbpType(OXPSbpType sbpType);
        OXPSbpVersion getSbpVsesion();
        Builder setSbpVersion(OXPSbpVersion sbpVersion);
        Set<OXPCapabilities> getCapabilities();
        Builder setCapabilities(Set<OXPCapabilities> set);
    }
}
