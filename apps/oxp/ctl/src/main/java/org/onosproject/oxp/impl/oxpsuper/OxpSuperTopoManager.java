package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.*;
import org.onosproject.common.DefaultTopology;
import org.onosproject.net.*;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.*;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.security.AppGuard;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.packet.Ethernet.TYPE_LLDP;
import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.security.AppPermission.Type.TOPOLOGY_READ;
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
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PathService pathService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    // 监听Domain SBP消息，完成vport,topo收集和邻间链路发现
    private OxpDomainMessageListener domainMessageListener = new InternalDomainMessageListener();

    // 记录Vport
    private Map<DeviceId, Set<PortNumber>> vportMap;
    private Map<ConnectPoint, OXPVportDesc> vportDescMap;
    private Map<ConnectPoint, Long> vportCapabilityMap;
    // 记录internalLinks
    private Map<DeviceId, Set<Link>> internalLinksMap;
    private Map<Link, OXPInternalLink> internalLinkDescMap;
    private ProviderId internalLinksProviderId = ProviderId.NONE;//new ProviderId("oxp","internalLinks");
    // 记录interLinks
    private Set<Link> interLinkSet;
    private ProviderId interLinksProviderId = ProviderId.NONE;//new ProviderId("'oxp", "interlinks");
    // 记录HostLocation
    private Map<DeviceId, Map<HostId, OXPHost>> hostMap;

    private volatile DefaultTopology currentTopo =
            new DefaultTopology(ProviderId.NONE,
                    new DefaultGraphDescription(0L, System.currentTimeMillis(),
                            Collections.<Device>emptyList(),
                            Collections.<Link>emptyList()));

    @Activate
    private void activate() {
        vportMap = new HashMap<>();
        vportDescMap = new HashMap<>();
        vportCapabilityMap = new HashMap<>();
        internalLinksMap = new HashMap<>();
        internalLinkDescMap = new HashMap<>();
        interLinkSet = new HashSet<>();
        hostMap = new HashMap<>();
        superController.addMessageListener(domainMessageListener);
    }

    @Deactivate
    private void deactivate() {
        superController.removeMessageListener(domainMessageListener);
        vportMap.clear();
        vportDescMap.clear();
        vportCapabilityMap.clear();
        internalLinksMap.clear();
        internalLinkDescMap.clear();
        interLinkSet.clear();
        hostMap.clear();
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
    public long getVportCapability(PortNumber portNumber) {
        if (!vportCapabilityMap.containsKey(portNumber)) return  0;
        return vportCapabilityMap.get(portNumber);
    }

    @Override
    public List<Link> getInterlinks() {
        return ImmutableList.copyOf(interLinkSet);
    }

    @Override
    public OXPInternalLink getInterLinkDesc(Link link) {
        return internalLinkDescMap.get(link);
    }

    @Override
    public List<Link> getIntraLinks(DeviceId deviceId) {
        return ImmutableList.copyOf(internalLinksMap.get(deviceId));
    }


    @Override
    public long getInterLinkCount() {
        return getInterlinks().size();
    }

    @Override
    public long getHostCount() {
        long count = 0L;
        for (OXPDomain domain : superController.getOxpDomains()) {
            count += hostMap.get(domain.getDeviceId()) == null ? 0 : hostMap.get(domain.getDeviceId()).size();
        }
        return count;
    }

    @Override
    public Set<OXPHost> getHostsByIp(IpAddress ipAddress) {
        IPv4Address iPv4Address = IPv4Address.of(ipAddress.toOctets());
        Set<OXPHost> result = new HashSet<>();
        for (Map<HostId, OXPHost> hosts : hostMap.values()) {
            for (OXPHost host : hosts.values()) {
                if (host.getIpAddress().equals(iPv4Address)) {
                    result.add(host);
                }
            }
        }
        return result;
    }

    @Override
    public Set<OXPHost> getHostsByDevice(DeviceId deviceId) {
        Map<HostId, OXPHost> hostsMap = hostMap.get(deviceId);
        if (null != hostsMap) {
            return ImmutableSet.copyOf(hostsMap.values());
        }
        return Collections.emptySet();
    }

    @Override
    public DeviceId getHostLocation(HostId hostId) {
        for (DeviceId deviceId : hostMap.keySet()) {
            Map<HostId, OXPHost> hosts = hostMap.get(deviceId);
            if (hosts.get(hostId) != null) {
                return deviceId;
            }
        }
        return null;

    }

    @Override
    public Set<Path> getPaths(DeviceId src, DeviceId dst) {
        return getPaths(src, dst, null);
    }

    @Override
    public Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight) {
        checkNotNull(src);
        checkNotNull(dst);
        Topology topology = currentTopo;
        Set<Path> paths = weight == null ?
                topologyService.getPaths(topology, src, dst) :
                topologyService.getPaths(topology, src, dst, weight);
        return paths;
    }


    //Todo - move above
    private BandwidthLinkWeight bandwidthLinkWeightTool = new BandwidthLinkWeight();

    @Override
    public Set<Path> getLoadBalancePaths(DeviceId src, DeviceId dst) {
        return topologyService.getPaths(currentTopo, src, dst, bandwidthLinkWeightTool);
    }
    private class BandwidthLinkWeight implements LinkWeight {

        private static final double LINK_LINE_SPEED = 10000000000.0; // 10Gbps
        private static final double LINK_WEIGHT_DOWN = -1.0;
        private static final double LINK_WEIGHT_FULL = 0.0;

        //FIXME - Bata1: Here, assume the edge is the inter-demain link
        @Override
        public double weight(TopologyEdge edge){

            if(edge.link().state() == Link.State.INACTIVE) {
                return LINK_WEIGHT_DOWN;
            }


            //FIXME - Bata1: Here, assume the value in the map is the rest bandwidth of inter-demain link
            long interLinkRestBandwidth =  vportCapabilityMap.get(edge.link());

            if (interLinkRestBandwidth <= 0) {
                return LINK_WEIGHT_FULL;
            }
            double restBandwidthPersent = interLinkRestBandwidth / LINK_LINE_SPEED * 100;
            return restBandwidthPersent;
        }
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
            if (srcPortNum.equals(dstPortNum)) {

                vportCapabilityMap.put(srcConnectPoint, internalLink.getCapability());
                continue;
            }
            Link link = DefaultLink.builder()
                    .src(srcConnectPoint)
                    .dst(dstConnectPoint)
                    .type(Link.Type.DIRECT)
                    .state(Link.State.ACTIVE)
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
     * @param eth
     * @param inport
     */
    private void processOxpLldp(DeviceId deviceId, Ethernet eth, PortNumber inport) {
        OXPLLDP oxplldp = OXPLLDP.parseOXPLLDP(eth);
        if (null == oxplldp) {
            return;
        }
        PortNumber srcPort = portNumber(oxplldp.getVportNum());
        PortNumber dstPort = inport;
        DomainId srcDomainId = DomainId.of(oxplldp.getDomainId());
        DeviceId srcDeviceId = DeviceId.deviceId("oxp:" + srcDomainId);
        DeviceId dstDeviceId = deviceId;
        ConnectPoint srcConnectPoint = new ConnectPoint(srcDeviceId, srcPort);
        ConnectPoint dstConnectPoint = new ConnectPoint(dstDeviceId, dstPort);
        Link link = DefaultLink.builder()
                .src(srcConnectPoint)
                .dst(dstConnectPoint)
                .type(Link.Type.DIRECT)
                .providerId(interLinksProviderId)
                .build();
        if (interLinkSet.contains(link)) {
            return;
        }
        interLinkSet.add(link);
        updateTopology();
    }

    private void processHostUpdate(DeviceId deviceId, List<OXPHost> hosts) {
        Map<HostId, OXPHost> map = hostMap.get(deviceId);
        if (null == map) {
            map = new HashMap<>();
            hostMap.put(deviceId, map);
        }
        for (OXPHost host : hosts) {
            HostId hostId = HostId.hostId(MacAddress.valueOf(host.getMacAddress().getLong()));
            if (host.getState().equals(OXPHostState.ACTIVE)) {
                map.put(hostId, host);
            } else {
                map.remove(hostId);
            }

        }
    }

    private void updateTopology() {
        Set<OXPDomain> domainSet = superController.getOxpDomains();
        Set<DeviceId> devices = new HashSet<>();
        for (OXPDomain domain : domainSet) {
            devices.add(domain.getDeviceId());
        }
        GraphDescription graphDescription = new DefaultGraphDescription(System.nanoTime(),
                System.currentTimeMillis(),
                superController.getDevices(),
                getInterlinks()
                );
        DefaultTopology newTopology = new DefaultTopology(ProviderId.NONE, graphDescription);
        currentTopo = newTopology;
    }


    /**
     * 监听Domain消息，完成vport,topo收集和邻间链路发现
     */
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
            if (msg.getType() == OXPType.OXPT_HOST_UPDATE) {
                OXPHostUpdate hostUpdate = (OXPHostUpdate) msg;
                processHostUpdate(deviceId, hostUpdate.getHosts());
                return;
            }
            if (msg.getType() == OXPType.OXPT_HOST_REPLY) {
                OXPHostReply hostReply = (OXPHostReply) msg;
                processHostUpdate(deviceId, hostReply.getHosts());
                return;
            }
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
                PortNumber inPort = PortNumber.portNumber(packetIn.getMatch().get(MatchField.IN_PORT).getPortNumber());
                Ethernet eth = superController.parseEthernet(packetIn.getData());
                if (null == eth) {
                    return;
                }
                if (eth.getEtherType() == TYPE_LLDP) {
                    processOxpLldp(deviceId, eth, inPort);
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
