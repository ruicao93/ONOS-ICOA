package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.types.OXPHost;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-hosts",
        description = "Show oxp hosts.")
public class OxpHostListCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
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
        print("%s", root);
    }
}
