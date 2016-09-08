package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.topology.OxpDomainConfig;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.domain.OxpSuperListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.ver10.OXPCapabilitiesSerializerVer10;
import org.onosproject.oxp.protocol.ver10.OXPConfigFlagsSerializerVer10;
import org.onosproject.oxp.protocol.ver10.OXPSbpTypeSerializerVer10;
import org.onosproject.oxp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by cr on 16-8-14.
 */
@Component(immediate = true)
@Service
public class OxpDomainControllerImpl implements OxpDomainController {

    private static final Logger log = LoggerFactory.getLogger(OxpDomainControllerImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry cfgRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    protected String oxpSuperIp = "127.0.0.1";
    protected int oxpSuperPort = 6688;
    private Set<OXPConfigFlags> flags;
    private int period;
    private long missSendLen;
    private Set<OXPCapabilities> capabilities;
    private OXPSbpType oxpSbpType;
    private OXPSbpVersion oxpSbpVersion;
    private DomainId domainId;
    private OxpSuper oxpSuper;
    private OXPVersion oxpVersion;

    private DomainConnector domainConnector = new DomainConnector(this);

    private Set<OxpSuperMessageListener> oxpSuperMessageListeners = new CopyOnWriteArraySet<>();

    private Set<OxpSuperListener> oxpSuperListeners = new CopyOnWriteArraySet<>();



    @Activate
    public void activate() {
        OxpDomainConfig oxpDomainConfig = null;
        int tryTimes = 10;
        int i = 0;
        while (oxpDomainConfig == null && i < tryTimes) {
            oxpDomainConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpDomainConfig.class);
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null == oxpDomainConfig) {
            log.info("Failed to read OXPdomain config.");
            return;
        }
        initDomainCfg();
        setUpConnectionToSuper();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        oxpSuper = null;
        oxpSuperMessageListeners.clear();
        oxpSuperListeners.clear();
        disconnectFromSuper();
        log.info("Stoped");
    }

    public void initDomainCfg() {
        OxpDomainConfig domainConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpDomainConfig.class);
        //-2. set OXP Version
        this.setOxpVersion(OXPVersion.ofWireValue(domainConfig.getOxpVersion()));
        //-1.DomainId
        this.setDomainId(DomainId.of(domainConfig.getDomainId()));
        //0.super ip
        this.setOxpSuperIp(domainConfig.getSuperIp());
        //1.super port
        this.setOxpSuperPort(domainConfig.getSuperPort());
        //2.sbp type
        this.setOxpSbpType(OXPSbpTypeSerializerVer10.ofWireValue((byte) domainConfig.getSbpType()));
        //3.sbp version
        this.setOxpSbpVersion(OXPSbpVersion.of((byte) domainConfig.getSbpVersion(), getOxpVersion() ));
        //4.capabilities
        this.setCapabilities(OXPCapabilitiesSerializerVer10.ofWireValue(domainConfig.getCapabilities()));
        //5.flags
        this.setFlags(OXPConfigFlagsSerializerVer10.ofWireValue((byte) domainConfig.getFlags()));
        //6.period
        this.setPeriod(domainConfig.getPeriod());
        //7.miss send length
        this.setMissSendLen(domainConfig.getMissSendLength());
    }
    @Override
    public void processMessage(OXPMessage msg) {
        for (OxpSuperMessageListener listener : oxpSuperMessageListeners) {
            listener.handleIncomingMessage(msg);
        }
    }

    @Override
    public void processDownstreamMessage(List<OXPMessage> msgs) {
        for (OxpSuperMessageListener listener : oxpSuperMessageListeners) {
            listener.handleOutGoingMessage(msgs);
        }
    }

    @Override
    public void setUpConnectionToSuper() {
        domainConnector.start();
    }

    @Override
    public void disconnectFromSuper() {
        domainConnector.stop();
    }

    @Override
    public boolean connectToSuper(OxpSuper oxpSuper) {
        this.oxpSuper = oxpSuper;
        for (OxpSuperListener listener : oxpSuperListeners) {
            listener.connectToSuper(oxpSuper);
        }
        return true;
    }

    @Override
    public boolean loseConnectionFromSuper() {
        this.oxpSuper = null;
        for (OxpSuperListener listener : oxpSuperListeners) {
            listener.disconnectFromSuper(oxpSuper);
        }
        return true;
    }

    @Override
    public void addMessageListener(OxpSuperMessageListener listener) {
        oxpSuperMessageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(OxpSuperMessageListener listener) {
        oxpSuperMessageListeners.remove(listener);
    }

    @Override
    public void addOxpSuperListener(OxpSuperListener listener) {
        this.oxpSuperListeners.add(listener);
    }

    @Override
    public void removeOxpSuperListener(OxpSuperListener listener) {
        this.oxpSuperListeners.remove(listener);
    }

    @Override
    synchronized public void write(OXPMessage msg) {
        if (null != oxpSuper && oxpSuper.isConnected()) {
            oxpSuper.sendMsg(msg);
        }
    }

    @Override
    public void processPacket(OXPMessage msg) {

    }

    @Override
    public boolean isConnectToSuper() {
        if (null != oxpSuper && oxpSuper.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public Set<OXPConfigFlags> getFlags() {
        return this.flags;
    }

    @Override
    public void setFlags(Set<OXPConfigFlags> flags) {
        this.flags = flags;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public long getMissSendLen() {
        return missSendLen;
    }

    @Override
    public void setMissSendLen(long missSendLen) {
        this.missSendLen = missSendLen;
    }

    @Override
    public Set<OXPCapabilities> getCapabilities() {
        return this.capabilities;
    }

    @Override
    public void setCapabilities(Set<OXPCapabilities> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public DomainId getDomainId() {
        return this.domainId;
    }

    @Override
    public void setDomainId(DomainId domainId) {
        this.domainId = domainId;
    }

    @Override
    public OXPSbpType getOxpSbpTpe() {
        return oxpSbpType;
    }

    @Override
    public void setOxpSbpType(OXPSbpType oxpSbpType) {
        this.oxpSbpType = oxpSbpType;
    }

    @Override
    public OXPSbpVersion getOxpSbpVersion() {
        return this.oxpSbpVersion;
    }

    @Override
    public void setOxpSbpVersion(OXPSbpVersion oxpSbpVersion) {
        this.oxpSbpVersion = oxpSbpVersion;
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
        return this.oxpSuperIp;
    }

    @Override
    public void setOxpSuperIp(String oxpSuperIp) {
        this.oxpSuperIp = oxpSuperIp;
    }

    @Override
    public OXPVersion getOxpVersion() {
        return this.oxpVersion;
    }

    @Override
    public void setOxpVersion(OXPVersion oxpVersion) {
        this.oxpVersion = oxpVersion;
    }
}
