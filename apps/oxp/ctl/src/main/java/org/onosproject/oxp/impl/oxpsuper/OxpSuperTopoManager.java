package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableList;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ethernet;
import org.onlab.packet.OXPLLDP;
import org.onosproject.net.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.onosproject.oxp.types.OXPInternalLink;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.slf4j.Logger;

import java.util.*;

import static org.onlab.packet.Ethernet.TYPE_LLDP;
import static org.onosproject.net.PortNumber.portNumber;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-9-3.
 */
@Component(immediate = true)
@Service
public class OxpSuperTopoManager implements OxpSuperTopoService {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private OxpSuperController superController;

    private OxpDomainMessageListener domainMessageListener = new InternalDomainMessageListener();

    // 记录Vport
    private Map<DeviceId, Set<PortNumber>> vportMap;
    private Map<ConnectPoint, OXPVportDesc> vportDescMap;
    private Map<ConnectPoint, Long> vportCapabilityMap;
    // 记录internalLinks
    private Map<DeviceId, Set<Link>> internalLinksMap;
    private Map<Link, OXPInternalLink> internalLinkDescMap;
    private ProviderId internalLinksProviderId = new ProviderId("oxp","internalLinks");
    // 记录interLinks
    private Set<Link> interLinkSet;
    private ProviderId interLinksProviderId = new ProviderId("'oxp", "interlinks");

    @Activate
    private void activate() {
        vportMap = new HashMap<>();
        vportDescMap = new HashMap<>();
        superController.addMessageListener(domainMessageListener);
    }

    @Deactivate
    private void deactivate() {
        superController.removeMessageListener(domainMessageListener);
        vportMap.clear();
        vportDescMap.clear();
    }

    @Override
    public List<PortNumber> getVports(DeviceId deviceId) {
        return ImmutableList.copyOf(vportMap.get(deviceId));
    }

    @Override
    public OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber) {
        return null;
    }

    @Override
    public List<Link> getInterlinks() {
        return null;
    }

    @Override
    public List<Link> getIntraLinks(DeviceId deviceId) {
        return null;
    }

    private void addOrUpdateVport(DeviceId deviceId, OXPVportDesc vportDesc) {
        Set<PortNumber> vportSet = vportMap.get(deviceId);
        if (null == vportSet) {
            vportSet = new HashSet<>();
            vportMap.put(deviceId, vportSet);
        }
        PortNumber vportNum = PortNumber.portNumber(vportDesc.getPortNo().getPortNumber());
        vportSet.add(vportNum);

        ConnectPoint connectPoint = new ConnectPoint(deviceId, vportNum);
        vportDescMap.put(connectPoint, vportDesc);
    }

    private void removeVport(DeviceId deviceId, PortNumber vportNum) {
        Set<PortNumber> vportSet = vportMap.get(deviceId);
        if (null == vportSet) {
            vportSet.remove(vportNum);
        }
        vportDescMap.remove(new ConnectPoint(deviceId, vportNum));
    }

    private void processVportStatusMsg(DeviceId deviceId, OXPVportStatus vportStatus) {
        switch (vportStatus.getReason()) {
            case ADD:
            case MODIFY:
                addOrUpdateVport(deviceId, vportStatus.getVportDesc());
                break;
            case DELETE:
                removeVport(deviceId, PortNumber.portNumber(vportStatus.getVportDesc().getPortNo().getPortNumber()));

        }
    }

    private void processTopoReplyMsg(DeviceId deviceId, OXPTopologyReply topologyReply) {
        List<OXPInternalLink> internalLinks = topologyReply.getInternalLinks();
        Set<Link> links = new HashSet<>();
        for (OXPInternalLink internalLink : internalLinks) {
            PortNumber srcPortNum = PortNumber.portNumber(internalLink.getSrcVport().getPortNumber());
            PortNumber dstPortNum = PortNumber.portNumber(internalLink.getDstVport().getPortNumber());
            ConnectPoint srcConnectPoint = new ConnectPoint(deviceId, srcPortNum);
            ConnectPoint dstConnectPoint = new ConnectPoint(deviceId, dstPortNum);
            if (srcPortNum == dstPortNum) {
                vportCapabilityMap.put(srcConnectPoint, internalLink.getCapability());
                continue;
            }
            Link link = DefaultLink.builder()
                    .src(srcConnectPoint)
                    .dst(dstConnectPoint)
                    .providerId(internalLinksProviderId)
                    .build();
            links.add(link);
            internalLinkDescMap.put(link, internalLink);

        }
        internalLinksMap.put(deviceId, links);
    }

    /**
     * 处理lldp包, 发现邻间链路
     * @param deviceId
     * @param packetIn
     */
    private void processOxpLldp(DeviceId deviceId, OFPacketIn packetIn) {
        packetIn.getData();
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        buffer.readBytes(packetIn.getData());
        Ethernet eth = null;
        try {
            Ethernet.deserializer().deserialize(buffer.array(), 0, buffer.readableBytes());
        } catch (DeserializationException e) {
            return;
        }
        if (eth == null || (eth.getEtherType() != TYPE_LLDP)) {
            return;
        }
        OXPLLDP oxplldp = OXPLLDP.parseOXPLLDP(eth);
        if (null == oxplldp) {
            return;
        }
        PortNumber srcPort = portNumber(oxplldp.getVportNum());
        PortNumber dstPort = portNumber(packetIn.getMatch().get(MatchField.IN_PORT).getPortNumber());
        DomainId srcDomainId = DomainId.of(oxplldp.getDomainId());
        DeviceId srcDeviceId = DeviceId.deviceId("oxp:" + srcDomainId);
        DeviceId dstDeviceId = deviceId;
        ConnectPoint srcConnectPoint = new ConnectPoint(srcDeviceId, srcPort);
        ConnectPoint dstConnectPoint = new ConnectPoint(dstDeviceId, dstPort);
        Link link = DefaultLink.builder()
                .src(srcConnectPoint)
                .dst(dstConnectPoint)
                .providerId(interLinksProviderId)
                .build();
        interLinkSet.add(link);
    }
    class InternalDomainMessageListener implements OxpDomainMessageListener {
        @Override
        public void handleIncomingMessage(DeviceId deviceId, OXPMessage msg) {
            if (msg.getType() == OXPType.OXPT_VPORT_STATUS) {
                OXPVportStatus vportStatus = (OXPVportStatus) msg;
                processVportStatusMsg(deviceId, vportStatus);
                return;
            }
            if (msg.getType() == OXPType.OXPT_TOPO_REPLY) {
                OXPTopologyReply topologyReply = (OXPTopologyReply) msg;
                processTopoReplyMsg(deviceId, topologyReply);
                return;
            }



        }

        @Override
        public void handleOutGoingMessage(DeviceId deviceId, List<OXPMessage> msgs) {

        }
    }
}
