package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.onosproject.core.CoreService;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.domain.OxpSuperListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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

    private static final String APP_ID = "org.onosproject.oxp";

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Activate
    public void activate() {
        //coreService.registerApplication(APP_ID);
        this.setOxpVersion(OXPVersion.OXP_10);
        //-1.DomainId
        this.setDomainId(DomainId.of(4));
        //0.super ip
        this.setOxpSuperIp("127.0.0.1");
        //1.super port
        this.setOxpSuperPort(6688);
        //2.sbp type
        this.setOxpSbpType(OXPSbpType.OPENFLOW);
        //3.sbp version
        this.setOxpSbpVersion(OXPSbpVersion.of((byte)4, OXPVersion.OXP_10 ));
        //4.capabilities
        Set<OXPCapabilities> capabilities = new HashSet<>();
        capabilities.add(OXPCapabilities.GROUP_STATS);
        capabilities.add(OXPCapabilities.IP_REASM);
        capabilities.add(OXPCapabilities.PORT_BLOCKED);
        capabilities.add(OXPCapabilities.PORT_STATS);
        capabilities.add(OXPCapabilities.QUEUE_STATS);
        capabilities.add(OXPCapabilities.FLOW_STATS);
        capabilities.add(OXPCapabilities.TABLE_STATS);
        this.setCapabilities(capabilities);
        //5.flags
        Set<OXPConfigFlags> flags = new HashSet<>();
        flags.add(OXPConfigFlags.CAP_BW);
        this.setFlags(flags);
        //6.period
        this.setPeriod(5);
        //7.miss send length
        this.setMissSendLen(128);
        domainConnector.start();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        oxpSuper = null;
        oxpSuperMessageListeners.clear();
        oxpSuperListeners.clear();
        domainConnector.stop();
        log.info("Stoped");
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
    public boolean connectToSuper(OxpSuper oxpSuper) {
        this.oxpSuper = oxpSuper;
        for (OxpSuperListener listener : oxpSuperListeners) {
            listener.connectToSuper(oxpSuper);
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
    public void write(OXPMessage msg) {
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
