package org.fnl.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Mao Test Label.
 */
@Path("mao")
public class MaoRestResource extends AbstractWebResource {

    @GET
    @Path("allPortSpeed")
    public Response getAllPortSpeed(){
        ObjectNode root = mapper().createObjectNode();
        root.put("Hello", 1080)
                .put("Mao",7181);

        ArrayNode array = root.putArray("RadioStation");
        array.add("192.168.1.1").add("127.0.0.1").add("10.3.8.211");

        return ok(root).build();
    }
}
