/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fnl.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;

import org.fnl.intf.MaoRoutingService;
import org.onlab.packet.EthType;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.IpPrefix;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.*;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.statistic.Load;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
@Service
public class MaoRoutingManager implements MaoRoutingService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PortStatisticsService portStatisticsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;

    private ApplicationId appId;

    private Set<Key> keySet;

    @Activate
    protected void activate() {
        log.info("Started");

        packetService.addProcessor(packetProcessor, PacketProcessor.director(0));

        appId = coreService.registerApplication("org.fnl.test");

        packetService.requestPackets(DefaultTrafficSelector.builder()
                        .matchEthType(Ethernet.TYPE_IPV4).build(),
                PacketPriority.REACTIVE,
                appId);

        keySet = new HashSet<>();
//        while(true) {
//            Iterable<Device> devices = deviceService.getDevices();
//            for (Device d : devices) {
//                List<Port> ports = deviceService.getPorts(d.id());
//                for (Port p : ports) {
//                    PortNumber portNumber = p.number();
//                    long portSpeed = p.portSpeed();
//                    int a = 0;
//                }
//            }
//
//
//            Iterable<Link> links = linkService.getLinks();
//            for (Link l : links) {
//                Load srcLoad = portStatisticsService.load(l.src());
//                Load dstLoad = portStatisticsService.load(l.dst());
//                int a = 0;
//            }
//        }
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
        packetService.removeProcessor(packetProcessor);
        packetService.cancelPackets(DefaultTrafficSelector.builder()
                        .matchEthType(Ethernet.TYPE_IPV4).build(),
                PacketPriority.REACTIVE,
                coreService.registerApplication("org.fnl.test"));


        keySet.forEach(key -> {
            intentService.withdraw(intentService.getIntent(key));
        });
        keySet.clear();
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private IntentService intentService;

    private InternalPacketProcessor packetProcessor = new InternalPacketProcessor();

    private class InternalPacketProcessor implements PacketProcessor {

        private volatile int count = 0;
        private Integer lock = 0;

        @Override
        public void process(PacketContext context) {

            synchronized (lock) {
//                if (context.isHandled() || count > 0) {
                if (context.isHandled()) {

                    return;

                }
            }

            Ethernet pkt = context.inPacket().parsed();
            if (pkt.getEtherType() == Ethernet.TYPE_IPV4) {

                Host srcHost = hostService.getHost(HostId.hostId(pkt.getSourceMAC()));
                Host dstHost = hostService.getHost(HostId.hostId(pkt.getDestinationMAC()));
                if(srcHost==null || dstHost==null){
                    log.warn("Routing but Host is null, not found");
                    return;
                }


                DeviceId srcDevice = srcHost.location().deviceId();
                DeviceId dstDevice = dstHost.location().deviceId();

                Set<Path> qingdao = getLoadBalancePaths(srcDevice, dstDevice);
                if (qingdao.isEmpty()) {
                    log.warn("Mao: qingdao is Empty !!!");
                    return;
                }

                IPv4 ipPkt = (IPv4) pkt.getPayload();
                TrafficSelector selector = DefaultTrafficSelector.builder()
                        .matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(IpPrefix.valueOf(ipPkt.getSourceAddress(), 32))
                        .matchIPDst(IpPrefix.valueOf(ipPkt.getDestinationAddress(), 32))
                        .build();

//                TrafficTreatment treatment = DefaultTrafficTreatment.builder()
//                        .setOutput(portNumber)
//                        .build();
                Path wholePath = getEdgeToEdgePath(
                        getEdgeLink(srcHost, true),
                        getEdgeLink(dstHost,false),
                        qingdao.iterator().next());

                PathIntent pathIntent = PathIntent.builder()
                        .path(wholePath)
                        .appId(appId)
                        .priority(63333)
                        .selector(selector)
                        .treatment(DefaultTrafficTreatment.emptyTreatment())
                        .build();

                HostToHostIntent hostToHostIntent = HostToHostIntent.builder()
                        .appId(appId)
                        .one(HostId.hostId(pkt.getSourceMAC()))
                        .two(HostId.hostId(pkt.getDestinationMAC()))
                        .selector(selector)
                        .treatment(DefaultTrafficTreatment.emptyTreatment())
                        .priority(33333)
                        .build();

                intentService.submit(pathIntent);
                keySet.add(pathIntent.key());
                synchronized (lock) {
                    count++;
                }

                context.treatmentBuilder().drop().build();
                context.send();
//                TrafficTreatment treatment = DefaultTrafficTreatment.builder()
//                        .setOutput(portNumber)
//                        .build();
//
//                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
//                        .withSelector(selectorBuilder.build())
//                        .withTreatment(treatment)
//                        .withPriority(flowPriority)
//                        .withFlag(ForwardingObjective.Flag.VERSATILE)
//                        .fromApp(appId)
//                        .makeTemporary(flowTimeout)
//                        .add();
            }
        }
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private TopologyService topologyService;

    private ProviderId providerId = new ProviderId("fnl", "org.fnl.test");


    private Set<Path> getLoadBalancePaths(DeviceId src, DeviceId dst) {
        Topology currentTopo = topologyService.currentTopology();
        return topologyService.getPaths(currentTopo, src, dst, bandwidthLinkWeightTool);
    }

    // Finds the host edge link if the element ID is a host id of an existing
    // host. Otherwise, if the host does not exist, it returns null and if
    // the element ID is not a host ID, returns NOT_HOST edge link.
    private EdgeLink getEdgeLink(Host host, boolean isIngress) {
        return new DefaultEdgeLink(providerId, new ConnectPoint(host.id(), PortNumber.portNumber(0)),
                host.location(), isIngress);
    }

    // Produces a direct edge-to-edge path.
    private Path getEdgeToEdgePath(EdgeLink srcLink, EdgeLink dstLink, Path path) {

        if (srcLink == null || dstLink == null || path == null) {
            log.warn("Generate EdgePath but found null");
            return null;
        }

        List<Link> links = Lists.newArrayListWithCapacity(2);
        double cost = 0;

        links.add(srcLink);
        cost++;

        links.addAll(path.links());
        cost += path.cost();

        links.add(dstLink);
        cost++;

        return new DefaultPath(providerId, links, cost);
    }

    private BandwidthLinkWeight bandwidthLinkWeightTool = new BandwidthLinkWeight();

    private class BandwidthLinkWeight implements LinkWeight {

        //        private static final double LINK_LINE_SPEED = 10000000000.0; // 10Gbps
        private static final double LINK_WEIGHT_DOWN = -1.0;
        private static final double LINK_WEIGHT_FULL = 0.0;

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
            double restBandwidthPersent = 100 - interLinkRestBandwidth * 1.0 / linkLineSpeed * 100 ;
//            restBandwidthPersent = 1.0;
            return restBandwidthPersent;
        }

        private long getLinkLineSpeed(Link link) {

            long srcSpeed = getPortLineSpeed(link.src());
            long dstSpeed = getPortLineSpeed(link.dst());

            assert srcSpeed == dstSpeed;
            return srcSpeed;
        }

        private long getLinkLoadSpeed(Link link) {

            long srcSpeed = getPortLoadSpeed(link.src());
            long dstSpeed = getPortLoadSpeed(link.dst());

            return max(srcSpeed, dstSpeed);
        }

//        private long getLinkRestSpeed(Link link){
//
//            long srcSpeed = getPortLoadSpeed(link.src());
//            long dstSpeed = getPortLoadSpeed(link.dst());
//
//            return max(srcSpeed, dstSpeed);
//        }

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
}
