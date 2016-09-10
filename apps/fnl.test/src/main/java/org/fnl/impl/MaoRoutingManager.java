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

import org.apache.felix.scr.annotations.*;

import org.fnl.intf.MaoRoutingService;
import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.statistic.Load;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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




    @Activate
    protected void activate() {
        log.info("Started");

        packetService.addProcessor(packetProcessor, PacketProcessor.director(0));

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

    }

    private InternalPacketProcessor packetProcessor = new InternalPacketProcessor();
    private class InternalPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {

            if(context.isHandled()){
                return;
            }


        }
    }

//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    private TopologyService topologyService;
//
//
//    private Set<Path> getLoadBalancePaths(DeviceId src, DeviceId dst) {
//        return topologyService.getPaths(topologyService.currentTopology(), src, dst, bandwidthLinkWeightTool);
//    }
//
//    private BandwidthLinkWeight bandwidthLinkWeightTool = new BandwidthLinkWeight();
//    private class BandwidthLinkWeight implements LinkWeight {
//
//        private static final double LINK_LINE_SPEED = 10000000000.0; // 10Gbps
//        private static final double LINK_WEIGHT_DOWN = -1.0;
//        private static final double LINK_WEIGHT_FULL = 0.0;
//
//        //FIXME - Bata1: Here, assume the edge is the inter-demain link
//        @Override
//        public double weight(TopologyEdge edge){
//
//            if(edge.link().state() == Link.State.INACTIVE) {
//                return LINK_WEIGHT_DOWN;
//            }
//
//
//            //FIXME - Bata1: Here, assume the value in the map is the rest bandwidth of inter-demain link
//            long interLinkRestBandwidth =  vportCapabilityMap.get(edge.link());
//
//            if (interLinkRestBandwidth <= 0) {
//                return LINK_WEIGHT_FULL;
//            }
//            double restBandwidthPersent = interLinkRestBandwidth / LINK_LINE_SPEED * 100;
//            return restBandwidthPersent;
//        }
//    }
}
