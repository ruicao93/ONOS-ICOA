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
package org.fnl;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Deactivate;

import org.onosproject.incubator.net.PortStatisticsService;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.statistic.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class MaoRestApp {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PortStatisticsService portStatisticsService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private LinkService linkService;

//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    private PortStatisticsService portStatisticsService;


    @Activate
    protected void activate() {
        log.info("Started");

//        while(true) {
            Iterable<Device> devices = deviceService.getDevices();
            for (Device d : devices) {
                List<Port> ports = deviceService.getPorts(d.id());
                for (Port p : ports) {
                    PortNumber portNumber = p.number();
                    long portSpeed = p.portSpeed();
                    int a = 0;
                }
            }


            Iterable<Link> links = linkService.getLinks();
            for (Link l : links) {
                Load srcLoad = portStatisticsService.load(l.src());
                Load dstLoad = portStatisticsService.load(l.dst());
                int a = 0;
            }
//        }
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

}
