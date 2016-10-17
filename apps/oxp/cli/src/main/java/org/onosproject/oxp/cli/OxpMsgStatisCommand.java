package org.onosproject.oxp.cli;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.OXPType;

import java.util.Map;

/**
 * Created by cr on 16-10-6.
 */
@Command(scope = "onos", name = "oxp-msg-statis",
        description = "Show oxp msg statistics.")
public class OxpMsgStatisCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
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
        print("%s", root);
    }
}
