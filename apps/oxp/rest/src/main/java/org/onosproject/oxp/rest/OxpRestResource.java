package org.onosproject.oxp.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.net.*;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.OXPType;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.net.DeviceId.deviceId;

/**
 * All of OXP API.
 */
@Path("oxp")
public class OxpRestResource extends AbstractWebResource {

    /**
     * Hello OXP ^0^.
     * Sample for extending RESTful API.
     * @return Infos of Fnl.
     */
    @GET
    @Path("/fnl")
    public Response fnl(){
        ObjectNode root = mapper().createObjectNode();
        ArrayNode array = root.putArray("Beijing Tower");
        array.add(118.5).add(120.3);

        root.put("708", "10.103.89.*").put("738", "10.103.90.*");

        return ok(root).build();
    }

    @GET
    @Path("/network")
    public Response network() {
        ObjectNode root = mapper().createObjectNode();
        long domainCount = get(OxpSuperController.class).getDomainCount();
        long linkCount = get(OxpSuperTopoService.class).getInterLinkCount();
        long hostCount = get(OxpSuperTopoService.class).getHostCount();
        root.put("domainCount", domainCount)
                .put("linkCount", linkCount)
                .put("hostCount", hostCount);
        ArrayNode domainArray = root.putArray("domains");
        for (OXPDomain domain : get(OxpSuperController.class).getOxpDomains()) {
            ObjectNode domainNode = mapper().createObjectNode();
            domainNode.put("id", domain.getDomainId().toString());
            if (domain.isAdvancedMode()) {
                domainNode.put("workMode", "Advanced");
                if (domain.isCapBwSet()) {
                    domainNode.put("capabilityType", "bandwidth");
                } else if (domain.isCapDelaySet()) {
                    domainNode.put("capabilityType", "delay");
                } else {
                    domainNode.put("capabilityType", "hop");
                }
            } else {
                domainNode.put("workMode", "Simple");
            }
            if (domain.isCompressedMode()) {
                domainNode.put("SBPTransferMode", "Compressed");
            } else {
                domainNode.put("SBPTransferMode", "Normal");
            }
            domainArray.add(domainNode);
        }
        return ok(root).build();
    }

    @GET
    @Path("/links")
    public Response links() {
        ObjectNode root = mapper().createObjectNode();
        List<Link> links = get(OxpSuperTopoService.class).getInterlinks();
        ArrayNode array = root.putArray("interLinks");
        for (Link link : links) {
            ObjectNode linkNode = mapper().createObjectNode();
            linkNode.put("srcDomain", get(OxpSuperController.class).getOxpDomain(link.src().deviceId()).getDomainId().getLong());
            linkNode.put("dstDomain", get(OxpSuperController.class).getOxpDomain(link.dst().deviceId()).getDomainId().getLong());
            linkNode.put("srcVport", link.src().port().toLong());
            linkNode.put("dstVport", link.dst().port().toLong());
            linkNode.put("capability", get(OxpSuperTopoService.class).getInterLinkCapability(link));
            array.add(linkNode);
        }
        return ok(root).build();
    }

    @GET
    @Path("/hosts")
    public Response hosts() {
        ObjectNode root = mapper().createObjectNode();
        long id = 1L;
        ArrayNode hosts = root.putArray("hosts");
        for (OXPDomain domain : get(OxpSuperController.class).getOxpDomains()) {
            for (OXPHost host : get (OxpSuperTopoService.class).getHostsByDevice(domain.getDeviceId())) {
                ObjectNode hostNode = mapper().createObjectNode();
                hostNode.put("id", id++);
                hostNode.put("domainId", domain.getDomainId().getLong());
                hostNode.put("ip", host.getIpAddress().toString());
                hosts.add(hostNode);
            }
        }
        return ok(root).build();
    }

    @GET
    @Path("/vports")
    public Response vports() {
        ObjectNode root = mapper().createObjectNode();
        long id = 1L;
        ArrayNode vports = root.putArray("vports");
        for (OXPDomain domain : get(OxpSuperController.class).getOxpDomains()) {
            ObjectNode domainNode = mapper().createObjectNode();
            domainNode.put("domainId", domain.getDomainId().getLong());
            ArrayNode vportArray = domainNode.putArray("vports");
            for (PortNumber vport : get(OxpSuperTopoService.class).getVports(domain.getDeviceId())) {
                ObjectNode vportNode = mapper().createObjectNode();
                vportNode.put("vportNum", vport.toLong());
                vportNode.put("capability", get(OxpSuperTopoService.class).getVportMaxCapability(
                        new ConnectPoint(domain.getDeviceId(), vport)));
                vportArray.add(vportNode);
            }
            vports.add(domainNode);
        }
        return ok(root).build();
    }

