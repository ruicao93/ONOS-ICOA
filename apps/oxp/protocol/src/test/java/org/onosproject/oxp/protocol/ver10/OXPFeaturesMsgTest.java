package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-19.
 */
public class OXPFeaturesMsgTest extends TestBaseVer10 {

    @Test
    public void OXPFeaturesRequestMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPFeaturesRequest featuresRequest = getMsgFactory()
                .buildFeaturesRequst()
                .build();
        featuresRequest.writeTo(buffer);
        assertThat(featuresRequest, instanceOf(OXPFeaturesRequestVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(featuresRequest.getClass()));

        OXPFeaturesRequest messageRev = (OXPFeaturesRequest) message;
        assertThat(featuresRequest, is(messageRev));
    }

    @Test
    public void OXPFeaturesReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPFeaturesReply featuresReply = getMsgFactory()
                .buildFeaturesReply()
                .setSbpType(OXPSbpType.OPENFLOW)
                .setSbpVersion(OXPSbpVersion.of((byte) 1, OXPVersion.OXP_10))
                .build();
        featuresReply.writeTo(buffer);
        assertThat(featuresReply, instanceOf(OXPFeaturesReplyVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(featuresReply.getClass()));

        OXPFeaturesReply messageRev = (OXPFeaturesReply) message;
        assertThat(featuresReply, is(messageRev));
    }
}
