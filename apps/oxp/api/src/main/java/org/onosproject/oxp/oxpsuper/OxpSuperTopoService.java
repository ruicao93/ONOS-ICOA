package org.onosproject.oxp.oxpsuper;

import org.onlab.packet.IpAddress;
import org.onosproject.net.*;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceInterfaceDescription;
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

    List<Link> getIntraLinks(DeviceId deviceId);

    OXPInternalLink getInterLinkDesc(Link link);

    OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber);

    long getVportCapability(PortNumber portNumber);

    Set<OXPHost> getHostsByIp(IpAddress ipAddress);

    DeviceId getHostLocation(HostId hostId);

    Set<OXPHost> getHostsByDevice(DeviceId deviceId);


    Set<Path> getPaths(DeviceId src, DeviceId dst);

    Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight);

    // just one best Path is returned now.
    Set<Path> getLoadBalancePaths(DeviceId src, DeviceId dst);

    long getInterLinkCount();

    long getHostCount();

}
