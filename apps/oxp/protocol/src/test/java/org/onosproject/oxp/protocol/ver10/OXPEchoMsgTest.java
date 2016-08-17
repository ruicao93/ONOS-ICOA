package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.OXPEchoReply;
import org.onosproject.oxp.protocol.OXPEchoRequest;
import org.onosproject.oxp.protocol.OXPMessage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-18.
 */
public class OXPEchoMsgTest extends TestBaseVer10{

    @Test
    public void OXPEchoRequestMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPEchoRequest echoRequest = getMsgFactory()
                .buildEchoRequest()
                .setData("OXPEchoRequestMsg test.".getBytes())
                .build();
        echoRequest.writeTo(buffer);
        assertThat(echoRequest, instanceOf(OXPEchoRequestVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(echoRequest.getClass()));

        OXPEchoRequest messageRev = (OXPEchoRequest) message;
        assertThat(echoRequest, is(messageRev));
    }

    @Test
    public void OXPEchoReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPEchoReply oxpEchoReply = getMsgFactory()
                .buildEchoReply()
                .setData("OXPEchoRequestMsg test.".getBytes())
                .build();
        oxpEchoReply.writeTo(buffer);
        assertThat(oxpEchoReply, instanceOf(OXPEchoReplyVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(oxpEchoReply.getClass()));

        OXPEchoReply messageRev = (OXPEchoReply) message;
        assertThat(oxpEchoReply, is(messageRev));
    }
}
