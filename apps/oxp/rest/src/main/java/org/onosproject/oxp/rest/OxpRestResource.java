package org.onosproject.oxp.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
    @Path("fnl")
    public Response fnl(){
        ObjectNode root = mapper().createObjectNode();
        ArrayNode array = root.putArray("Beijing Tower");
        array.add(118.5).add(120.3);

        root.put("708", "10.103.89.*").put("738", "10.103.90.*");

        return ok(root).build();
    }
}
