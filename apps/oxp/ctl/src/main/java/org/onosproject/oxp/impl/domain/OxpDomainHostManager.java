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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.onlab.util.Tools.groupedThreads;
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
    private ScheduledExecutorService executor;
    private boolean bootFlag = false;
    @Activate
    public void activate() {
        domainController.addOxpSuperListener(oxpSuperListener);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        domainController.removeOxpSuperListener(oxpSuperListener);
        if (!bootFlag) {
            return;
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        domainController.removeMessageListener(oxpMsgListener);
        hostService.removeListener(hostListener);
        log.info("Stoped");
    }
    private void setUp() {
        bootFlag = true;
        oxpVersion = domainController.getOxpVersion();
        oxpFactory = OXPFactories.getFactory(oxpVersion);
        executor = newSingleThreadScheduledExecutor(groupedThreads("oxp/hostupdate", "oxp-hostupdate-%d", log));
        executor.scheduleAtFixedRate(new HostUpdateTask(),
                domainController.getPeriod(), domainController.getPeriod(), SECONDS);
        domainController.addMessageListener(oxpMsgListener);
        hostService.addListener(hostListener);
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
            if (oxpHosts.isEmpty()) {
                return;
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
            setUp();
            updateExistHosts(null);
        }

        @Override
        public void disconnectFromSuper(OxpSuper oxpSuper) {

        }
    }

    class HostUpdateTask implements Runnable {
        @Override
        public void run() {
            updateExistHosts(null);
        }
    }
}
