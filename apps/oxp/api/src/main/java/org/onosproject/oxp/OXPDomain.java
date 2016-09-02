package org.onosproject.oxp;

import org.jboss.netty.channel.Channel;
import org.onosproject.net.DeviceId;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;

import java.util.List;
import java.util.Set;

/**
 * Created by cr on 16-9-1.
 */
public interface OXPDomain {

    void sendMsg(OXPMessage msg);

    void sendMsg(List<OXPMessage> msgs);

    void handleMessage(OXPMessage msg);

    OXPFactory factory();

    void setConnected(boolean isConnected);

    boolean isConnected();

    String channleId();

    void setChannel(Channel channel);

    DeviceId getDeviceId();
    void setDeviceId(DeviceId deviceId);

    Set<OXPConfigFlags> getFlags();
    void setFlags(Set<OXPConfigFlags> flags);

    int getPeriod();
    void setPeriod(int period);

    long getMissSendLen();
    void setMissSendLen(long missSendLen);

    Set<OXPCapabilities> getCapabilities();
    void setCapabilities(Set<OXPCapabilities> capabilities);

    DomainId getDomainId();
    void setDomainId(DomainId domainId);

    OXPSbpType getOxpSbpTpe();
    void setOxpSbpType(OXPSbpType oxpSbpType);

    OXPSbpVersion getOxpSbpVersion();
    void setOxpSbpVersion(OXPSbpVersion oxpSbpVersion);

    int getOxpSuperPort();
    void setOxpSuperPort(int oxpSuperPort);

    String getOxpSuperIp();
    void setOxpSuperIp(String oxpSuperIp);

    OXPVersion getOxpVersion();
    void setOxpVersion(OXPVersion oxpVersion);
}
