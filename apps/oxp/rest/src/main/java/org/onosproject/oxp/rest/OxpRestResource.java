package org.onosproject.oxp.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.types.OXPHost;
import org.onosproject.oxp.types.OXPVport;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

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
            for (PortNumber vport : get(OxpSuperTopoService.class).getVports(domain.getDeviceId())) {
                ObjectNode vportNode = mapper().createObjectNode();
                vportNode.put("vportNum", vport.toLong());
                vportNode.put("domainId", domain.getDomainId().getLong());
                vports.add(vportNode);
            }
        }
        return ok(root).build();
    }

}
