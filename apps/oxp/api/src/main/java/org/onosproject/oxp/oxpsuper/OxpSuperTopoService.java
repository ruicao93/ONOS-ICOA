package org.onosproject.oxp.oxpsuper;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.oxp.protocol.OXPVportDesc;
import org.onosproject.oxp.types.OXPHost;
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

    OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber);

    Set<OXPHost> getHostsByIp(IpAddress ipAddress);

    DeviceId getHostLocation(HostId hostId);

}
