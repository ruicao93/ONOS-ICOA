package org.onosproject.oxp.impl.oxpsuper;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Created by cr on 16-9-1.
 */
public class SuperConnector {
    public static final Logger log = LoggerFactory.getLogger(SuperConnector.class);

    private OxpSuperController superController;

    private NioServerSocketChannelFactory execFactory;
    protected static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;
    protected long systemStartTime;
    private ChannelGroup cg;
    protected int workerThreads = 16;


    public SuperConnector(OxpSuperController superController) {
        this.superController = superController;
    }


    public void init() {
        this.systemStartTime = System.currentTimeMillis();
    }

    public void run() {
        try {
            final ServerBootstrap bootstrap = createServerBootStrap();

            bootstrap.setOption("reuseAddr", true);
            bootstrap.setOption("child.keepAlive", true);
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.sendBufferSize", SEND_BUFFER_SIZE);

            ChannelPipelineFactory pfact =
                    new OxpSuperPiplineFactory(this.superController);
            bootstrap.setPipelineFactory(pfact);
            cg = new DefaultChannelGroup();
            InetSocketAddress sa = new InetSocketAddress(superController.getOxpSuperPort());
            cg.add(bootstrap.bind(sa));
            log.info("Listening for domain connections on {}", sa);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private ServerBootstrap createServerBootStrap() {

        if (workerThreads == 0) {
            execFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/oxp", "boss-%d", log)),
                    Executors.newCachedThreadPool(groupedThreads("onos/oxp", "worker-%d", log)));
            return new ServerBootstrap(execFactory);
        } else {
            execFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/oxp", "boss-%d", log)),
                    Executors.newCachedThreadPool(groupedThreads("onos/oxp", "worker-%d", log)), workerThreads);
            return new ServerBootstrap(execFactory);
        }
    }

    public void start() {
        init();
        run();
    }

    public void stop() {
        log.info("Stopping OxpSuper IO");
        cg.close();
        execFactory.shutdown();
    }
}
