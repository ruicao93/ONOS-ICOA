package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.domain.OxpDomainTopoService;
import org.onosproject.oxp.domain.OxpSuperListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.ver10.OXPForwardingRequestVer10;
import org.onosproject.oxp.types.*;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-8-20.
 */
@Component(immediate = true)
public class OxpDomainRouting {

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
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OxpDomainTopoService oxpDomainTopoService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EdgePortService edgeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    private ApplicationId appId;

    private PacketProcessor packetProcessor = new ReactivePacketProcessor();
    private OxpSuperMessageListener oxpSbpMsgListener = new InternalOxpSuperMsgListener();
    private OxpSuperListener oxpSuperListener = new InternalOxpSuperListener();
    private boolean bootFlag = false;

    private static final Ip4Address broadcast = Ip4Address.valueOf("255.255.255.255");


    @Activate
    public void activate() {
        appId = coreService.getAppId("org.onosproject.oxp");
        domainController.addOxpSuperListener(oxpSuperListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        domainController.removeOxpSuperListener(oxpSuperListener);
        if (!bootFlag) {
            return;
        }
        domainController.removeMessageListener(oxpSbpMsgListener);
        packetService.removeProcessor(packetProcessor);
        log.info("Stoped");
    }

    private void setUp() {
        bootFlag = true;
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        ofVersion = OFVersion.OF_13;
        ofFactory = OFFactories.getFactory(ofVersion);
        domainController.addMessageListener(oxpSbpMsgListener);
        packetService.addProcessor(packetProcessor, PacketProcessor.director(1)); //Handle before other APPs
    }

    /**
     * 翻译Packet-out消息
     * @param pktout
     * @throws DeserializationException
     */
    private void translatePacketOutMessage(OFPacketOut pktout) throws DeserializationException {
        Ethernet ethpkt = Ethernet.deserializer().deserialize(pktout.getData(), 0, pktout.getData().length);
        OFPort outPort  = null;
        for (OFAction action :pktout.getActions()) {
            if (action.getType() == OFActionType.OUTPUT) {
                outPort = ((OFActionOutput) action).getPort();
                break;
            }
        }
        translatePacketOutMessage(PortNumber.portNumber((long) outPort.getPortNumber()), ethpkt);
//        if (outPort == null) {
//            return;
//        }
//        if (outPort.getPortNumber() == OXPVport.LOCAL.getPortNumber()) {
//            if (ethpkt.getEtherType() == Ethernet.TYPE_ARP) {
//                ARP arp = (ARP) ethpkt.getPayload();
//                packetOut(Ip4Address.valueOf(arp.getTargetProtocolAddress()), ethpkt);
//            } else if (ethpkt.getEtherType() == Ethernet.TYPE_IPV4) {
//                IPv4 ipv4 = (IPv4) ethpkt.getPayload();
//                packetOut(Ip4Address.valueOf(ipv4.getDestinationAddress()), ethpkt);
//            }
//        } else {
//            // vport ---> port
//            ConnectPoint location = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(outPort.getPortNumber()));
//            if (null == location) {
//                flood(ethpkt);
//            } else {
//                packetOut(location, ethpkt);
//            }
//        }
    }
    private void translatePacketOutMessage(OXPPacketOut cmpPacketOut) throws DeserializationException{
        byte[] data = cmpPacketOut.getData();
        translatePacketOutMessage(PortNumber.portNumber(cmpPacketOut.getOutPort()),
                Ethernet.deserializer().deserialize(data, 0, data.length));
    }

    private void translatePacketOutMessage(PortNumber outPort, Ethernet ethpkt) {
        if (outPort == null) {
            return;
        }
        if (outPort.toLong() == OXPVport.LOCAL.getPortNumber()) {
            if (ethpkt.getEtherType() == Ethernet.TYPE_ARP) {
                ARP arp = (ARP) ethpkt.getPayload();
                packetOut(Ip4Address.valueOf(arp.getTargetProtocolAddress()), ethpkt);
            } else if (ethpkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4 = (IPv4) ethpkt.getPayload();
                packetOut(Ip4Address.valueOf(ipv4.getDestinationAddress()), ethpkt);
            }
        } else {
            // vport ---> port
            ConnectPoint location = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(outPort.toString()));
            if (null == location) {
                flood(ethpkt);
            } else {
                packetOut(location, ethpkt);
            }
        }
    }

