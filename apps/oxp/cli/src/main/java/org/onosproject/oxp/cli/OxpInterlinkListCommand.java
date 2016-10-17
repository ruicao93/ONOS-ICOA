package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.Link;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;

import java.util.List;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-links",
        description = "Show oxp interlinks.")
public class OxpInterlinkListCommand  extends AbstractShellCommand {

    @Override
    protected void execute() {
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
            linkNode.put("loadCapability", get(OxpSuperTopoService.class).getInterLinkLoadCapability(link));
            array.add(linkNode);
        }
        print("%s", root);
    }
}
