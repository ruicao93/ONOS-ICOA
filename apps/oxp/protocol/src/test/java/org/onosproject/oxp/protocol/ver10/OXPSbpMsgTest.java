package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPSbpData;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-23.
 */
public class OXPSbpMsgTest extends TestBaseVer10 {
    @Test
    public void OXPEchoRequestMsgTest() throws Exception{


        ChannelBuffer dataBuffer = ChannelBuffers.dynamicBuffer();
        OXPEchoRequest echoRequest = getMsgFactory()
                .buildEchoRequest()
                .setData("OXPEchoRequestMsg test.".getBytes())
                .build();
        echoRequest.writeTo(dataBuffer);

        byte[] data = new byte[dataBuffer.readableBytes()];
        dataBuffer.getBytes(0, data);


        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.NORMAL;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbpData sbpData = OXPSbpData.of(data, OXPVersion.OXP_10);

        OXPSbp sbpMsg = getMsgFactory()
                .buildSbp()
                .setSbpData(sbpData)
                .build();
        sbpMsg.writeTo(buffer);
        assertThat(sbpMsg, instanceOf(OXPSbpVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(sbpMsg.getClass()));

        OXPSbp messageRev = (OXPSbp) message;
        assertThat(sbpMsg, is(messageRev));

        ChannelBuffer sbpDataBuffer = ChannelBuffers.dynamicBuffer();
        sbpDataBuffer.writeBytes(sbpMsg.getSbpData().getData());
        OXPMessage sbpDataCopy = getMsgReader().readFrom(sbpDataBuffer);
        assertThat(sbpDataCopy, instanceOf(echoRequest.getClass()));

        OXPEchoRequest sbpDataParsed = (OXPEchoRequest) sbpDataCopy;
        assertThat(echoRequest, is(sbpDataParsed));
    }
}
