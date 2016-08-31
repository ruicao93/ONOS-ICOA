package org.onosproject.oxp.impl.domain;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Created by cr on 16-8-14.
 */
public class DomainConnector {

    public static final Logger log = LoggerFactory.getLogger(DomainConnector.class);

    private static final OXPFactory FACTORY10 = OXPFactories.getFactory(OXPVersion.OXP_10);

    private ChannelGroup cg;

    //Configuration options
    protected int workerThreads = 16;

    private NioClientSocketChannelFactory execFactory;
    private ClientBootstrap bootstrap;
    private long systemStartTime;

    private OxpDomainController domainController;

    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    public DomainConnector(OxpDomainController domainController) {
        this.domainController = domainController;
    }

    public OXPFactory getOxpMessageFactory10() {
        return FACTORY10;
    }

    public long getSystemStartTime() {
        return systemStartTime;
    }

    /**
     * Tell OxpDomainController that we're ready to connect super controller.
     */
    public void run() {

        try {
            bootstrap = createBootStrap();
            bootstrap.setOption("reuseAddr", true);
            bootstrap.setOption("child.keepAlive", true);
            bootstrap.setOption("child.tcpNodelay", true);
            bootstrap.setOption("child.sendBufferSize",SEND_BUFFER_SIZE);

            //TODO PiplineFactory
            ChannelPipelineFactory pfact =
                    new OxpDomainPipeLineFactory(domainController);
            bootstrap.setPipelineFactory(pfact);
            cg = new DefaultChannelGroup();
            InetSocketAddress sa = new InetSocketAddress(domainController.getOxpSuperIp(),
                    domainController.getOxpSuperPort());
            bootstrap.connect(sa);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private ClientBootstrap createBootStrap() {
        if (workerThreads == 0) {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/oxpdomain", "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/oxpdomain","worker-%d")));
            return new ClientBootstrap(execFactory);
        } else {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/oxpdomain", "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/oxpdomain","worker-%d")),
                    workerThreads);
            return new ClientBootstrap(execFactory);
        }
    }

    public ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    public void init() {
        this.systemStartTime = System.currentTimeMillis();
    }

    /**
     * *******************************
     *  Statrs the OxpDomainConnector
     * *******************************
     */
    public void start() {
        log.info("Started");
        this.init();
        this.run();
    }

    public void stop() {
        log.info("Stopped");
        execFactory.shutdown();
        cg.close();
    }

    //*************************************
    // Operation about OxpDomainController
    //*************************************
    public Set<OXPConfigFlags> getFlags(){
        return domainController.getFlags();

    }
    public void setFlags(Set<OXPConfigFlags> flags) {
        domainController.setFlags(flags);

    }

    public int getPeriod() {
        return domainController.getPeriod();
    }
    public void setPeriod(int period) {
        domainController.setPeriod(period);
    }

    public long getMissSendLen() {
        return domainController.getMissSendLen();
    }
    public void setMissSendLen(long missSendLen) {
        domainController.setMissSendLen(missSendLen);
    }

    public Set<OXPCapabilities> getCapabilities() {
        return domainController.getCapabilities();
    }

    public DomainId getDomainId() {
        return domainController.getDomainId();
    }

    public OXPSbpType getOxpSbpTpe() {
        return domainController.getOxpSbpTpe();
    }

    public OXPSbpVersion getOxpSbpVersion() {
        return domainController.getOxpSbpVersion();
    }
}
