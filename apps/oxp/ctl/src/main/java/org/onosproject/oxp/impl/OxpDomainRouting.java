package org.onosproject.oxp.impl;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.*;
import org.onosproject.net.*;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.oxp.OxpDomainController;
import org.onosproject.oxp.OxpDomainTopoService;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPSbpData;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.types.EthType;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
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
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    private PacketProcessor packetProcessor = new ReactivePacketProcessor();
    private OxpSuperMessageListener oxpSbpMsgListener = new InternalOxpSuperMsgListener();

    @Activate
    public void activate() {
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        ofVersion = OFVersion.OF_13;
        ofFactory = OFFactories.getFactory(ofVersion);
        packetService.addProcessor(packetProcessor, PacketProcessor.director(4));
        domainController.addMessageListener(oxpSbpMsgListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        domainController.removeMessageListener(oxpSbpMsgListener);
        log.info("Stoped");
    }

    private void translatePacketOutMessage(OFPacketOut pktout) throws DeserializationException {
        //TODO
        Ethernet ethpkt = Ethernet.deserializer().deserialize(pktout.getData(), 0, pktout.getData().length);
        OFPort outPort  = null;
        for (OFAction action :pktout.getActions()) {
            if (action.getType() == OFActionType.OUTPUT) {
                outPort = ((OFActionOutput) action).getPort();
                break;
            }
        }
        if (outPort == null) {
            return;
        }
        if (outPort.equals(OFPort.LOCAL)) {
            if (ethpkt.getEtherType() == Ethernet.TYPE_ARP) {
                ARP arp = (ARP) ethpkt.getPayload();
                packetOut(Ip4Address.valueOf(arp.getTargetProtocolAddress()), ethpkt);
            } else if (ethpkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4 = (IPv4) ethpkt.getPayload();
                packetOut(Ip4Address.valueOf(ipv4.getDestinationAddress()), ethpkt);
            }
        } else {
            // vport ---> port
            ConnectPoint location = oxpDomainTopoService.getLocationByVport(PortNumber.portNumber(outPort.getPortNumber()));
            if (null == location) {
                flood(ethpkt);
            } else {
                packetOut(location, ethpkt);
            }
        }
    }

    private void translateFlowModeMessage(OFFlowMod flowMod) {
        //TODO
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
        // install path between connectpoints
        Set<Path> paths = pathService.getPaths(srcConnectPoint.deviceId(), dstConnectPoint.deviceId());
        if (paths == null || paths.size() == 0) return;
        Path path = (Path) paths.toArray()[0];
        // TODO install path

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
        ByteBuffer buff = ByteBuffer.wrap(ethpkt.serialize());
        for ( ConnectPoint connectPoint : edgeService.getEdgePoints()) {
            if (!oxpDomainTopoService.isOuterPort(connectPoint)) {
                builder = DefaultTrafficTreatment.builder();
                builder.setOutput(connectPoint.port());
                packetService.emit(new DefaultOutboundPacket(connectPoint.deviceId(),
                        builder.build(), buff));
            }
        }
    }

    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();

            if (ethPkt == null || ethPkt.getEtherType() == Ethernet.TYPE_LLDP) {
                return;
            }
            PortNumber srcPort = null;
            PortNumber dstPort = context.inPacket().receivedFrom().port();
            DeviceId srcDeviceId = null;
            DeviceId dstDeviceId = context.inPacket().receivedFrom().deviceId();
            ConnectPoint connectPoint = new ConnectPoint(srcDeviceId, srcPort);

            IpAddress target;
            HostId id = HostId.hostId(ethPkt.getDestinationMAC());
            if (ethPkt.getEtherType() == Ethernet.TYPE_ARP) {
                target = Ip4Address.valueOf(((ARP) ethPkt.getPayload()).getTargetProtocolAddress());
            } else if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                target = Ip4Address.valueOf(((IPv4) ethPkt.getPayload()).getDestinationAddress());
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
                    .setTotalLen(frame.length)
                    .setReason(OFPacketInReason.NO_MATCH)
                    .setTableId(TableId.ZERO)
                    .setCookie(U64.ofRaw(context.inPacket().cookie().get()))
                    .setMatch(mBuilder.build())
                    .setData(frame)
                    .build();
            ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            ofPacketInForSuper.writeTo(buffer);
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data, 0, buffer.readableBytes());
            OXPSbp oxpSbp = oxpFactory.buildSbp()
                    .setSbpData(OXPSbpData.of(data, domainController.getOxpVersion()))
                    .build();
            domainController.write(oxpSbp);
            context.block();
        }
    }

    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
            // translate sbp response
            if (msg.getType() != OXPType.OXPT_SBP) {
                return;
            }
            OXPSbp oxpSbp = (OXPSbp) msg;
            ChannelBuffer buff = ChannelBuffers.dynamicBuffer();
            oxpSbp.getSbpData().writeTo(buff);
            OFMessage ofMessage = null;
            try {
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

            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }


}
