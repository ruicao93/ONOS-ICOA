package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPTopologyReply;
import org.onosproject.oxp.protocol.OXPTopologyRequest;
import org.onosproject.oxp.protocol.OXPVersion;
import org.onosproject.oxp.types.OXPInternalLink;
import org.onosproject.oxp.types.OXPVport;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-21.
 */
public class OXPTopologyMsgTest extends TestBaseVer10 {
    @Test
    public void OXPTopologyRequestMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPTopologyRequest topologyRequest = getMsgFactory()
                .buildTopologyRequest()
                .build();
        topologyRequest.writeTo(buffer);
        assertThat(topologyRequest, instanceOf(OXPTopologyRequestVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(topologyRequest.getClass()));

        OXPTopologyRequest messageRev = (OXPTopologyRequest) message;
        assertThat(topologyRequest, is(messageRev));
    }

    @Test
    public void OXPTopologyReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPVport vport = OXPVport.ofShort((short) 1);
        List<OXPInternalLink> internalLinks = new ArrayList<>();
        internalLinks.add(OXPInternalLink.of(vport, vport, 9999999464L, OXPVersion.OXP_10));
        OXPTopologyReply topologyReply = getMsgFactory()
                .buildTopologyReply()
                .setInternalLink(internalLinks)
                .build();
        topologyReply.writeTo(buffer);
        assertThat(topologyReply, instanceOf(OXPTopologyReplyVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(topologyReply.getClass()));

        OXPTopologyReply messageRev = (OXPTopologyReply) message;
        assertThat(topologyReply, is(messageRev));
    }
}
