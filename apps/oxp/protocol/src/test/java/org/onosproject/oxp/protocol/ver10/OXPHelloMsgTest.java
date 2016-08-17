package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.exceptions.OXPParseError;
import org.onosproject.oxp.protocol.OXPHello;
import org.onosproject.oxp.protocol.OXPMessage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-16.
 */
public class OXPHelloMsgTest extends TestBaseVer10{

    @Test
    public void oxpHelloMsgTest() throws OXPParseError{

        OXPHello helloMsg = getMsgFactory().buildHello().build();
        assertThat(helloMsg, instanceOf(OXPHelloVer10.class));
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        helloMsg.writeTo(buffer);

        OXPMessage message;
        message = getMsgReader().readFrom(buffer);
        //check message type
        assertThat(message, instanceOf(helloMsg.getClass()));

        OXPHello messageRecv = (OXPHello) message;
        assertThat(helloMsg, is(messageRecv));
    }
}
