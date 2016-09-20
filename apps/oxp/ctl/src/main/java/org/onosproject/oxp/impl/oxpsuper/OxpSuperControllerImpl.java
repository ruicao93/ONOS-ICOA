package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.ChassisId;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onosproject.core.CoreService;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.OxpSuperConfig;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.oxpsuper.OxpDomainListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.ver10.OXPForwardingReplyVer10;
import org.onosproject.oxp.protocol.ver10.OXPPacketOutVer10;
import org.onosproject.oxp.types.IPv4Address;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
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
    private Map<OXPType, Long> msgCountStatis = new HashMap<>();
    private Map<OXPType, Long> msgLengthStatis = new HashMap<>();

    private OxpDomainMessageListener msgStatisListener = new InternalDomainMsgListener();
    private OxpDomainListener domainListener = new InternalDomainListener();

    private SuperConnector connector = new SuperConnector(this);
    private Set<OxpDomainMessageListener> messageListeners = new CopyOnWriteArraySet<>();
    private Set<OxpDomainListener> oxpDomainListeners = new CopyOnWriteArraySet<>();

    protected String oxpSuperIp = "127.0.0.1";
    protected int oxpSuperPort = 6688;
    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;
    private OxpSuperConfig superConfig;
    private OXPConfigFlags pathComputeParam = OXPConfigFlags.CAP_BW;
    private boolean isLoadBalance = true;

    @Activate
    public void activate() {
//        OxpSuperConfig superConfig = null;
//        int tryTimes = 10;
//        int i = 0;
//        while (superConfig == null && i < tryTimes) {
//            superConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpSuperConfig.class);
//            i++;
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        if (null == superConfig) {
//            log.info("Failed to read OXPsuper config.");
//            return;
//        }
        superConfig = cfgRegistry.getConfig(coreService.registerApplication("org.onosproject.oxpcfg"),OxpSuperConfig.class);
        if (!superConfig.getBootFlag()) {
            return;
        }
        initSuperCfg();
        domainMap = new HashMap<>();
        deviceMap = new HashMap<>();
        this.addMessageListener(msgStatisListener);
        this.addOxpDomainListener(domainListener);
        connector.start();
        log.info("OxpSuperController started...");
    }

    @Deactivate
    public void deactivate() {
        if (!superConfig.getBootFlag()) {
            return;
        }
        connector.stop();
        domainMap.clear();
        deviceMap.clear();
        msgCountStatis.clear();
        msgLengthStatis.clear();
        log.info("OxpSuperController stoped...");
    }

    public void initSuperCfg() {
        this.setOxpVersion(OXPVersion.ofWireValue(superConfig.getOxpVersin()));
        this.setOxpSuperPort(superConfig.getSuperPort());
        oxpFactory = OXPFactories.getFactory(oxpVersion);
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
    public OXPConfigFlags getPathComputeParam() {
        return pathComputeParam;
    }

    @Override
    public boolean isLoadBalance() {
        return isLoadBalance;
    }

    @Override
    public boolean setLoadBalance(boolean isLoadBalance) {
        if (isLoadBalance == false) {
            this.isLoadBalance = isLoadBalance;
            return true;
        }
        if (isLoadBalance()) return true;
        if (getPathComputeParam() != null) {
            this.isLoadBalance = isLoadBalance;
            return true;
        }
        return false;
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
    public void sendSbpPacketOut(DeviceId deviceId, PortNumber outPort, byte[] data) {
        OXPPacketOut oxpPacketOut = OXPPacketOutVer10.of((int) outPort.toLong(), data);
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.PACKET_OUT;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp sbpMsg = oxpFactory.buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) oxpPacketOut.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(oxpPacketOut)
                .build();
        sendMsg(deviceId, sbpMsg);
    }

    @Override
    public void sendSbpFwdReply(DeviceId deviceId, IpAddress srcIp, IpAddress dstIp,
                                PortNumber srcPort, PortNumber dstPort,
                                IpAddress mask, short ethType, byte qos) {
        OXPForwardingReply sbpCmpFwdReply = OXPForwardingReplyVer10.of(
                IPv4Address.of(srcIp.getIp4Address().toInt()),
                IPv4Address.of(dstIp.getIp4Address().toInt()),
                (int) srcPort.toLong(),(int) dstPort.toLong(),
                IPv4Address.of(mask.getIp4Address().toInt()),
                ethType, qos);
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.FORWARDING_REPLY;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp sbpMsg = oxpFactory.buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) sbpCmpFwdReply.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(sbpCmpFwdReply)
                .build();
        sendMsg(deviceId, sbpMsg);
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
    public long getDomainCount() {
        return domainMap.size();
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
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
        Ethernet eth = null;
        try {
            eth = Ethernet.deserializer().deserialize(buffer.array(), 0, buffer.readableBytes());
        } catch (DeserializationException e) {
            return null;
        }
        return eth;
    }

    @Override
    public Map<OXPType, Long> getMsgCountStatis() {
        return ImmutableMap.copyOf(msgCountStatis);
    }

    @Override
    public Map<OXPType, Long> getMsgLengthStatis() {
        return ImmutableMap.copyOf(msgLengthStatis);
    }

    synchronized private void updateMsgStatis(OXPType type, long newLength) {
        Long msgCount = null;
        Long msgLength = null;
        msgCount = msgCountStatis.get(type);
        if (null == msgCount) {
            msgCount = Long.valueOf(0);
        }
        msgCountStatis.put(type, ++msgCount);
        msgLength = msgLengthStatis.get(type);
        if (null == msgLength) {
            msgLength = Long.valueOf(0);
        }
        msgLengthStatis.put(type, msgLength + newLength);
    }

    class InternalDomainMsgListener implements OxpDomainMessageListener {
        @Override
        public void handleIncomingMessage(DeviceId deviceId, OXPMessage msg) {
            ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            msg.writeTo(buffer);
            updateMsgStatis(msg.getType(), buffer.readableBytes());
        }

        @Override
        public void handleOutGoingMessage(DeviceId deviceId, List<OXPMessage> msgs) {
            for (OXPMessage msg : msgs) {
                ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
                msg.writeTo(buffer);
                updateMsgStatis(msg.getType(), buffer.readableBytes());
            }
        }
    }

    class InternalDomainListener implements OxpDomainListener {
        @Override
        public void domainConnected(OXPDomain domain) {
            if (!domain.isAdvancedMode()) isLoadBalance = false;
            if (pathComputeParam == null) return;
            if (getOxpDomains().size() == 1) {
                if (domain.isCapBwSet()) {
                    pathComputeParam = OXPConfigFlags.CAP_BW;
                } else if (domain.isCapDelaySet()) {
                    pathComputeParam = OXPConfigFlags.CAP_DELAY;
                } else if (domain.isCapHopSet()) {
                    pathComputeParam = OXPConfigFlags.CAP_HOP;
                } else {
                    pathComputeParam = null;
                }
            } else {
                switch (pathComputeParam) {
                    case CAP_BW:
                        if (!domain.isCapBwSet()) pathComputeParam = null;
                        break;
                    case CAP_DELAY:
                        if (!domain.isCapDelaySet()) pathComputeParam = null;
                        break;
                    case CAP_HOP:
                        if (!domain.isCapHopSet()) pathComputeParam = null;
                        break;
                    default:
                }
            }
        }

        @Override
        public void domainDisconnected(OXPDomain domain) {

        }
    }
}