    private void translateFlowModeMessage(OXPForwardingReply cmpFwdReply) {
        translateFlowModeMessage(
                IPv4Address.of(cmpFwdReply.getSrcIpAddress().getBytes()),
                IPv4Address.of(cmpFwdReply.getDstIpAddress().getBytes()),
                cmpFwdReply.getSrcVport(),
                cmpFwdReply.getDstVport(),
                EthType.of(cmpFwdReply.getEthType()));
    }

    private void translateFlowModeMessage(OFFlowMod flowMod) {
        //Ethernet ethpkt = Ethernet.deserializer().deserialize(flowMod.getData(), 0, pktout.getData().length);
        Match match = flowMod.getMatch();
        IPv4Address srcIp = (IPv4Address) match.get(MatchField.IPV4_SRC);
        IPv4Address dstIp = (IPv4Address) match.get(MatchField.IPV4_DST);
        Host srcHost = getFirstHostByIp(IpAddress.valueOf(srcIp.getInt()));
        Host dstHost = getFirstHostByIp(IpAddress.valueOf(dstIp.getInt()));
        EthType ethType = (EthType) match.get(MatchField.ETH_TYPE);
        OFPort inPort  = match.get(MatchField.IN_PORT);
        OFPort outPort  = null;
        for (OFAction action :flowMod.getActions()) {
            if (action.getType() == OFActionType.OUTPUT) {
                outPort = ((OFActionOutput) action).getPort();
                break;
            }
        }
        translateFlowModeMessage(srcIp, dstIp, inPort.getPortNumber(), outPort.getPortNumber(), ethType);
//        ConnectPoint srcConnectPoint = null;
//        ConnectPoint dstConnectPoint = null;
//        if (null == srcHost) {
//            srcConnectPoint = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(inPort.getPortNumber()));
//        } else {
//            srcConnectPoint = srcHost.location();
//        }
//        if (null == dstHost) {
//            dstConnectPoint = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(outPort.getPortNumber()));
//        } else {
//            dstConnectPoint = dstHost.location();
//        }
//        if (srcConnectPoint == null || dstConnectPoint == null) {
//            return;
//        }
//        // check if src and dst are on same device
//        if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())) {
//            installForwardRule(srcConnectPoint.deviceId(), ethType,
//                    srcIp, dstIp,
//                    srcConnectPoint.port(), dstConnectPoint.port());
//            return;
//        }
//        // install path between connectpoints
//        Set<Path> paths = pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId());
//        if (paths == null || paths.size() == 0) return;
//        Path path = (Path) paths.toArray()[0];
//        List<Link> links = path.links();
//        Link lastLink = null;
//        for (Link link : links) {
//            if (link.src().equals(path.src())) {
//                installForwardRule(link.src().deviceId(), ethType,
//                        srcIp, dstIp,
//                        srcConnectPoint.port(), link.src().port());
//            } else {
//                installForwardRule(link.src().deviceId(), ethType,
//                        srcIp, dstIp,
//                        lastLink.dst().port(), link.src().port());
//            }
//            if (link.dst().equals(path.dst())) {
//                installForwardRule(link.src().deviceId(), ethType,
//                        srcIp, dstIp,
//                        link.dst().port(), dstConnectPoint.port());
//            }
//            lastLink = link;
//        }
        //packetOut(dstConnectPoint, ethpkt);
    }

