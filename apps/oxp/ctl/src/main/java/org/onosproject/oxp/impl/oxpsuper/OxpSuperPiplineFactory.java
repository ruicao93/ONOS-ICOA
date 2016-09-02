package org.onosproject.oxp.impl.oxpsuper;

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
import org.onosproject.oxp.impl.domain.OxpDomainChannelHandler;
import org.onosproject.oxp.oxpsuper.OxpSuperController;

/**
 * Created by cr on 16-9-1.
 */
public class OxpSuperPiplineFactory implements ChannelPipelineFactory, ExternalResourceReleasable {

    private OxpSuperController superController;
    protected Timer timer;
    protected IdleStateHandler idleStateHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    public OxpSuperPiplineFactory(OxpSuperController superController) {
        super();
        this.superController = superController;
        this.timer = new HashedWheelTimer();
        this.idleStateHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        OxpSuperChannelHandler handler = new OxpSuperChannelHandler(superController);

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
