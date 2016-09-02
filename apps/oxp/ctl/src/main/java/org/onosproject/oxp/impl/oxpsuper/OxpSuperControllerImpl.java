package org.onosproject.oxp.impl.oxpsuper;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.DeviceId;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpMessageListener;
import org.onosproject.oxp.oxpsuper.OxpDomainListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by cr on 16-9-1.
 */
@Component(immediate = true)
@Service
public class OxpSuperControllerImpl implements OxpSuperController {

    private static final Logger log = LoggerFactory.getLogger(OxpSuperControllerImpl.class);

    private Map<DeviceId, OXPDomain> domainMap;

    private SuperConnector connector = new SuperConnector(this);
    private Set<OxpMessageListener> oxpMessageListeners = new CopyOnWriteArraySet<>();
    private Set<OxpDomainListener> oxpDomainListeners = new CopyOnWriteArraySet<>();


    protected String oxpSuperIp = "127.0.0.1";
    protected int oxpSuperPort = 6688;
    private OXPVersion oxpVersion;

    @Activate
    public void activate() {
        domainMap = new HashMap<>();
        connector.start();
        log.info("OxpSuperController started...");
    }

    @Deactivate
    public void deactivate() {
        connector.stop();
        domainMap.clear();
        log.info("OxpSuperController stoped...");
    }

    @Override
    public OXPVersion getOxpVersion() {
        return this.oxpVersion;
    }

    @Override
    public void setOxpVersion(OXPVersion oxpVersion) {
        this.oxpVersion = oxpVersion;
    }

    @Override
    public int getOxpSuperPort() {
        return oxpSuperPort;
    }

    @Override
    public void setOxpSuperPort(int oxpSuperPort) {
        this.oxpSuperPort = oxpSuperPort;
    }

    @Override
    public String getOxpSuperIp() {
        return oxpSuperIp;
    }

    @Override
    public void setOxpSuperIp(String oxpSuperIp) {
        this.oxpSuperIp = oxpSuperIp;
    }

    @Override
    public void addMessageListener(OxpMessageListener listener) {
        this.oxpMessageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(OxpMessageListener listener) {
        this.oxpMessageListeners.remove(listener);
    }

    @Override
    public void addOxpDomainListener(OxpDomainListener listener) {
        this.oxpDomainListeners.add(listener);
    }

    @Override
    public void removeOxpDomainListener(OxpDomainListener listener) {
        this.oxpDomainListeners.remove(listener);
    }

    @Override
    public void sendMsg(DeviceId deviceId, OXPMessage msg) {
        OXPDomain domain = getOxpDomain(deviceId);
        if (null != domain && domain.isConnected()) {
            domain.sendMsg(msg);
        }
    }

    @Override
    public void addDomain(DeviceId deviceId, OXPDomain domain) {
        domainMap.put(deviceId, domain);
        for (OxpDomainListener listener : oxpDomainListeners) {
            listener.domainConnected(domain);
        }
    }

    @Override
    public void removeDomain(DeviceId deviceId) {
        OXPDomain oxpDomain = getOxpDomain(deviceId);
        if (null != oxpDomain) {
            domainMap.remove(deviceId);
            for (OxpDomainListener listener : oxpDomainListeners) {
                listener.domainDisconnected(oxpDomain);
            }
        }

    }

    @Override
    public void processDownstreamMessage(List<OXPMessage> msgs) {
        for (OxpMessageListener msgListener : oxpMessageListeners) {
            msgListener.handleOutGoingMessage(msgs);
        }
    }

    @Override
    public void processMessage(OXPMessage msg) {
        for (OxpMessageListener listener : oxpMessageListeners) {
            listener.handleIncomingMessage(msg);
        }
    }

    @Override
    public OXPDomain getOxpDomain(DeviceId deviceId) {
        return domainMap.get(deviceId);
    }
}