    @GET
    @Path("/msgStatis")
    public Response msgStatis() {
        ObjectNode root = mapper().createObjectNode();
        long id = 1L;
        ArrayNode array = root.putArray("msgStatis");
        Map<OXPType, Long> msgCountStatis = get(OxpSuperController.class).getMsgCountStatis();
        Map<OXPType, Long> msgLengthStatis = get(OxpSuperController.class).getMsgLengthStatis();
        for (OXPType type : msgCountStatis.keySet()) {
            ObjectNode typeNode = mapper().createObjectNode();
            typeNode.put("type", type.getName());
            typeNode.put("count", msgCountStatis.get(type));
            typeNode.put("length", msgLengthStatis.get(type));
            array.add(typeNode);
        }
        return ok(root).build();
    }

    @GET
    @Path("/getPath/{src}/{dst}")
    public Response getPath(@PathParam("src") String src, @PathParam("dst") String dst) {
        ObjectNode root = mapper().createObjectNode();
        // srcIp, dstIp
        IpAddress srcIp = Ip4Address.valueOf(src);
        IpAddress dstIp = Ip4Address.valueOf(dst);
        // srcHost, dstHost
        Set<OXPHost> srcHosts = get(OxpSuperTopoService.class).getHostsByIp(srcIp);
        Set<OXPHost> dstHosts = get(OxpSuperTopoService.class).getHostsByIp(dstIp);
        if (srcHosts.isEmpty() || dstHosts.isEmpty()) {
            root.put("isReachable", false);
            root.put("isDirected", false);
            return ok(root).build();
        }
        OXPHost srcHost = (OXPHost) srcHosts.toArray()[0];
        OXPHost dstHost = (OXPHost) dstHosts.toArray()[0];
        // srcDevice, dstDevice
        HostId srcHostId = HostId.hostId(MacAddress.valueOf(srcHost.getMacAddress().getLong()));
        DeviceId srcDeviceId = get(OxpSuperTopoService.class).getHostLocation(srcHostId);
        HostId dstHostId = HostId.hostId(MacAddress.valueOf(dstHost.getMacAddress().getLong()));
        DeviceId dstDeviceId = get(OxpSuperTopoService.class).getHostLocation(dstHostId);

        if (srcDeviceId.equals(dstDeviceId)) {
            root.put("isReachable", true);
            root.put("isDirected", true);
            return ok(root).build();
        }
        // Path
        Set<org.onosproject.net.Path> paths = get(OxpSuperTopoService.class).getPaths(srcDeviceId, dstDeviceId);
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
        return ok(root).build();
    }

    @GET
    @Path("/getLoadBalancePath/{src}/{dst}")
    public Response getLoadBalancePath(@PathParam("src") String src, @PathParam("dst") String dst) {
        ObjectNode root = mapper().createObjectNode();
        // srcIp, dstIp
        IpAddress srcIp = Ip4Address.valueOf(src);
        IpAddress dstIp = Ip4Address.valueOf(dst);
        // srcHost, dstHost
        Set<OXPHost> srcHosts = get(OxpSuperTopoService.class).getHostsByIp(srcIp);
        Set<OXPHost> dstHosts = get(OxpSuperTopoService.class).getHostsByIp(dstIp);
        if (srcHosts.isEmpty() || dstHosts.isEmpty()) {
            root.put("isReachable", false);
            root.put("isDirected", false);
            return ok(root).build();
        }
        OXPHost srcHost = (OXPHost) srcHosts.toArray()[0];
        OXPHost dstHost = (OXPHost) dstHosts.toArray()[0];
        // srcDevice, dstDevice
        HostId srcHostId = HostId.hostId(MacAddress.valueOf(srcHost.getMacAddress().getLong()));
        DeviceId srcDeviceId = get(OxpSuperTopoService.class).getHostLocation(srcHostId);
        HostId dstHostId = HostId.hostId(MacAddress.valueOf(dstHost.getMacAddress().getLong()));
        DeviceId dstDeviceId = get(OxpSuperTopoService.class).getHostLocation(dstHostId);

        if (srcDeviceId.equals(dstDeviceId)) {
            root.put("isReachable", true);
            root.put("isDirected", true);
            return ok(root).build();
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
        return ok(root).build();
    }

}
