package org.onosproject.oxp;

import org.onosproject.oxp.protocol.OXPMessage;

import java.util.List;

/**
 * Notify providers about all OXP messages from OxpSuperController
 *
 * Created by cr on 16-8-14.
 */
public interface OxpSuperMessageListener {
    void handleIncomingMessage(OXPMessage msg);

    void handleOutGoingMessage(List<OXPMessage> msgs);
}
