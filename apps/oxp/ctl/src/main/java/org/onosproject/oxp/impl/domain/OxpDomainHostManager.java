package org.onosproject.oxp.impl.domain;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.IpAddress;
import org.onosproject.net.Host;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.domain.OxpSuperListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.MacAddress;
import org.onosproject.oxp.types.OXPHost;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-8-16.
 */
@Component(immediate = true)
public class OxpDomainHostManager {

    private final Logger log = getLogger(getClass());

    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OxpDomainController domainController;

    private HostListener hostListener = new InternalHostListener();
    private OxpSuperMessageListener oxpMsgListener = new InternalOxpSuperMsgListener();
    private OxpSuperListener oxpSuperListener = new InternalOxpSuperListener();

    @Activate
    public void activate() {
        int tryTimes = 10;
        int i = 0;
        while (oxpVersion == null && i < tryTimes) {
            oxpVersion = domainController.getOxpVersion();
            i++;
        }
        if (null == oxpVersion) {
            return;
        }
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        domainController.addMessageListener(oxpMsgListener);
        domainController.addOxpSuperListener(oxpSuperListener);
        hostService.addListener(hostListener);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        hostService.removeListener(hostListener);
        domainController.removeMessageListener(oxpMsgListener);
        domainController.removeOxpSuperListener(oxpSuperListener);
        log.info("Stoped");
    }

    private void updateExistHosts(OXPHostRequest oxpHostRequest) {
        List<OXPHost> oxpHosts = new ArrayList<>();
        for (Host host : hostService.getHosts()) {
            oxpHosts.addAll(toOxpHosts(host, OXPHostState.ACTIVE));
        }
        if (oxpHosts.isEmpty()) {
            return;
        }
        sendHostChangeMsg(oxpHosts, oxpHostRequest);
    }

    private void updateHost(Host host) {
        sendHostChangeMsg(toOxpHosts(host, OXPHostState.ACTIVE), null);
    }

    private void removeHost(Host host) {
        sendHostChangeMsg(toOxpHosts(host, OXPHostState.INACTIVE), null);
    }

    private void sendHostChangeMsg(List<OXPHost> oxpHosts, OXPHostRequest oxpHostRequest) {
        OXPMessage msg = null;
        if (null != oxpHostRequest) {
            msg = oxpFactory.buildHostReply()
                    .setHosts(oxpHosts)
                    .setXid(oxpHostRequest.getXid())
                    .build();
        } else {
            msg = oxpFactory.buildHostUpdate()
                    .setHosts(oxpHosts)
                    .build();
        }
        log.info("Host update,num:{} .", oxpHosts.size());
        domainController.write(msg);
    }
    private List<OXPHost> toOxpHosts(Host host,OXPHostState oxpHostState) {
        List<OXPHost> hosts = new ArrayList<>();
        for (IpAddress ip :host.ipAddresses()) {
            IPv4Address ipAddress = IPv4Address.of(ip.toOctets());
            MacAddress macAddress = MacAddress.of(host.mac().toBytes());
            OXPHost oxpHost = OXPHost.of(ipAddress, macAddress, IPv4Address.NO_MASK, oxpHostState);
            hosts.add(oxpHost);
        }
        return hosts;
    }
    
    private class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            Host updatedHost = null;
            Host removedHost = null;
            List<OXPHost> oxpHosts = new ArrayList<>();
            switch (event.type()) {
                case HOST_ADDED:
                    updatedHost = event.subject();
                    break;
                case HOST_REMOVED:
                    removedHost = event.subject();
                    break;
                case HOST_UPDATED:
                    updatedHost = event.subject();
                    removedHost = event.prevSubject();
                    break;
                default:
            }
            if (null != removedHost) {
                oxpHosts.addAll(toOxpHosts(removedHost, OXPHostState.INACTIVE));
            }
            if (null != updatedHost) {
                oxpHosts.addAll(toOxpHosts(updatedHost, OXPHostState.ACTIVE));
            }
            sendHostChangeMsg(oxpHosts, null);
        }
    }

    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
            if (msg.getType() != OXPType.OXPT_HOST_REQUEST) {
                return;
            }
            updateExistHosts((OXPHostRequest) msg);
        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }

    private class InternalOxpSuperListener implements OxpSuperListener {
        @Override
        public void connectToSuper(OxpSuper oxpSuper) {
            updateExistHosts(null);
        }

        @Override
        public void disconnectFromSuper(OxpSuper oxpSuper) {

        }
    }
}
