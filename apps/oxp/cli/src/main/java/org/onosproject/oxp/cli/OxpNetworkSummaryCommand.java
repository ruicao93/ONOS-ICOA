package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.OXPConfigFlags;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-network",
        description = "Show oxp network summary.")
public class OxpNetworkSummaryCommand extends AbstractShellCommand {


    @Override
    protected void execute() {
        ObjectNode root = mapper().createObjectNode();
        long domainCount = get(OxpSuperController.class).getDomainCount();
        long linkCount = get(OxpSuperTopoService.class).getInterLinkCount();
        long hostCount = get(OxpSuperTopoService.class).getHostCount();
        root.put("domainCount", domainCount)
                .put("linkCount", linkCount)
                .put("hostCount", hostCount);
        boolean isLoadBalance = get(OxpSuperController.class).isLoadBalance();
        root.put("isLoadBalance", isLoadBalance);
        OXPConfigFlags pathComputeParam = get(OxpSuperController.class).getPathComputeParam();
        root.put("pathComputeParam", pathComputeParam.name());
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
        print("%s", root);
    }
}
