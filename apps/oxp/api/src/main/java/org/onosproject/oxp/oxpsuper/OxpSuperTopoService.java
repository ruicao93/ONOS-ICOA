package org.onosproject.oxp.oxpsuper;

import org.onlab.packet.IpAddress;
import org.onosproject.net.*;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceInterfaceDescription;
import org.onosproject.net.topology.Topology;
import org.onosproject.oxp.protocol.OXPVportDesc;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.types.OXPVport;

import java.util.List;
import java.util.Set;

/**
 * Created by cr on 16-9-1.
 */
public interface OxpSuperTopoService {

    List<PortNumber> getVports(DeviceId deviceId);

    List<Link> getInterlinks();

    long getInterLinkCapability(Link link);

    List<Link> getIntraLinks(DeviceId deviceId);

    OXPInternalLink getIntraLinkDesc(Link link);

    OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber);

    long getVportMaxCapability(ConnectPoint portLocation);
    long getVportLoadCapability(ConnectPoint portLocation);
    long getVportRestCapability(ConnectPoint portLocation);

    Set<OXPHost> getHostsByIp(IpAddress ipAddress);

    DeviceId getHostLocation(HostId hostId);

    Set<OXPHost> getHostsByDevice(DeviceId deviceId);


    Set<Path> getPaths(DeviceId src, DeviceId dst);

    Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight);

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
    Set<Path> getLoadBalancePaths(Topology topo, ElementId src, ElementId dst, LinkWeight linkWeight);
    Set<Path> getLoadBalancePaths(Topology topo, ElementId src, ElementId dst);
    Set<Path> getLoadBalancePaths(ElementId src, ElementId dst, LinkWeight linkWeight);
    Set<Path> getLoadBalancePaths(ElementId src, ElementId dst);

    long getInterLinkCount();

    long getHostCount();

}
