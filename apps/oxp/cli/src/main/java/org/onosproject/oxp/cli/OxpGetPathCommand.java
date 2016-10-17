package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.types.OXPHost;

import java.util.Set;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-getpath",
        description = "Get inter path between two hosts.")
public class OxpGetPathCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "srcIp", description = "Src host ip address.",
            required = true, multiValued = false)
    String src = null;

    @Argument(index = 1, name = "dstIp", description = "Dst host ip address.",
            required = true, multiValued = false)
    String dst = null;

    @Override
    protected void execute() {
        ObjectNode root = mapper().createObjectNode();
        // srcIp, dstIp
        IpAddress srcIp = Ip4Address.valueOf(src);
        IpAddress dstIp = Ip4Address.valueOf(dst);
        // srcHost, dstHost
        Set<OXPHost> srcHosts = get(OxpSuperTopoService.class).getHostsByIp(srcIp);
        Set<OXPHost> dstHosts = get(OxpSuperTopoService.class).getHostsByIp(dstIp);
        if (srcHosts.isEmpty() || dstHosts.isEmpty()) {
            print("IsReachable:%s", false);
            print("IsDirected: %s", false);
            return;
        }
        OXPHost srcHost = (OXPHost) srcHosts.toArray()[0];
        OXPHost dstHost = (OXPHost) dstHosts.toArray()[0];
        // srcDevice, dstDevice
        HostId srcHostId = HostId.hostId(MacAddress.valueOf(srcHost.getMacAddress().getLong()));
        DeviceId srcDeviceId = get(OxpSuperTopoService.class).getHostLocation(srcHostId);
        HostId dstHostId = HostId.hostId(MacAddress.valueOf(dstHost.getMacAddress().getLong()));
        DeviceId dstDeviceId = get(OxpSuperTopoService.class).getHostLocation(dstHostId);

        if (srcDeviceId.equals(dstDeviceId)) {
            print("IsReachable:%s", true);
            print("IsDirected: %s", true);
            return;
        }
        // Path
        Set<org.onosproject.net.Path> paths = get(OxpSuperTopoService.class).getLoadBalancePaths(srcDeviceId, dstDeviceId);
        if (paths.isEmpty()) ;
        org.onosproject.net.Path path = (org.onosproject.net.Path) paths.toArray()[0];
        // result
        root.put("isReachable", true);
        root.put("isDirected", false);
        ArrayNode array = root.putArray("path");
        for (Link link : path.links()) {
            ObjectNode linkNode = mapper().createObjectNode();
            linkNode.put("srcDomain", get(OxpSuperController.class).getOxpDomain(link.src().deviceId()).getDomainId().getLong());
            linkNode.put("dstDomain", get(OxpSuperController.class).getOxpDomain(link.dst().deviceId()).getDomainId().getLong());
            linkNode.put("srcVport", link.src().port().toLong());
            linkNode.put("dstVport", link.dst().port().toLong());
            array.add(linkNode);
        }
        print("%s", root);
    }
}
