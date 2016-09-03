package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.Ethernet;
import org.onlab.packet.ONOSLLDP;
import org.onlab.packet.OXPLLDP;
import org.onosproject.cluster.ClusterMetadataService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.oxp.*;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.onlab.packet.Ethernet.TYPE_LLDP;
import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;
import static org.slf4j.LoggerFactory.getLogger;

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

    private LinkListener linkListener = new InternalLinkListener();
    private OxpSuperMessageListener oxpMsgListener = new InternalOxpSuperMsgListener();
    private OxpSuperListener oxpSuperListener = new InternalOxpSuperListener();
    private PacketProcessor oxpLlapPacketProcessor = new InternalPacketProcessor();

    private AtomicLong vportNo = new AtomicLong(1);
    private Map<ConnectPoint, PortNumber> vportMap = new HashMap<>();

    private final static int LLDP_VPORT_LOCAL = 0xffff;

    @Activate
    public void activate() {
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        ofVersion = OFVersion.OF_13;
        ofFactory = OFFactories.getFactory(ofVersion);
        domainController.addMessageListener(oxpMsgListener);
        domainController.addOxpSuperListener(oxpSuperListener);
        linkService.addListener(linkListener);
        packetService.addProcessor(oxpLlapPacketProcessor, PacketProcessor.advisor(0));

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        linkService.removeListener(linkListener);
        domainController.removeMessageListener(oxpMsgListener);
        domainController.removeOxpSuperListener(oxpSuperListener);
        packetService.removeProcessor(oxpLlapPacketProcessor);
        vportMap.clear();
        log.info("Stoped");
    }

    private void sendTopoReplyMsg(List<OXPInternalLink> oxpInternalLinks) {

        //TODO
    }

    private void addOrUpdateVport(ConnectPoint edgeConnectPoint) {
        if (vportMap.containsKey(edgeConnectPoint)) {
            return;
        } else {
            long allocatedVportNum = vportNo.getAndIncrement();
            vportMap.put(edgeConnectPoint, portNumber(allocatedVportNum));
            // update Vport statu
            OXPVport vport = OXPVport.ofShort((short) allocatedVportNum);
            Set<OXPVportState> state = new HashSet<>();
            state.add(OXPVportState.LIVE);
            OXPVportDesc vportDesc = new OXPVportDescVer10.Builder().setPortNo(vport)
                    .setState(state)
                    .build();
            OXPVportStatus msg = oxpFactory.buildVportStatus()
                    .setReason(OXPVportReason.ADD)
                    .setVportDesc(vportDesc)
                    .build();
            domainController.write(msg);
            List<OXPInternalLink> internalLinks = new ArrayList<>();
            internalLinks.add(OXPInternalLink.of(vport, vport,
                    PortSpeed.SPEED_10MB.getSpeedBps(), OXPVersion.OXP_10));

            // check intra link
            for (ConnectPoint connectPoint : vportMap.keySet()) {
                PortNumber existVport = vportMap.get(connectPoint);
                if (existVport.toLong() == vport.getPortNumber()) continue;
                if (pathService.getPaths(edgeConnectPoint.deviceId(), connectPoint.deviceId()).size() > 0) {
                    OXPVport existOxpVport = OXPVport.ofShort((short) existVport.toLong());
                    //internalLinks = new ArrayList<>();
                    internalLinks.add(OXPInternalLink.of(vport, existOxpVport, PortSpeed.SPEED_10MB.getSpeedBps(), OXPVersion.OXP_10));
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

            if (oxplldp.getDomainId() == domainController.getDomainId().getLong()) {
                context.block();
                return;
            }
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
                // Send lldp to Super throuth SBP message
                // build packet_in from lldp
                OXPLLDP sbpOxplldp = OXPLLDP.oxpLLDP(oxplldp.getDomainId(),
                        oxplldp.getVportNum(),
                        domainController.getDomainId().getLong(),
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
                        .setTotalLen(frame.length)
                        .setReason(OFPacketInReason.NO_MATCH)
                        .setTableId(TableId.ZERO)
                        .setCookie(U64.ofRaw(context.inPacket().cookie().get()))
                        .setMatch(mBuilder.build())
                        .setData(frame)
                        .build();
                ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
                ofPacketInForSuper.writeTo(buffer);
                //byte[] data = new byte[buffer.readableBytes()];
                //buffer.readBytes(data, 0, buffer.readableBytes());
                OXPSbp oxpSbp = oxpFactory.buildSbp()
                        .setSbpCmpType(OXPSbpCmpType.NORMAL)
                        .setSbpData(OXPSbpData.read(buffer, buffer.readableBytes(), domainController.getOxpVersion()))
                        .build();
                domainController.write(oxpSbp);
                context.block();
            }
        }
    }
}
