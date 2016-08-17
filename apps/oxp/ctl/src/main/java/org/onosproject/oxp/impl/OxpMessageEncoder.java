package org.onosproject.oxp.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.onosproject.oxp.protocol.OXPMessage;

import java.util.List;

/**
 * Created by cr on 16-8-15.
 */
public class OxpMessageEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof List)) {
            return msg;
        }
        List<OXPMessage> msgList = (List<OXPMessage>) msg;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

        for (OXPMessage oxpm :msgList) {
            if (oxpm != null) {
                oxpm.writeTo(buf);
            }
        }
        return buf;
    }
}
