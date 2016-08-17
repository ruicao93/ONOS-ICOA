package org.onosproject.oxp.impl;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.IpAddress;
import org.onosproject.net.Host;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.oxp.OxpDomainController;
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

    @Activate
    public void activate() {
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        domainController.addMessageListener(oxpMsgListener);
        hostService.addListener(hostListener);
        //updateExistHosts();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        hostService.removeListener(hostListener);
        log.info("Stoped");
    }

    private void updateExistHosts() {
        List<OXPHost> oxpHosts = new ArrayList<>();
        for (Host host : hostService.getHosts()) {
            oxpHosts.addAll(toActiveOxpHosts(host));
        }
        sendHostChangeMsg(oxpHosts);
    }


    private void updateHost(Host host) {
        sendHostChangeMsg(toActiveOxpHosts(host));
    }

    private void removeHost(Host host) {
        sendHostChangeMsg(toInactiveOxpHosts(host));
    }

    private void sendHostChangeMsg(List<OXPHost> oxpHosts) {
        OXPHostUpdate hostUpdateMsg = oxpFactory.buildHostUpdate()
                .setHosts(oxpHosts)
                .build();

        if (!domainController.isConnectToSuper()) {
            return;
        }
        log.info("Host update,num:{} .", oxpHosts.size());
        //TODO fix here after test
        domainController.write(hostUpdateMsg);
    }
    private List<OXPHost> toActiveOxpHosts(Host host) {
        List<OXPHost> hosts = new ArrayList<>();
        for (IpAddress ip :host.ipAddresses()) {
            IPv4Address ipAddress = IPv4Address.of(ip.toOctets());
            MacAddress macAddress = MacAddress.of(host.mac().toBytes());
            OXPHost oxpHost = OXPHost.of(ipAddress, macAddress, IPv4Address.NO_MASK, OXPHostState.ACTIVE);
            hosts.add(oxpHost);
        }
        return hosts;
    }

    private List<OXPHost> toInactiveOxpHosts(Host host) {
        List<OXPHost> hosts = new ArrayList<>();
        for (IpAddress ip :host.ipAddresses()) {
            IPv4Address ipAddress = IPv4Address.of(ip.toOctets());
            MacAddress macAddress = MacAddress.of(host.mac().toBytes());
            OXPHost oxpHost = OXPHost.of(ipAddress, macAddress, IPv4Address.NO_MASK, OXPHostState.INACTIVE);
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
                oxpHosts.addAll(toInactiveOxpHosts(removedHost));
            }
            if (null != updatedHost) {
                oxpHosts.addAll(toActiveOxpHosts(updatedHost));
            }
            sendHostChangeMsg(oxpHosts);
        }
    }

    private class InternalOxpSuperMsgListener implements OxpSuperMessageListener {
        @Override
        public void handleIncomingMessage(OXPMessage msg) {
            if (msg.getType() != OXPType.OXPT_HOST_REQUEST) {
                return;
            }
            updateExistHosts();
        }

        @Override
        public void handleOutGoingMessage(List<OXPMessage> msgs) {

        }
    }
}
