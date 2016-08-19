package org.onosproject.oxp.impl;

import org.apache.felix.scr.annotations.*;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.oxp.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.OxpSuperListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.OXPFactories;
import org.onosproject.oxp.protocol.OXPFactory;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPVersion;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.types.OXPVport;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-8-18.
 */
@Component(immediate = true)
public class OxpDomainTopoManager {

    private final Logger log = getLogger(getClass());

    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OxpDomainController domainController;

    private LinkListener linkListener = new InternalLinkListener();
    private OxpSuperMessageListener oxpMsgListener = new InternalOxpSuperMsgListener();
    private OxpSuperListener oxpSuperListener = new InternalOxpSuperListener();

    @Activate
    public void activate() {
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        domainController.addMessageListener(oxpMsgListener);
        domainController.addOxpSuperListener(oxpSuperListener);
        linkService.addListener(linkListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        linkService.removeListener(linkListener);
        domainController.removeMessageListener(oxpMsgListener);
        domainController.removeOxpSuperListener(oxpSuperListener);
        log.info("Stoped");
    }

    private void sendTopoReplyMsg(List<OXPInternalLink> oxpInternalLinks) {

        //TODO
    }

    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            //TODO handle link event
        }
    }

    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
            //TODO handle topo req msg
        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }

    private class InternalOxpSuperListener implements OxpSuperListener {
        @Override
        public void connectToSuper(OxpSuper oxpSuper) {
            //TODO handle super online
        }

        @Override
        public void disconnectFromSuper(OxpSuper oxpSuper) {

        }
    }
}
