package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.ONOSLLDP;
import org.onlab.packet.OXPLLDP;
import org.onosproject.cluster.ClusterMetadataService;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.domain.OxpDomainTopoService;
import org.onosproject.oxp.domain.OxpSuperListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.ver10.OXPVportDescVer10;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.types.OXPSbpData;
import org.onosproject.oxp.types.OXPVport;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.onlab.packet.Ethernet.TYPE_LLDP;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;
import static org.slf4j.LoggerFactory.getLogger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by cr on 16-8-18.
 */
@Component(immediate = true)
@Service
public class OxpDomainTopoManager implements OxpDomainTopoService {

    private final Logger log = getLogger(getClass());

    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;
    private OFVersion ofVersion;
    private OFFactory ofFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OxpDomainController domainController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterMetadataService clusterMetadataService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PortStatisticsService portStatisticsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    //TODO 需要添加Device监听,当有设备下线时,更新vport列表和links列表.

    private LinkListener linkListener = new InternalLinkListener();
    private OxpSuperMessageListener oxpMsgListener = new InternalOxpSuperMsgListener();
    private OxpSuperListener oxpSuperListener = new InternalOxpSuperListener();
    private PacketProcessor oxpLlapPacketProcessor = new InternalPacketProcessor();
    private DeviceListener deviceListener = new InternalDeviceListener();

    private AtomicLong vportNo = new AtomicLong(1);
    private Map<ConnectPoint, PortNumber> vportMap = new HashMap<>();
    private Map<PortNumber, Long> vportCapabilityMap = new HashMap<>();
    private Map<ConnectPoint, PortNumber> vportAllocateCache = new HashMap<>();
    private Set<Link> intraLinkSet = new HashSet<>();
    private boolean bootFlag = false;

    private ScheduledExecutorService executor;

    private final static int LLDP_VPORT_LOCAL = 0xffff;
    private final static long DEFAULT_VPORT_CAP = 0;

