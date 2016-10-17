package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.oxp.oxpsuper.OxpSuperController;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-loadbalance",
        description = "Show or config oxp loadbalance status.")
public class OxpLoadBalanceCommand  extends AbstractShellCommand {

    @Argument(index = 0, name = "operation", description = "Show or set.",
            required = false, multiValued = false)
    String operation = "show";

    @Argument(index = 1, name = "status", description = "Set loadbalance status.",
            required = false, multiValued = false)
    boolean status = true;

    @Override
    protected void execute() {
        if ("set".equals(operation)) {
            ObjectNode root = mapper().createObjectNode();
            boolean result = get(OxpSuperController.class).setLoadBalance(status);
            print("Set loadbalance status %s", result ? "success." : "failed.");
            boolean isLoadBalance = get(OxpSuperController.class).isLoadBalance();
            print("Current loadbalance staus: %s", isLoadBalance ? "active." : "deactive.");
        } else {
            boolean isLoadBalance = get(OxpSuperController.class).isLoadBalance();
            print("Current loadbalance staus: %s", isLoadBalance ? "active." : "deactive.");
        }

    }
}
