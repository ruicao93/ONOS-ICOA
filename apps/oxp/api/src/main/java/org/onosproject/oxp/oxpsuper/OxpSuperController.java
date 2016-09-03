package org.onosproject.oxp.oxpsuper;

import org.onosproject.net.DeviceId;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;

import java.util.List;

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

    void addMessageListener(OxpDomainMessageListener listener);

    void removeMessageListener(OxpDomainMessageListener listener);

    void addOxpDomainListener(OxpDomainListener listener);
    void removeOxpDomainListener(OxpDomainListener listener);

    void sendMsg(DeviceId deviceId, OXPMessage msg);

    void addDomain(DeviceId deviceId, OXPDomain domain);
    void removeDomain(DeviceId deviceId);

    void processDownstreamMessage(DeviceId deviceId,List<OXPMessage> msgs);
    void processMessage(DeviceId deviceId,OXPMessage msg);

    OXPDomain getOxpDomain(DeviceId deviceId);
}