    @Activate
    public void activate() {
        domainController.addOxpSuperListener(oxpSuperListener);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        domainController.removeOxpSuperListener(oxpSuperListener);
        if (!bootFlag) {
            return;
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        linkService.removeListener(linkListener);
        domainController.removeMessageListener(oxpMsgListener);
        packetService.removeProcessor(oxpLlapPacketProcessor);
        deviceService.removeListener(deviceListener);
        vportMap.clear();
        vportCapabilityMap.clear();
        intraLinkSet.clear();
        vportAllocateCache.clear();
        log.info("Stoped");
    }

    private void setUp() {
        bootFlag = true;
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        ofVersion = OFVersion.OF_13;
        ofFactory = OFFactories.getFactory(ofVersion);
        domainController.addMessageListener(oxpMsgListener);
        linkService.addListener(linkListener);
        deviceService.addListener(deviceListener);
        packetService.addProcessor(oxpLlapPacketProcessor, PacketProcessor.advisor(0));
        executor = newSingleThreadScheduledExecutor(groupedThreads("oxp/topoupdate", "oxp-topoupdate-%d", log));
        executor.scheduleAtFixedRate(new TopoUpdateTask(),
                domainController.getPeriod(), domainController.getPeriod(), SECONDS);
    }

    private void sendTopoReplyMsg(List<OXPInternalLink> oxpInternalLinks) {

        //TODO
    }


    private PortNumber allocateVportNo(ConnectPoint location) {
        if (vportAllocateCache.containsKey(location)) {
            return vportAllocateCache.get(location);
        } else {
            PortNumber newVportNum = portNumber(vportNo.getAndIncrement());
            vportAllocateCache.put(location, newVportNum);
            return newVportNum;
        }
    }
    private void addOrUpdateVport(ConnectPoint edgeConnectPoint, OXPVportState vportState, OXPVportReason reason) {
        checkNotNull(edgeConnectPoint);
        if (reason.equals(OXPVportReason.ADD) && !vportMap.containsKey(edgeConnectPoint)) {
            // 添加Vport
            // 1.分配Vport号,并记录<ConnectPoint, vportNo>
            PortNumber allocatedVportNum = allocateVportNo(edgeConnectPoint);
            vportMap.put(edgeConnectPoint, allocatedVportNum);
            vportCapabilityMap.put(allocatedVportNum, DEFAULT_VPORT_CAP);
        }
        // 1.获取对应的vportNum
        PortNumber vportNum = vportMap.get(edgeConnectPoint);
        // 2.构造vportStatus消息:
        //    Reason:Add, State:Live
        OXPVport vport = OXPVport.ofShort((short) vportNum.toLong());
        Set<OXPVportState> state = new HashSet<>();
        state.add(vportState);
        OXPVportDesc vportDesc = new OXPVportDescVer10.Builder().setPortNo(vport)
                .setState(state)
                .build();
        OXPVportStatus msg = oxpFactory.buildVportStatus()
                .setReason(reason)
                .setVportDesc(vportDesc)
                .build();
        // 3.发送vportStatus消息到Super
        domainController.write(msg);
        if (reason.equals(OXPVportReason.DELETE)) {
            vportMap.remove(edgeConnectPoint);
        }
        updateTopo();
    }

    private void updateTopo() {
        // if mode is Advanced: synchronize intra links
        // else if mode is Simple: only send information of vport

        List<OXPInternalLink> internalLinks = new ArrayList<>();
        Set<PortNumber> hasHandledVport = new HashSet<>();
        for (ConnectPoint srcConnectPoint : vportMap.keySet()) {
            PortNumber srcVport = vportMap.get(srcConnectPoint);
            OXPVport srcVportDesc = OXPVport.ofShort((short) srcVport.toLong());
            long srcVportMaxCapability = getVportMaxCapability(srcConnectPoint);
            long srcVportLoadCapability = getVportLoadCapability(srcConnectPoint);
            for (ConnectPoint dstConnectPoint : vportMap.keySet()) {
                PortNumber dstVport = vportMap.get(dstConnectPoint);
                OXPVport dstVportDesc = OXPVport.ofShort((short) dstVport.toLong());
                if (srcVport.equals(dstVport) && !hasHandledVport.contains(srcVport)) {
                    hasHandledVport.add(srcVport);
                    internalLinks.add(OXPInternalLink.of(srcVportDesc, dstVportDesc, srcVportMaxCapability, OXPVersion.OXP_10));
                    internalLinks.add(OXPInternalLink.of(srcVportDesc, OXPVport.LOCAL, srcVportLoadCapability, OXPVersion.OXP_10));
                    if (!domainController.isAdvancedMode()) {
                        break;
                    }
                } else {
                    if (!domainController.isAdvancedMode()) {
                        continue;
                    }
                    if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())) {
                        long linkCapability = getIntraLinkCapability(srcConnectPoint, dstConnectPoint);
                        internalLinks.add(OXPInternalLink.of(srcVportDesc, dstVportDesc,
                                linkCapability, OXPVersion.OXP_10));
                    } else if (!pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId()).isEmpty()) {
                        long linkCapability = getIntraLinkCapability(srcConnectPoint, dstConnectPoint);
                        internalLinks.add(OXPInternalLink.of(srcVportDesc, dstVportDesc,
                                linkCapability, OXPVersion.OXP_10));
                    }
                }
            }
        }
        // 将internalLinks发送至Super
        OXPTopologyReply topologyReply = oxpFactory
                .buildTopologyReply()
                .setInternalLink(internalLinks)
                .build();
        domainController.write(topologyReply);
    }

    private long getVportLoadCapability(ConnectPoint connectPoint) {
        if (domainController.isCapBwSet()) {
            long vportCurSpeed =0;
            try {
                vportCurSpeed = portStatisticsService.load(connectPoint).rate() * 8;//data source: Bps
            } catch (Exception e) {
                log.info("Get port rate error.");
                return 0;
            }
            return vportCurSpeed;
        } else if (domainController.isCapDelaySet()) {
            return 0;
        } else {
            // hop flag is set
            return 0;
        }
    }

    private long getVportRestCapability(ConnectPoint connectPoint) {
        return getVportMaxCapability(connectPoint) - getVportLoadCapability(connectPoint);
    }

    private long getVportMaxCapability(ConnectPoint connectPoint) {
        if (domainController.isCapBwSet()) {
            Port port = deviceService.getPort(connectPoint.deviceId(), connectPoint.port());
            long vportMaxSpeed = port.portSpeed() * 1000 * 1000;  //Bps

            return vportMaxSpeed;
        } else if (domainController.isCapDelaySet()) {
            return 0;
        } else {
            // hop flag is set
            return 0;
        }
    }

    private long getIntraLinkCapability(ConnectPoint srcConnectPoint, ConnectPoint dstConnectPoint) {
        if (domainController.isCapBwSet()) {
            if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())
                    || !pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId()).isEmpty()) {
                long srcVportCap = getVportRestCapability(srcConnectPoint);
                long dstVportCap = getVportRestCapability(dstConnectPoint);
                return Long.min(srcVportCap, dstVportCap);
            } else {
                return Long.MIN_VALUE;
            }

        } else if (domainController.isCapDelaySet()) {
            return 0;
        } else {
            // hop flag is set
            if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())) {
                return 0;
            }
            Set<Path> path = pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId());
            if (!path.isEmpty()) {
                return ((Path) path.toArray()[0]).links().size();
            } else {
                return Long.MAX_VALUE;
            }
        }

    }

    public PortNumber getLogicalVportNum(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint) ? vportMap.get(connectPoint) : PortNumber.portNumber(OXPVport.LOCAL.getPortNumber());
    }

    @Override
    public boolean isOuterPort(ConnectPoint connectPoint) {
        return vportMap.containsKey(connectPoint);
    }

    private PortNumber getVportNum(ConnectPoint edgeConnectPoint) {
        return vportMap.get(edgeConnectPoint);
    }

    @Override
    public ConnectPoint getLocationByVport(PortNumber portNum) {
        for (ConnectPoint connectPoint : vportMap.keySet()) {
            if (vportMap.get(connectPoint).equals(portNum)) {
                return connectPoint;
            }
        }
        return null;
    }

    private final String buildSrcMac() {
        String srcMac = ProbedLinkProvider.fingerprintMac(clusterMetadataService.getClusterMetadata());
        String defMac = ProbedLinkProvider.defaultMac();
        if (srcMac.equals(defMac)) {
            log.warn("Couldn't generate fingerprint. Using default value {}", defMac);
            return defMac;
        }
        log.trace("Generated MAC address {}", srcMac);
        return srcMac;
    }



    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            //TODO handle link event
        }
    }

    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
           //TODO
            if (msg.getType() != OXPType.OXPT_TOPO_REQUEST) {
                return;
            }
            List<OXPInternalLink> internalLinks = new ArrayList<>();
            for (ConnectPoint srcConnectPoint : vportMap.keySet()) {
                PortNumber srcVportNum = vportMap.get(srcConnectPoint);
                for (ConnectPoint dstConnectPoint : vportMap.keySet()) {
                    PortNumber dstVportNum = vportMap.get(dstConnectPoint);
                    if (pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId()).size() > 0) {
                        OXPVport srcOxpVport = OXPVport.ofShort((short) srcVportNum.toLong());
                        OXPVport dstOxpVport = OXPVport.ofShort((short) dstVportNum.toLong());
                        internalLinks.add(OXPInternalLink.of(srcOxpVport, dstOxpVport, PortSpeed.SPEED_10MB.getSpeedBps(), OXPVersion.OXP_10));
                    }
                }
            }
            if (internalLinks.size() == 0) {
                return;
            }
            OXPTopologyReply topologyReply = oxpFactory
                    .buildTopologyReply()
                    .setInternalLink(internalLinks)
                    .build();
            domainController.write(topologyReply);
        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }

    private class InternalOxpSuperListener implements OxpSuperListener {
        @Override
        public void connectToSuper(OxpSuper oxpSuper) {
            //TODO handle super online
            setUp();
        }

        @Override
        public void disconnectFromSuper(OxpSuper oxpSuper) {

        }
    }

    private class InternalPacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            if (context.isHandled()) {
                return;
            }
            Ethernet eth = context.inPacket().parsed();
            if (eth == null || (eth.getEtherType() != TYPE_LLDP)) {
                return;
            }
            if (!domainController.isConnectToSuper()) {
                return;
            }
            OXPLLDP oxplldp = OXPLLDP.parseOXPLLDP(eth);
            if (null == oxplldp) {
                return;
            }
            PortNumber srcPort = portNumber(oxplldp.getPortNum());
            PortNumber dstPort = context.inPacket().receivedFrom().port();
            DeviceId srcDeviceId = DeviceId.deviceId("of:" + oxplldp.getDpid());
            DeviceId dstDeviceId = context.inPacket().receivedFrom().deviceId();
            ConnectPoint edgeConnectPoint = new ConnectPoint(dstDeviceId, dstPort);

            // 若domainId相同,说明来自同一个Domain,不做处理
            if (oxplldp.getDomainId() == domainController.getDomainId().getLong()) {
                context.block();
                return;
            }
            // Vport为Local,说明对面端口尚未被发现为Vport,需要回复本地虚拟端口号的lldp,
            // 让对面端口被发现为Vport,并且上报lldp,供邻间链路发现
            if (LLDP_VPORT_LOCAL == oxplldp.getVportNum()) {
                // allocate vport_no and send msg to super
                addOrUpdateVport(edgeConnectPoint, OXPVportState.LIVE, OXPVportReason.ADD);
                OXPLLDP replyOxplldp = OXPLLDP.oxpLLDP(Long.valueOf(dstDeviceId.toString().substring("of:".length()),16),
                          Long.valueOf(dstPort.toLong()).intValue(),
                        domainController.getDomainId().getLong(),
                        Long.valueOf(getVportNum(edgeConnectPoint).toLong()).intValue());
                Ethernet ethPacket = new Ethernet();
                ethPacket.setEtherType(Ethernet.TYPE_LLDP);
                ethPacket.setDestinationMACAddress(ONOSLLDP.LLDP_NICIRA);
                ethPacket.setPad(true);
                ethPacket.setSourceMACAddress(buildSrcMac()).setPayload(replyOxplldp);
                OutboundPacket outboundPacket = new DefaultOutboundPacket(dstDeviceId,
                        builder().setOutput(dstPort).build(),
                        ByteBuffer.wrap(ethPacket.serialize()));
                packetService.emit(outboundPacket);
                context.block();
            }else {
                //若lldp包携带对端vport号,则需将此lldp上报Super,以使Super可以发现邻间链路
                if (null == getVportNum(edgeConnectPoint)) {
                    addOrUpdateVport(edgeConnectPoint, OXPVportState.LIVE, OXPVportReason.ADD);
                }
                //为隐蔽domain域内信息,重写lldp,将portNo改为VportNo
                // Send lldp to Super throuth SBP message
                // build packet_in from lldp
                OXPLLDP sbpOxplldp = OXPLLDP.oxpLLDP(oxplldp.getDomainId(),
                        oxplldp.getVportNum(),
                        oxplldp.getDomainId(),
                        oxplldp.getVportNum());
                Ethernet ethPacket = new Ethernet();
                ethPacket.setEtherType(Ethernet.TYPE_LLDP);
                ethPacket.setDestinationMACAddress(ONOSLLDP.LLDP_NICIRA);
                ethPacket.setPad(true);
                ethPacket.setSourceMACAddress(buildSrcMac()).setPayload(sbpOxplldp);
                Match.Builder mBuilder = ofFactory.buildMatch();
                mBuilder.setExact(MatchField.IN_PORT, OFPort.of((int) getVportNum(edgeConnectPoint).toLong()));
                //byte[] frame = context.inPacket().parsed().serialize();
                byte[] frame = ethPacket.serialize();

                if (domainController.isCompressedMode()) {
                    domainController.sendSbpFwdReqMsg(Ip4Address.valueOf("127.0.0.1"), Ip4Address.valueOf("255.255.255.255"),
                            (int) getLogicalVportNum(edgeConnectPoint).toLong()
                            , Ip4Address.valueOf("255.255.255.255"),
                            eth.getEtherType(), (byte) 0, eth.serialize());
                    context.block();
                } else {
                    OFPacketIn ofPacketInForSuper = ofFactory.buildPacketIn()
                            .setBufferId(OFBufferId.NO_BUFFER)
                            .setReason(OFPacketInReason.NO_MATCH)
                            .setTableId(TableId.ZERO)
                            .setCookie(U64.ofRaw(context.inPacket().cookie().get()))
                            .setMatch(mBuilder.build())
                            .setData(frame)
                            .setTotalLen(frame.length)
                            .build();
                    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
                    ofPacketInForSuper.writeTo(buffer);
//                    byte[] data = new byte[buffer.readableBytes()];
//                    buffer.readBytes(data, 0, buffer.readableBytes());
                    Set<OXPSbpFlags> sbpFlagses = new HashSet<>();
                    sbpFlagses.add(OXPSbpFlags.DATA_EXIST);
                    OXPSbp oxpSbp = oxpFactory.buildSbp()
                            .setSbpCmpType(OXPSbpCmpType.NORMAL)
                            .setFlags(sbpFlagses)
                            .setSbpData(OXPSbpData.read(buffer, buffer.readableBytes(), domainController.getOxpVersion()))
                            .build();
                    domainController.write(oxpSbp);
                }

            }
        }
    }


    class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
                case PORT_REMOVED:
                    Port delPort = event.port();
                    ConnectPoint location = new ConnectPoint(delPort.element().id(), delPort.number());
                    addOrUpdateVport(location,OXPVportState.BLOCKED, OXPVportReason.DELETE);
                    break;
                case DEVICE_AVAILABILITY_CHANGED:
                    Device device = event.subject();
                    if (!deviceService.isAvailable(device.id())) {
                        for (Port port : deviceService.getPorts(device.id())) {
                            ConnectPoint pLocation = new ConnectPoint(port.element().id(), port.number());
                            if (vportMap.containsKey(pLocation)) {
                                addOrUpdateVport(pLocation,OXPVportState.BLOCKED, OXPVportReason.DELETE);
                            }

                        }
                    }

                default:
            }
        }
    }

    class TopoUpdateTask implements Runnable {
        @Override
        public void run() {
            updateTopo();
        }
    }
}
