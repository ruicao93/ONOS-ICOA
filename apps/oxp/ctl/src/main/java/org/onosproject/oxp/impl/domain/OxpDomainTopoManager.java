package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.Ethernet;
import org.onlab.packet.ONOSLLDP;
import org.onlab.packet.OXPLLDP;
import org.onosproject.cluster.ClusterMetadataService;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
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

    private AtomicLong vportNo = new AtomicLong(1);
    private Map<ConnectPoint, PortNumber> vportMap = new HashMap<>();
    private Map<PortNumber, Long> vportCapabilityMap = new HashMap<>();

    private ScheduledExecutorService executor;

    private final static int LLDP_VPORT_LOCAL = 0xffff;
    private final static long DEFAULT_VPORT_CAP = 0;

    @Activate
    public void activate() {
        int tryTimes = 10;
        int i = 0;
        while (oxpVersion == null && i < tryTimes) {
            oxpVersion = domainController.getOxpVersion();
            i++;
        }
        if (null == oxpVersion) {
            return;
        }
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        ofVersion = OFVersion.OF_13;
        ofFactory = OFFactories.getFactory(ofVersion);
        domainController.addMessageListener(oxpMsgListener);
        domainController.addOxpSuperListener(oxpSuperListener);
        linkService.addListener(linkListener);
        packetService.addProcessor(oxpLlapPacketProcessor, PacketProcessor.advisor(0));
        executor = newSingleThreadScheduledExecutor(groupedThreads("oxp/topoupdate", "oxp-topoupdate-%d", log));
        executor.scheduleAtFixedRate(new TopoUpdateTask(),
                domainController.getPeriod(), domainController.getPeriod(), SECONDS);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        if (executor != null) {
            executor.shutdownNow();
        }
        linkService.removeListener(linkListener);
        domainController.removeMessageListener(oxpMsgListener);
        domainController.removeOxpSuperListener(oxpSuperListener);
        packetService.removeProcessor(oxpLlapPacketProcessor);
        vportMap.clear();
        vportCapabilityMap.clear();
        log.info("Stoped");
    }

    private void sendTopoReplyMsg(List<OXPInternalLink> oxpInternalLinks) {

        //TODO
    }

    private void addOrUpdateVport(ConnectPoint edgeConnectPoint) {
        checkNotNull(edgeConnectPoint);
        if (!vportMap.containsKey(edgeConnectPoint)) {
            // 添加Vport
            // 1.分配Vport号,并记录<ConnectPoint, vportNo>
            long allocatedVportNum = vportNo.getAndIncrement();
            vportMap.put(edgeConnectPoint, portNumber(allocatedVportNum));
            vportCapabilityMap.put(portNumber(allocatedVportNum), DEFAULT_VPORT_CAP);
        }
        // 1.获取对应的vportNum
        PortNumber vportNum = vportMap.get(edgeConnectPoint);
        // 2.构造vportStatus消息:
        //    Reason:Add, State:Live
        OXPVport vport = OXPVport.ofShort((short) vportNum.toLong());
        Set<OXPVportState> state = new HashSet<>();
        state.add(OXPVportState.LIVE);
        OXPVportDesc vportDesc = new OXPVportDescVer10.Builder().setPortNo(vport)
                .setState(state)
                .build();
        OXPVportStatus msg = oxpFactory.buildVportStatus()
                .setReason(OXPVportReason.ADD)
                .setVportDesc(vportDesc)
                .build();
        // 3.发送vportStatus消息到Super
        domainController.write(msg);
        // 4.计算internalLinks
        List<OXPInternalLink> internalLinks = new ArrayList<>();

        // 4.1添加Link: <vport,vport>,capability为默认值
        // Mao: real port speed in bytes per second
        //FIXME - Here,assume vport is in one "Device", not a "Host", so we can use DeviceId() directly.
        //FIXME - If vport is on one "Host", it doesn't accord with OXP logic.
        //TODO - Part 4.1 is not debuged and tested.
        assert edgeConnectPoint.elementId() instanceof DeviceId;

        Port borderPort = deviceService.getPort(edgeConnectPoint.deviceId(),edgeConnectPoint.port());
        long vportMaxSpeed = borderPort.portSpeed() * 1000000;//data source: Mbps
        long vportCurSpeed = portStatisticsService.load(edgeConnectPoint).rate() * 8;//data source: Bps
        internalLinks.add(OXPInternalLink.of(vport, vport, vportMaxSpeed - vportCurSpeed, OXPVersion.OXP_10));

        // 4.2 check intra link
        //TODO - capability of intra link is holding...
        for (ConnectPoint connectPoint : vportMap.keySet()) {
            PortNumber existVport = vportMap.get(connectPoint);
            if (existVport.toLong() == vport.getPortNumber())
                continue;
            long existVportCurSpeed = portStatisticsService.load(connectPoint).rate() * 8;
            long linkSpeed = vportCurSpeed < existVportCurSpeed ? vportCurSpeed : existVportCurSpeed;
            if (!pathService.getPaths(edgeConnectPoint.deviceId(), connectPoint.deviceId()).isEmpty()) {
                OXPVport existOxpVport = OXPVport.ofShort((short) existVport.toLong());
                //internalLinks = new ArrayList<>();
                internalLinks.add(OXPInternalLink.of(vport, existOxpVport,
                        linkSpeed, OXPVersion.OXP_10));
            }
            if (!pathService.getPaths(connectPoint.deviceId(), edgeConnectPoint.deviceId()).isEmpty()) {
                OXPVport existOxpVport = OXPVport.ofShort((short) existVport.toLong());
                //internalLinks = new ArrayList<>();
                internalLinks.add(OXPInternalLink.of(existOxpVport, vport, linkSpeed, OXPVersion.OXP_10));
            }
        }
        if (internalLinks.size() == 0) {
            return;
        }

        // 5.将internalLinks发送至Super
        OXPTopologyReply topologyReply = oxpFactory
                .buildTopologyReply()
                .setInternalLink(internalLinks)
                .build();
        domainController.write(topologyReply);
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
                addOrUpdateVport(edgeConnectPoint);
                OXPLLDP replyOxplldp = OXPLLDP.oxpLLDP(Long.valueOf(dstDeviceId.toString().substring("of:".length())),
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
                    addOrUpdateVport(edgeConnectPoint);
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
                //byte[] data = new byte[buffer.readableBytes()];
                //buffer.readBytes(data, 0, buffer.readableBytes());
                Set<OXPSbpFlags> sbpFlagses = new HashSet<>();
                sbpFlagses.add(OXPSbpFlags.DATA_EXIST);
                OXPSbp oxpSbp = oxpFactory.buildSbp()
                        .setSbpCmpType(OXPSbpCmpType.NORMAL)
                        .setFlags(sbpFlagses)
                        .setSbpData(OXPSbpData.read(buffer, buffer.readableBytes(), domainController.getOxpVersion()))
                        .build();
                domainController.write(oxpSbp);
                context.block();
            }
        }
    }

    class TopoUpdateTask implements Runnable {
        @Override
        public void run() {
            for (ConnectPoint vportConnectPoint : vportMap.keySet()) {
                addOrUpdateVport(vportConnectPoint);
            }
        }
    }
}
