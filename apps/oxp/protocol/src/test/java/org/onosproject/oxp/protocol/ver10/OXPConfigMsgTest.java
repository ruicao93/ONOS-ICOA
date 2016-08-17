package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-21.
 */
public class OXPConfigMsgTest extends TestBaseVer10 {
    @Test
    public void OXPGetConfigRequestMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPGetConfigRequest configRequest = getMsgFactory()
                .buildGetConfigRequest()
                .build();
        configRequest.writeTo(buffer);
        assertThat(configRequest, instanceOf(OXPGetConfigRequestVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(configRequest.getClass()));

        OXPGetConfigRequest messageRev = (OXPGetConfigRequest) message;
        assertThat(configRequest, is(messageRev));
    }

    @Test
    public void OXPGetConfigReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        Set<OXPConfigFlags> flags = EnumSet.noneOf(OXPConfigFlags.class);
        flags.add(OXPConfigFlags.MODE_ADVANCED);
        flags.add(OXPConfigFlags.CAP_BW);
        OXPGetConfigReply configReply = getMsgFactory()
                .buildGetConfigReply()
                .setFlags(flags)
                .build();
        configReply.writeTo(buffer);
        assertThat(configReply, instanceOf(OXPGetConfigReplyVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(configReply.getClass()));

        OXPGetConfigReply messageRev = (OXPGetConfigReply) message;
        assertThat(configReply, is(messageRev));
    }

    @Test
    public void OXPSetConfigMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        Set<OXPConfigFlags> flags = EnumSet.noneOf(OXPConfigFlags.class);
        flags.add(OXPConfigFlags.MODE_ADVANCED);
        flags.add(OXPConfigFlags.CAP_BW);
        OXPSetConfig setConfig = getMsgFactory()
                .buildSetConfig()
                .setFlags(flags)
                .build();
        setConfig.writeTo(buffer);
        assertThat(setConfig, instanceOf(OXPSetConfigVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(setConfig.getClass()));

        OXPSetConfig messageRev = (OXPSetConfig) message;
        assertThat(setConfig, is(messageRev));
    }
}
