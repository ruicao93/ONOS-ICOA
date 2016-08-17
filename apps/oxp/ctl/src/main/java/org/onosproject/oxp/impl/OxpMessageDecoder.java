package org.onosproject.oxp.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.onosproject.oxp.protocol.OXPFactories;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPMessageReader;

/**
 * Created by cr on 16-8-15.
 */
public class OxpMessageDecoder extends FrameDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            return null;
        }

        OXPMessageReader<OXPMessage> reader = OXPFactories.getGenericReader();
        OXPMessage message = reader.readFrom(buffer);

        return message;
    }
}
