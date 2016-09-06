package org.onosproject.oxp.impl.oxpsuper;

import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.*;
import org.onosproject.net.*;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.types.OXPSbpData;
import org.onosproject.oxp.types.OXPVport;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;

import java.util.*;

import static org.onlab.packet.Ethernet.TYPE_ARP;
import static org.onlab.packet.Ethernet.TYPE_IPV4;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-9-5.
 */
@Component(immediate = true)
public class OxpSuperRouting {

    private final Logger log = getLogger(getClass());


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private OxpSuperController superController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private OxpSuperTopoService topoService;

    private OxpDomainMessageListener domainMessageListener = new InternalDomainMsgListener();

    @Activate
    public void actviate() {
        superController.addMessageListener(domainMessageListener);
    }

    @Deactivate
    public void deactivate() {
        superController.removeMessageListener(domainMessageListener);
    }

    private void processArp(DeviceId deviceId, Ethernet eth, PortNumber inPort, long xid) {
        ARP arp = (ARP) eth.getPayload();
        IpAddress target = Ip4Address.valueOf(arp.getTargetProtocolAddress());
        IpAddress sender = Ip4Address.valueOf(arp.getSenderProtocolAddress());
        Set<OXPHost> hosts = topoService.getHostsByIp(target);
        if (hosts.isEmpty()) {
            flood(eth, xid);
            return;
        };
        OXPHost host = (OXPHost) hosts.toArray()[0];
        HostId hostId = HostId.hostId(MacAddress.valueOf(host.getMacAddress().getLong()));
        DeviceId hostLocation = topoService.getHostLocation(hostId);
        OXPDomain domain = superController.getOxpDomain(deviceId);
        if (null == hostLocation) return;
        switch (arp.getOpCode()) {
            case ARP.OP_REPLY:
                packetOut(hostLocation, inPort, PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()), eth, xid);
                break;
            case ARP.OP_REQUEST:
                Ethernet reply = ARP.buildArpReply(Ip4Address.valueOf(arp.getTargetProtocolAddress()), MacAddress.valueOf(host.getMacAddress().getLong()), eth);
                packetOut(hostLocation, inPort, PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()), reply, xid);
                break;
            default:
                return;
        }
    }


    private void processIpv4(DeviceId deviceId, Ethernet eth, PortNumber inPort, long xid, long cookie) {
        IPv4 iPv4 = (IPv4) eth.getPayload();
        IpAddress srcIp = Ip4Address.valueOf(iPv4.getSourceAddress());
        IpAddress target = Ip4Address.valueOf(iPv4.getDestinationAddress());
        Set<OXPHost> dstHosts = topoService.getHostsByIp(target);
        if (dstHosts.isEmpty()) {
            flood(eth, xid);
            return;
        }
        OXPHost host = (OXPHost) dstHosts.toArray()[0];
        HostId hostId = HostId.hostId(MacAddress.valueOf(host.getMacAddress().getLong()));
        DeviceId srcDeviceId = deviceId;
        DeviceId dstDeviceId = topoService.getHostLocation(hostId);
        OXPDomain srcDomain = superController.getOxpDomain(deviceId);
        if (null == dstDeviceId) {
            flood(eth, xid);
            return;
        }
        //若在同一域内
        if (srcDeviceId.equals(dstDeviceId)) {
            // 安装流表 inport: inport, outPort:local
            OFFlowMod fm = buildFlowMod(srcDomain, inPort, PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()),
                    srcIp, target, xid, cookie);
//            OFActionOutput.Builder action = srcDomain.ofFactory().actions().buildOutput()
//                    .setPort(OFPort.of(OXPVport.LOCAL.getPortNumber()));
//            List<OFAction> actions = new ArrayList<>();
//            Match.Builder mBuilder = srcDomain.ofFactory().buildMatch();
//            mBuilder.setExact(MatchField.IN_PORT,
//                    OFPort.of(OXPVport.LOCAL.getPortNumber()));
//            mBuilder.setExact(MatchField.IPV4_SRC,
//                    IPv4Address.of(srcIp.getIp4Address().toInt()));
//            mBuilder.setExact(MatchField.IPV4_DST,
//                    IPv4Address.of(target.getIp4Address().toInt()));
//            Match match = mBuilder.build();
//            OFFlowAdd fm = srcDomain.ofFactory().buildFlowAdd()
//                    .setXid(xid)
//                    .setCookie(U64.of(cookie))
//                    .setBufferId(OFBufferId.NO_BUFFER)
//                    .setActions(actions)
//                    .setMatch(match)
//                    .setFlags(Collections.singleton(OFFlowModFlags.SEND_FLOW_REM))
//                    .setPriority(10)
//                    .build();
            installFlow(deviceId, fm);
            packetOut(deviceId, inPort, PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()), eth, xid);
            return;
        }
        // TODO computePath and install flows
        Set<Path> paths = topoService.getPaths(srcDeviceId, dstDeviceId);
        if (paths.isEmpty()) return;
        Path path = (Path) paths.toArray()[0];
        // 安装
        Link formerLink = null;
        for (Link link : path.links()) {
            if (link.equals(path.src())) {
                OFFlowMod fm = buildFlowMod(superController.getOxpDomain(link.src().deviceId()), inPort, link.src().port(),
                        srcIp, target, xid, cookie);
                installFlow(link.src().deviceId(), fm);

            } else {
                OFFlowMod fmFommer = buildFlowMod(superController.getOxpDomain(link.src().deviceId()), formerLink.dst().port(), link.src().port(),
                        srcIp, target, xid, cookie);
                installFlow(link.src().deviceId(), fmFommer);
            }
            if (link.equals(path.dst())) {
                OFFlowMod fmLatter = buildFlowMod(superController.getOxpDomain(link.dst().deviceId()), link.dst().port(),
                        PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()),
                        srcIp, target, xid, cookie);
                installFlow(link.dst().deviceId(), fmLatter);
                packetOut(link.dst().deviceId(), link.dst().port(), PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()), eth, xid);
            }
            formerLink = link;
        }
    }

    private void packetOut(DeviceId deviceId,PortNumber inPort, PortNumber outPort, Ethernet eth, long xid) {
        OXPDomain domain = superController.getOxpDomain(deviceId);
        OFActionOutput act = domain.ofFactory().actions()
                .buildOutput()
                .setPort(OFPort.of((int) outPort.toLong()))
                .build();
        OFPacketOut.Builder builder = domain.ofFactory().buildPacketOut();
        OFPacketOut pktout = builder.setXid(xid)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setInPort(OFPort.of((int) inPort.toLong()))
                .setActions(Collections.singletonList(act))
                .setData(eth.serialize())
                .build();
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        pktout.writeTo(buffer);
        Set<OXPSbpFlags> sbpFlagses = new HashSet<>();
        sbpFlagses.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp oxpSbp = domain.factory().buildSbp()
                .setSbpCmpType(OXPSbpCmpType.NORMAL)
                .setFlags(sbpFlagses)
                .setSbpData(OXPSbpData.read(buffer, buffer.readableBytes(), domain.getOxpVersion()))
                .build();
        superController.sendMsg(deviceId, oxpSbp);
    }

    private void flood(Ethernet eth, long xid) {
        for (OXPDomain domain : superController.getOxpDomains()) {
            packetOut(domain.getDeviceId(), PortNumber.portNumber(OXPVport.LOCAL.getPortNumber()),
                    PortNumber.portNumber(OXPVport.FLOOD.getPortNumber()), eth, xid);
        }
    }

    private void installFlow(DeviceId deviceId, OFFlowMod flowMod) {
        OXPDomain domain = superController.getOxpDomain(deviceId);
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        flowMod.writeTo(buffer);
        Set<OXPSbpFlags> sbpFlagses = new HashSet<>();
        sbpFlagses.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp oxpSbp = domain.factory().buildSbp()
                .setSbpCmpType(OXPSbpCmpType.NORMAL)
                .setFlags(sbpFlagses)
                .setSbpData(OXPSbpData.read(buffer, buffer.readableBytes(), domain.getOxpVersion()))
                .build();
        superController.sendMsg(deviceId, oxpSbp);
    }

    private OFFlowMod buildFlowMod(OXPDomain srcDomain, PortNumber inPort, PortNumber outPort,
                              IpAddress srcIp, IpAddress dstIP,
                              long xid, long cookie) {
        // 安装流表 inport: inport, outPort:local
        OFActionOutput.Builder action = srcDomain.ofFactory().actions().buildOutput()
                .setPort(OFPort.of((int) outPort.toLong()));
        List<OFAction> actions = new ArrayList<>();
        Match.Builder mBuilder = srcDomain.ofFactory().buildMatch();
        mBuilder.setExact(MatchField.IN_PORT,
                OFPort.of((int) inPort.toLong()));
        mBuilder.setExact(MatchField.IPV4_SRC,
                IPv4Address.of(srcIp.getIp4Address().toInt()));
        mBuilder.setExact(MatchField.IPV4_DST,
                IPv4Address.of(dstIP.getIp4Address().toInt()));
        Match match = mBuilder.build();
        OFFlowAdd fm = srcDomain.ofFactory().buildFlowAdd()
                .setXid(xid)
                .setCookie(U64.of(cookie))
                .setBufferId(OFBufferId.NO_BUFFER)
                .setActions(actions)
                .setMatch(match)
                .setFlags(Collections.singleton(OFFlowModFlags.SEND_FLOW_REM))
                .setPriority(10)
                .build();
        return fm;
    }

    class InternalDomainMsgListener implements OxpDomainMessageListener {
        @Override
        public void handleIncomingMessage(DeviceId deviceId, OXPMessage msg) {
            if (msg.getType() == OXPType.OXPT_SBP) {
                OXPSbp sbp = (OXPSbp) msg;
                ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
                sbp.getSbpData().writeTo(buffer);
                OFMessage ofMsg = superController.parseOfMessage(sbp);
                //只处理packet-in消息
                if (null == ofMsg || ofMsg.getType() != OFType.PACKET_IN) {
                    return;
                }
                OFPacketIn packetIn = (OFPacketIn) ofMsg;
                long xid = packetIn.getXid();
                PortNumber inPort = PortNumber.portNumber(packetIn.getMatch().get(MatchField.IN_PORT).getPortNumber());
                Ethernet eth = superController.parseEthernet(packetIn.getData());
                if (null == eth) {
                    return;
                }
                if (eth.getEtherType() == TYPE_ARP) {
                    processArp(deviceId, eth, inPort, packetIn.getXid());
                    return;
                }
                if (eth.getEtherType() == TYPE_IPV4) {
                    processIpv4(deviceId, eth, inPort, packetIn.getXid(), packetIn.getCookie().getValue());
                    return;
                }

                return;
            }
        }

        @Override
        public void handleOutGoingMessage(DeviceId deviceId, List<OXPMessage> msgs) {

        }
    }
}