    private void translateFlowModeMessage(IPv4Address srcIp, IPv4Address dstIp,
                                     long srcVport, long dstVport,
                                     EthType ethType) {
        OFPort inPort  = OFPort.of((int) srcVport);
        OFPort outPort  = OFPort.of((int) dstVport);
        Host srcHost = getFirstHostByIp(IpAddress.valueOf(srcIp.getInt()));
        Host dstHost = getFirstHostByIp(IpAddress.valueOf(dstIp.getInt()));
        ConnectPoint srcConnectPoint = null;
        ConnectPoint dstConnectPoint = null;
        if (null == srcHost) {
            srcConnectPoint = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(inPort.getPortNumber()));
        } else {
            srcConnectPoint = srcHost.location();
        }
        if (null == dstHost) {
            dstConnectPoint = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(outPort.getPortNumber()));
        } else {
            dstConnectPoint = dstHost.location();
        }
        if (srcConnectPoint == null || dstConnectPoint == null) {
            return;
        }
        // check if src and dst are on same device
        if (srcConnectPoint.deviceId().equals(dstConnectPoint.deviceId())) {
            installForwardRule(srcConnectPoint.deviceId(), ethType,
                    srcIp, dstIp,
                    srcConnectPoint.port(), dstConnectPoint.port());
            return;
        }
        // install path between connectpoints
        Set<Path> paths = pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId());
        if (paths == null || paths.size() == 0) return;
        Path path = (Path) paths.toArray()[0];
        List<Link> links = path.links();
        Link lastLink = null;
        for (Link link : links) {
            if (link.src().equals(path.src())) {
                installForwardRule(link.src().deviceId(), ethType,
                        srcIp, dstIp,
                        srcConnectPoint.port(), link.src().port());
            } else {
                installForwardRule(link.src().deviceId(), ethType,
                        srcIp, dstIp,
                        lastLink.dst().port(), link.src().port());
            }
            if (link.dst().equals(path.dst())) {
                installForwardRule(link.dst().deviceId(), ethType,
                        srcIp, dstIp,
                        link.dst().port(), dstConnectPoint.port());
            }
            lastLink = link;
        }
    }

    private void installForwardRule(DeviceId deviceId, EthType ethType,
                               IPv4Address srcIp, IPv4Address dstIp,
                                    PortNumber inPort,PortNumber outPort) {
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        selectorBuilder.matchInPort(inPort)
                .matchEthType((short) ethType.getValue())
                .matchIPSrc(Ip4Prefix.valueOf(srcIp.getInt(),
                        Ip4Prefix.MAX_MASK_LENGTH))
                .matchIPDst(Ip4Prefix.valueOf(dstIp.getInt(),
                        Ip4Prefix.MAX_MASK_LENGTH));
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(outPort)
                .build();
        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(10)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makeTemporary(15)
                .add();

        flowObjectiveService.forward(deviceId, forwardingObjective);
    }
    private Host getFirstHostByIp(IpAddress ip) {
        Set<Host> dstHosts = hostService.getHostsByIp(ip);
        if (null != dstHosts && dstHosts.size() > 0) {
            return (Host) dstHosts.toArray()[0];
        }
        return null;
    }
    private void packetOut(ConnectPoint connectPoint, Ethernet ethpkt) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        builder.setOutput(connectPoint.port());
        packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),
                builder.build(), ByteBuffer.wrap(ethpkt.serialize())));
        return;
    }

    private void packetOut(Ip4Address ip4Address, Ethernet ethpkt) {
        Set<Host> dstHosts = hostService.getHostsByIp(ip4Address);
        if (null != dstHosts && dstHosts.size() > 0) {
            Host dstHost = (Host) dstHosts.toArray()[0];
            packetOut(dstHost.location(), ethpkt);
            return;
        } else {
            // Flood
            flood(ethpkt);
        }
    }

    private void flood(Ethernet ethpkt) {
        TrafficTreatment.Builder builder = null;
        for ( ConnectPoint connectPoint : edgeService.getEdgePoints()) {
            if (null == deviceService.getDevice(connectPoint.deviceId())) continue;
            if (!oxpDomainTopoService.isOuterPort(connectPoint)) {
                ByteBuffer buff = ByteBuffer.wrap(ethpkt.serialize());
                builder = DefaultTrafficTreatment.builder();
                builder.setOutput(connectPoint.port());
                packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),
                        builder.build(), buff));
            }
        }
    }

    /**
     * 处理来自device的Packet-in消息,处理一下三种数据包:
     *  1.ARP-Request
     *  2.ARP-Repy
     *  3.Ip
     */
    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            if (context.isHandled()) {
                return;
            }

            // Only handle packet when has connected to OXP Super controller
            if (!domainController.isConnectToSuper()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();

            if (ethPkt == null || ethPkt.getEtherType() == Ethernet.TYPE_LLDP) {
                return;
            }
            PortNumber dstPort = context.inPacket().receivedFrom().port();
            DeviceId dstDeviceId = context.inPacket().receivedFrom().deviceId();
            ConnectPoint connectPoint = new ConnectPoint(dstDeviceId, dstPort);

            IpAddress target;
            IpAddress srcAddress;
            HostId id = HostId.hostId(ethPkt.getDestinationMAC());
            // 只处理Arp和IPV4
            if (ethPkt.getEtherType() == Ethernet.TYPE_ARP) {
                target = Ip4Address.valueOf(((ARP) ethPkt.getPayload()).getTargetProtocolAddress());
                srcAddress = Ip4Address.valueOf(((ARP) ethPkt.getPayload()).getSenderProtocolAddress());
            } else if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                target = Ip4Address.valueOf(((IPv4) ethPkt.getPayload()).getDestinationAddress());
                srcAddress = Ip4Address.valueOf(((IPv4) ethPkt.getPayload()).getSourceAddress());
                if (target.equals(broadcast)) {
                    context.block();
                    return;
                }
            } else {
                    return;
            }
            Set<Host> hosts =  hostService.getHostsByIp(target);
            if (null != hosts && hosts.size() > 0) {
                return;
            }

            Match.Builder mBuilder = ofFactory.buildMatch();
            mBuilder.setExact(MatchField.IN_PORT, OFPort.of((int) oxpDomainTopoService.getLogicalVportNum(connectPoint).toLong()));
            //byte[] frame = context.inPacket().parsed().serialize();
            byte[] frame = ethPkt.serialize();
            OFPacketIn ofPacketInForSuper = ofFactory.buildPacketIn()
                    .setBufferId(OFBufferId.NO_BUFFER)
                    .setReason(OFPacketInReason.NO_MATCH)
                    .setTableId(TableId.ZERO)
                    .setCookie(U64.ofRaw(context.inPacket().cookie().get()))
                    .setMatch(mBuilder.build())
                    .setData(frame)
                    //.setTotalLen(frame.length)
                    .build();
            ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            ofPacketInForSuper.writeTo(buffer);
            //byte[] data = buffer.array();
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data, 0, buffer.readableBytes());
            if (domainController.isCompressedMode()) {
                domainController.sendSbpFwdReqMsg(srcAddress, target,
                        (int) oxpDomainTopoService.getLogicalVportNum(connectPoint).toLong()
                        , Ip4Address.valueOf("255.255.255.255"),
                        ethPkt.getEtherType(), (byte) 0, ethPkt.serialize());
            } else {
                Set<OXPSbpFlags> oxpSbpflgs = new HashSet<>();
                oxpSbpflgs.add(OXPSbpFlags.DATA_EXIST);
                OXPSbp oxpSbp = oxpFactory.buildSbp()
                        .setSbpCmpType(OXPSbpCmpType.NORMAL)
                        .setFlags(oxpSbpflgs)
                        .setSbpData(OXPSbpData.of(data, domainController.getOxpVersion()))
                        .build();
                domainController.write(oxpSbp);
            }


            //flood(ethPkt);
            context.block();
        }
    }



    /**
     * 翻译SBP消息
     */
    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
            // translate sbp response
            if (msg.getType() != OXPType.OXPT_SBP) {
                return;
            }
            OXPSbp oxpSbp = (OXPSbp) msg;
            try {
                switch (oxpSbp.getSbpCmpType()) {
                    case NORMAL:
                        ChannelBuffer buff = ChannelBuffers.dynamicBuffer();
                        oxpSbp.getSbpData().writeTo(buff);
                        OFMessage ofMessage = null;
                        ofMessage = ofFactory.getReader().readFrom(buff);
                        if (null == ofMessage) {
                            return;
                        }
                        // 1. if packet_out
                        if (ofMessage.getType() == OFType.PACKET_OUT) {
                            translatePacketOutMessage((OFPacketOut) ofMessage);
                        } else if (ofMessage.getType() == OFType.FLOW_MOD) {
                            // 2. if install_flow
                            translateFlowModeMessage((OFFlowMod) ofMessage);
                        }
                        break;
                    case FORWARDING_REPLY:
                        translateFlowModeMessage((OXPForwardingReply) oxpSbp.getSbpCmpData());
                        break;
                    case PACKET_OUT:
                        translatePacketOutMessage((OXPPacketOut) oxpSbp.getSbpCmpData());
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }

        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }

    class InternalOxpSuperListener implements OxpSuperListener {
        @Override
        public void connectToSuper(OxpSuper oxpSuper) {
            setUp();
        }

        @Override
        public void disconnectFromSuper(OxpSuper oxpSuper) {

        }
    }
}
