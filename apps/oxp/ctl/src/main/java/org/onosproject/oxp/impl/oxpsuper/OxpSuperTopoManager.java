package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.OXPLLDP;
import org.onosproject.common.DefaultTopology;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.*;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.oxpsuper.OxpDomainListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.types.OXPInternalLink;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.slf4j.Logger;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
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
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PathService pathService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    // 监听Domain SBP消息，完成vport,topo收集和邻间链路发现
    private OxpDomainMessageListener domainMessageListener = new InternalDomainMessageListener();
    private OxpDomainListener domainListener = new InternalDomainListener();

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
        superController.addOxpDomainListener(domainListener);
    }

    @Deactivate
    private void deactivate() {
        superController.removeMessageListener(domainMessageListener);
        superController.removeOxpDomainListener(domainListener);
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
        if (!vportMap.containsKey(deviceId)) return Collections.emptyList();
        return ImmutableList.copyOf(vportMap.get(deviceId));
    }

    @Override
    public OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber) {
        return null;
    }

    @Override
    public long getVportCapability(ConnectPoint portLocation) {
        if (!vportCapabilityMap.containsKey(portLocation)) return  0;
        return vportCapabilityMap.get(portLocation);
    }

    @Override
    public List<Link> getInterlinks() {
        return ImmutableList.copyOf(interLinkSet);
    }

    @Override
    public long getInterLinkCapability(Link link) {
        checkNotNull(link);
        long srcVportCapability = getVportCapability(link.src());
        long dstVportCapability = getVportCapability(link.dst());
        return srcVportCapability < dstVportCapability ? srcVportCapability : dstVportCapability;
    }

    @Override
    public OXPInternalLink getIntraLinkDesc(Link link) {
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


    //=================== Start =====================
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PortStatisticsService portStatisticsService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private HostService hostService;

    ProviderId routeProviderId = new ProviderId("BUPT-FNLab", "OXP");
    private LinkWeight linkWeightTool = null;


    public Set<Path> getLoadBalancePaths(ElementId src, ElementId dst) {
        Topology currentTopo = topologyService.currentTopology();
        return getLoadBalancePaths(currentTopo, src, dst);
    }


    public Set<Path> getLoadBalancePaths(ElementId src, ElementId dst, LinkWeight linkWeight) {
        Topology currentTopo = topologyService.currentTopology();
        return getLoadBalancePaths(currentTopo, src, dst, linkWeight);
    }

    public Set<Path> getLoadBalancePaths(Topology topo, ElementId src, ElementId dst) {
        return getLoadBalancePaths(currentTopo, src, dst, null);
    }

    /**
     * Core Entry of routing function.
     * just one best Path is returned now.
     *
     * @param topo
     * @param src
     * @param dst
     * @param linkWeight
     * @return empty Set if
     * 1. no path found
     * 2. given srcHost or dstHost is not discovered by ONOS
     * 3. given srcDevice and dstDevice are identical one.
     */
    public Set<Path> getLoadBalancePaths(Topology topo, ElementId src, ElementId dst, LinkWeight linkWeight) {

        linkWeightTool = linkWeight == null ? new BandwidthLinkWeight() : linkWeight;

        if (src instanceof DeviceId && dst instanceof DeviceId) {

            // no need to create edge link.
            // --- Three Step by Mao. ---

            Set<List<TopologyEdge>> allRoutes = findAllRoutes(topo, (DeviceId) src, (DeviceId) dst);

            Set<Path> allPaths = calculateRoutesCost(allRoutes);

            Path linkPath = selectRoute(allPaths);


            //use Set to be compatible with ONOS API
            return linkPath != null ? ImmutableSet.of(linkPath) : ImmutableSet.of();

        } else if (src instanceof HostId && dst instanceof HostId) {


            Host srcHost = hostService.getHost((HostId) src);
            Host dstHost = hostService.getHost((HostId) dst);
            if (srcHost == null || dstHost == null) {
                log.warn("Generate whole path but found null, hostSrc:{}, hostDst:{}", srcHost, dstHost);
                return ImmutableSet.of();
            }
            EdgeLink srcLink = getEdgeLink(srcHost, true);
            EdgeLink dstLink = getEdgeLink(dstHost, false);


            // --- Four Step by Mao. ---

            Set<List<TopologyEdge>> allRoutes = findAllRoutes(topo, srcLink.dst().deviceId(), dstLink.src().deviceId());

            Set<Path> allPaths = calculateRoutesCost(allRoutes);

            Path linkPath = selectRoute(allPaths);

            Path wholePath = buildWholePath(srcLink, dstLink, linkPath);

            //use Set to be compatible with ONOS API
            return wholePath != null ? ImmutableSet.of(wholePath) : ImmutableSet.of();

        } else {
            //use Set to be compatible with ONOS API
            return ImmutableSet.of();
        }
    }

    /**
     * Generate EdgeLink which is between Host and Device.
     * Tool for getLoadBalancePaths().
     *
     * @param host
     * @param isIngress whether it is Ingress to Device or not.
     * @return
     */
    private EdgeLink getEdgeLink(Host host, boolean isIngress) {
        return new DefaultEdgeLink(routeProviderId, new ConnectPoint(host.id(), PortNumber.portNumber(0)),
                host.location(), isIngress);
    }

    //=================== Step One: Find routes =====================

    /**
     * Entry for find all Paths between Src and Dst.
     * By Mao.
     *
     * @param src  Src of Path.
     * @param dst  Dst of Path.
     * @param topo Topology, MUST be an Object of DefaultTopology now.
     */
    private Set<List<TopologyEdge>> findAllRoutes(Topology topo, DeviceId src, DeviceId dst) {
        if (!(topo instanceof DefaultTopology)) {
            log.error("topology is not the object of DefaultTopology.");
            return ImmutableSet.of();
        }

        Set<List<TopologyEdge>> graghResult = new HashSet<>();
        dfsFindAllRoutes(new DefaultTopologyVertex(src), new DefaultTopologyVertex(dst),
                new ArrayList<>(), new ArrayList<>(),
                ((DefaultTopology) topo).getGraph(), graghResult);

        return graghResult;
    }

    /**
     * Get all possible path between Src and Dst using DFS, by Mao.
     * DFS Core, Recursion Part.
     *
     * @param src          Source point per Recursion
     * @param dst          Final Objective
     * @param passedLink   dynamic, record passed links in real time
     * @param passedDevice dynamic, record entered devices in real time, to avoid loop
     * @param topoGraph    represent the whole world
     * @param result       Set of all Paths.
     * @return no use.
     */
    private void dfsFindAllRoutes(TopologyVertex src,
                                  TopologyVertex dst,
                                  List<TopologyEdge> passedLink,
                                  List<TopologyVertex> passedDevice,
                                  TopologyGraph topoGraph,
                                  Set<List<TopologyEdge>> result) {
        if (src.equals(dst))
            return;

        passedDevice.add(src);

        Set<TopologyEdge> egressSrc = topoGraph.getEdgesFrom(src);
        egressSrc.forEach(egress -> {
            TopologyVertex vertexDst = egress.dst();
            if (vertexDst.equals(dst)) {
                //Gain a Path
                passedLink.add(egress);
                result.add(ImmutableList.copyOf(passedLink.iterator()));
                passedLink.remove(egress);

            } else if (!passedDevice.contains(vertexDst)) {
                //DFS into
                passedLink.add(egress);
                dfsFindAllRoutes(vertexDst, dst, passedLink, passedDevice, topoGraph, result);
                passedLink.remove(egress);

            } else {
                //means - passedDevice.contains(vertexDst)
                //We hit a loop, NOT go into
            }
        });

        passedDevice.remove(src);
    }

    /**
     * Parse several TopologyEdge(s) to one Path.
     * Tool for findAllPaths.
     */
    private List<Link> parseEdgeToLink(List<TopologyEdge> edges) {
        List<Link> links = new ArrayList<>();
        edges.forEach(edge -> links.add(edge.link()));
        return links;
    }

    //=================== Step Two: Calculate Cost =====================

    private Set<Path> calculateRoutesCost(Set<List<TopologyEdge>> routes) {

        Set<Path> paths = new HashSet<>();

        routes.forEach(route -> {
            double cost = maxLinkWeight(route);
            paths.add(parseEdgeToPath(route, cost));
        });

        return paths;
    }

    /**
     * A strategy to calculate the weight of one path.
     */
    private double maxLinkWeight(List<TopologyEdge> edges) {

        double weight = 0;
        for (TopologyEdge edge : edges) {
            double linkWeight = linkWeightTool.weight(edge);
            weight = weight < linkWeight ? linkWeight : weight;
        }
        return weight;
    }

    /**
     * Parse several TopologyEdge(s) to one Path.
     * Tool for calculateRoutesWeight().
     */
    private Path parseEdgeToPath(List<TopologyEdge> edges, double cost) {

        ArrayList links = new ArrayList();
        edges.forEach(edge -> links.add(edge.link()));

        return new DefaultPath(routeProviderId, links, cost);
    }

    //=================== Step Three: Select one route(Path) =====================

    private Path selectRoute(Set<Path> paths) {
        if (paths.size() < 1)
            return null;

        return getMinCostMinHopPath(new ArrayList(paths));
    }

    /**
     * A strategy to select one best Path.
     *
     * @param paths
     * @return whose max cost of all links is lowest.
     */
    private Path getMinCostPath(List<Path> paths) {
        Path result = paths.get(0);
        for (int i = 1, pathCount = paths.size(); i < pathCount; i++) {
            Path temp = paths.get(i);
            result = result.cost() > temp.cost() ? temp : result;
        }
        return result;
    }

    /**
     * A strategy to select one best Path.
     *
     * @param paths
     * @return whose count of all links is lowest.
     */
    private Path getMinHopPath(List<Path> paths) {
        Path result = paths.get(0);
        for (int i = 1, pathCount = paths.size(); i < pathCount; i++) {
            Path temp = paths.get(i);
            result = result.links().size() > temp.links().size() ? temp : result;
        }
        return result;
    }

    /**
     * An integrated strategy to select one best Path.
     *
     * @param paths
     * @return whose count of all links is lowest.
     */
    private Path getMinCostMinHopPath(List<Path> paths) {

        final double MEASURE_TOLERANCE = 0.05; // 0.05% represent 5M(10G), 12.5M(25G), 50M(100G)

        //Sort by Cost in order
        paths.sort((p1, p2) -> p1.cost() > p2.cost() ? 1 : (p1.cost() < p2.cost() ? -1 : 0));

        // get paths with similar lowest cost within MEASURE_TOLERANCE range.
        List<Path> minCostPaths = new ArrayList<>();
        Path result = paths.get(0);
        minCostPaths.add(result);
        for (int i = 1, pathCount = paths.size(); i < pathCount; i++) {
            Path temp = paths.get(i);
            if (temp.cost() - result.cost() < MEASURE_TOLERANCE) {
                minCostPaths.add(temp);
            }
        }

        result = getMinHopPath(minCostPaths);

        return result;
    }
    //=================== Step Four: Build whole Path, with edge links =====================

    /**
     * @param srcLink
     * @param dstLink
     * @param linkPath
     * @return At least, Path will include two edge links.
     */
    private Path buildWholePath(EdgeLink srcLink, EdgeLink dstLink, Path linkPath) {
        if (linkPath == null && !(srcLink.dst().deviceId().equals(dstLink.src().deviceId()))) {
            log.warn("no available Path is found!");
            return null;
        }

        return buildEdgeToEdgePath(srcLink, dstLink, linkPath);
    }

    /**
     * Produces a direct edge-to-edge path.
     *
     * @param srcLink
     * @param dstLink
     * @param linkPath
     * @return
     */
    private Path buildEdgeToEdgePath(EdgeLink srcLink, EdgeLink dstLink, Path linkPath) {

        List<Link> links = Lists.newArrayListWithCapacity(2);

        double cost = 0;

        //The cost of edge link is 0.
        links.add(srcLink);

        if (linkPath != null) {
            links.addAll(linkPath.links());
            cost += linkPath.cost();
        }

        links.add(dstLink);

        return new DefaultPath(routeProviderId, links, cost);
    }

    //=================== The End =====================


    /**
     * Tool for calculating weight value for each Link(TopologyEdge).
     *
     * @author Mao.
     */
    private class BandwidthLinkWeight implements LinkWeight {

        //        private static final double LINK_LINE_SPEED = 10000000000.0; // 10Gbps
        private static final double LINK_WEIGHT_DOWN = 100.0;
        private static final double LINK_WEIGHT_FULL = 100.0;

        //FIXME - Bata1: Here, assume the edge is the inter-demain link
        @Override
        public double weight(TopologyEdge edge) {

            if (edge.link().state() == Link.State.INACTIVE) {
                return LINK_WEIGHT_DOWN;
            }


            long linkLineSpeed = getLinkLineSpeed(edge.link());

            //FIXME - Bata1: Here, assume the value in the map is the rest bandwidth of inter-demain link
            long interLinkRestBandwidth = linkLineSpeed - getLinkLoadSpeed(edge.link());

            if (interLinkRestBandwidth <= 0) {
                return LINK_WEIGHT_FULL;
            }

            return 100 - interLinkRestBandwidth * 1.0 / linkLineSpeed * 100;//restBandwidthPersent
        }

        private long getLinkLineSpeed(Link link) {

            long srcSpeed = getPortLineSpeed(link.src());
            long dstSpeed = getPortLineSpeed(link.dst());

            return min(srcSpeed, dstSpeed);
        }

        private long getLinkLoadSpeed(Link link) {

            long srcSpeed = getPortLoadSpeed(link.src());
            long dstSpeed = getPortLoadSpeed(link.dst());

            return max(srcSpeed, dstSpeed);
        }

        /**
         * Unit: bps
         *
         * @param port
         * @return
         */
        private long getPortLoadSpeed(ConnectPoint port) {

            return portStatisticsService.load(port).rate() * 8;//data source: Bps

        }

        /**
         * Unit bps
         *
         * @param port
         * @return
         */
        private long getPortLineSpeed(ConnectPoint port) {

            assert port.elementId() instanceof DeviceId;
            return deviceService.getPort(port.deviceId(), port.port()).portSpeed() * 1000000;//data source: Mbps

        }

        private long max(long a, long b) {
            return a > b ? a : b;
        }

        private long min(long a, long b) {
            return a < b ? a : b;
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
                Ethernet eth = null;
                PortNumber inPort = null;
                if (sbp.getSbpCmpType().equals(OXPSbpCmpType.NORMAL)) {
                    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
                    sbp.getSbpData().writeTo(buffer);
                    OFMessage ofMsg = superController.parseOfMessage(sbp);
                    //只处理packet-in消息
                    if (null == ofMsg || ofMsg.getType() != OFType.PACKET_IN) {
                        return;
                    }
                    OFPacketIn packetIn = (OFPacketIn) ofMsg;
                    inPort = PortNumber.portNumber(packetIn.getMatch().get(MatchField.IN_PORT).getPortNumber());
                    eth = superController.parseEthernet(packetIn.getData());
                } else if (sbp.getSbpCmpType().equals(OXPSbpCmpType.FORWARDING_REQUEST)){
                    OXPForwardingRequest sbpCmpFwdReq = (OXPForwardingRequest)sbp.getSbpCmpData();
                    inPort = PortNumber.portNumber(sbpCmpFwdReq.getInport());
                    eth = superController.parseEthernet(sbpCmpFwdReq.getData());
                }
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

    class InternalDomainListener implements OxpDomainListener {
        @Override
        public void domainConnected(OXPDomain domain) {

        }

        @Override
        public void domainDisconnected(OXPDomain domain) {
            for (PortNumber vport : vportMap.get(domain.getDeviceId())) {
                ConnectPoint vportLocation = new ConnectPoint(domain.getDeviceId(), vport);
                vportDescMap.remove(vportLocation);
                vportCapabilityMap.remove(vportLocation);
            }
            vportMap.remove(domain.getDeviceId());
        }
    }
}
