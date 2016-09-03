package org.onosproject.oxp;

import org.onosproject.net.DeviceId;
import org.onosproject.oxp.protocol.OXPMessage;

import java.util.List;

/**
 * Created by cr on 16-9-3.
 */
public interface OxpDomainMessageListener {
    void handleIncomingMessage(DeviceId deviceId, OXPMessage msg);

    void handleOutGoingMessage(DeviceId deviceId, List<OXPMessage> msgs);
}
