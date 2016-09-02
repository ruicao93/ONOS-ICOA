package org.onosproject.oxp.oxpsuper;

import org.onosproject.net.DeviceId;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpMessageListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;

import java.util.List;
import java.util.Set;

/**
 * Created by cr on 16-9-1.
 */
public interface OxpSuperController {
    OXPVersion getOxpVersion();
    void setOxpVersion(OXPVersion oxpVersion);

    int getOxpSuperPort();
    void setOxpSuperPort(int oxpSuperPort);

    String getOxpSuperIp();
    void setOxpSuperIp(String oxpSuperIp);

    void addMessageListener(OxpMessageListener listener);

    void removeMessageListener(OxpMessageListener listener);

    void addOxpDomainListener(OxpDomainListener listener);
    void removeOxpDomainListener(OxpDomainListener listener);

    void sendMsg(DeviceId deviceId, OXPMessage msg);

    void addDomain(DeviceId deviceId, OXPDomain domain);
    void removeDomain(DeviceId deviceId);

    void processDownstreamMessage(List<OXPMessage> msgs);
    void processMessage(OXPMessage msg);

    OXPDomain getOxpDomain(DeviceId deviceId);
}
