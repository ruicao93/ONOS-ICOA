package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.PortNumber;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-vports",
        description = "Show oxp vports.")
public class OxpVportListCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
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
                vportNode.put("capability", get(OxpSuperTopoService.class).getVportRestCapability(
                        new ConnectPoint(domain.getDeviceId(), vport)));
                vportNode.put("loadCapability", get(OxpSuperTopoService.class).getVportLoadCapability(
                        new ConnectPoint(domain.getDeviceId(), vport)));
                vportNode.put("maxCapability", get(OxpSuperTopoService.class).getVportMaxCapability(
                        new ConnectPoint(domain.getDeviceId(), vport)));
                vportArray.add(vportNode);
            }
            vports.add(domainNode);
        }

        print("%s", root);
    }
}
