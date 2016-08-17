package org.onosproject.oxp;

import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;

import java.util.List;
import java.util.Set;

/**
 * Created by cr on 16-8-14.
 */
public interface OxpDomainController {

    /**
     * processMessage, called by OxpSuper
     * @param msg
     */
    void processMessage(OXPMessage msg);

    void processDownstreamMessage(List<OXPMessage> msgs);

    /**
     * called by OxpSuper
     * @param oxpSuper
     * @return
     */
    boolean superConnect(OxpSuper oxpSuper);

    boolean isConnectToSuper();

    void addMessageListener(OxpSuperMessageListener listener);

    void removeMessageListener(OxpSuperMessageListener listener);

    /**
     * send a message to OxpSuperController
     * @param msg
     */
    void write(OXPMessage msg);

    /**
     * Process a msg and notify the appropriate listener.
     * @param msg
     */
    void processPacket(OXPMessage msg);

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
