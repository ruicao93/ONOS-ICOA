package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.ChassisId;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreService;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.OxpSuperConfig;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.oxpsuper.OxpDomainListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPSbp;
import org.onosproject.oxp.protocol.OXPVersion;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by cr on 16-9-1.
 */
@Component(immediate = true)
@Service
public class OxpSuperControllerImpl implements OxpSuperController {

    private static final Logger log = LoggerFactory.getLogger(OxpSuperControllerImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry cfgRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private Map<DeviceId, OXPDomain> domainMap;
    private Map<DeviceId, Device> deviceMap;

    private SuperConnector connector = new SuperConnector(this);
    private Set<OxpDomainMessageListener> messageListeners = new CopyOnWriteArraySet<>();
    private Set<OxpDomainListener> oxpDomainListeners = new CopyOnWriteArraySet<>();


    protected String oxpSuperIp = "127.0.0.1";
    protected int oxpSuperPort = 6688;
    private OXPVersion oxpVersion;

    @Activate
    public void activate() {
        OxpSuperConfig superConfig = null;
        int tryTimes = 10;
        int i = 0;
        while (superConfig == null && i < tryTimes) {
            superConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpSuperConfig.class);
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null == superConfig) {
            log.info("Failed to read OXPsuper config.");
            return;
        }
        initSuperCfg();
        domainMap = new HashMap<>();
        deviceMap = new HashMap<>();
        connector.start();
        log.info("OxpSuperController started...");
    }

    @Deactivate
    public void deactivate() {
        connector.stop();
        domainMap.clear();
        deviceMap.clear();
        log.info("OxpSuperController stoped...");
    }

    public void initSuperCfg() {
        OxpSuperConfig superConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpSuperConfig.class);
        this.setOxpVersion(OXPVersion.ofWireValue(superConfig.getOxpVersin()));
        this.setOxpSuperPort(superConfig.getSuperPort());
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
    public void addMessageListener(OxpDomainMessageListener listener) {
        this.messageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(OxpDomainMessageListener listener) {
        this.messageListeners.remove(listener);
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
        Device device = new DefaultDevice(ProviderId.NONE, deviceId, Device.Type.CONTROLLER,
                "FNL", "1.0", "1.0", "001", new ChassisId(domain.getDomainId().getLong()));
        deviceMap.put(deviceId, device);
        for (OxpDomainListener listener : oxpDomainListeners) {
            listener.domainConnected(domain);
        }
    }

    @Override
    public void removeDomain(DeviceId deviceId) {
        OXPDomain oxpDomain = getOxpDomain(deviceId);
        if (null != oxpDomain) {
            domainMap.remove(deviceId);
            deviceMap.remove(deviceId);
            for (OxpDomainListener listener : oxpDomainListeners) {
                listener.domainDisconnected(oxpDomain);
            }
        }
    }

    @Override
    public void processDownstreamMessage(DeviceId deviceId,List<OXPMessage> msgs) {
        for (OxpDomainMessageListener msgListener : messageListeners) {
            msgListener.handleOutGoingMessage(deviceId, msgs);
        }
    }

    @Override
    public void processMessage(DeviceId deviceId,OXPMessage msg) {
        for (OxpDomainMessageListener listener : messageListeners) {
            listener.handleIncomingMessage(deviceId, msg);
        }
    }

    @Override
    public OXPDomain getOxpDomain(DeviceId deviceId) {
        return domainMap.get(deviceId);
    }

    @Override
    public Set<OXPDomain> getOxpDomains() {
        return ImmutableSet.copyOf(domainMap.values());
    }

    @Override
    public Device getDevice(DeviceId deviceId) {
        return deviceMap.get(deviceId);
    }

    @Override
    public Set<Device> getDevices() {
        return ImmutableSet.copyOf(deviceMap.values());
    }

    @Override
    public OFMessage parseOfMessage(OXPSbp sbp) {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        sbp.getSbpData().writeTo(buffer);
        OFMessage ofMsg = null;
        try {
            ofMsg = OFFactories.getGenericReader().readFrom(buffer);
        } catch (OFParseError e) {
            log.info(e.getMessage());
            return null;
        }
        return ofMsg;
    }
    @Override
    public Ethernet parseEthernet(byte data[]) {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        buffer.writeBytes(data);
        Ethernet eth = null;
        try {
            eth = Ethernet.deserializer().deserialize(buffer.array(), 0, buffer.readableBytes());
        } catch (DeserializationException e) {
            return null;
        }
        return eth;
    }

}
