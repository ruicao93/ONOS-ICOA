package org.onosproject.oxp.impl.domain;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.onosproject.oxp.domain.OxpDomainController;
import org.onosproject.oxp.impl.OxpMessageDecoder;
import org.onosproject.oxp.impl.OxpMessageEncoder;

/**
 * Created by cr on 16-8-15.
 */
public class OxpDomainPipeLineFactory
        implements ChannelPipelineFactory, ExternalResourceReleasable{

    private OxpDomainController domainController;
    protected Timer timer;
    protected IdleStateHandler idleStateHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    public OxpDomainPipeLineFactory(OxpDomainController domainController) {
        super();
        this.domainController = domainController;
        this.timer = new HashedWheelTimer();
        this.idleStateHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        OxpDomainChannelHandler handler = new OxpDomainChannelHandler(domainController);

        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("oxpmessagedecoder", new OxpMessageDecoder());
        pipeline.addLast("oxpmessageencoder", new OxpMessageEncoder());
        pipeline.addLast("idle", idleStateHandler);
        pipeline.addLast("timeout", readTimeoutHandler);
        pipeline.addLast("handler", handler);
        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        timer.stop();
    }
}
